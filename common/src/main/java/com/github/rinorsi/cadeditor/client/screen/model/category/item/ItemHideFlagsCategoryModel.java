package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.HideFlagEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemHideFlagsCategoryModel extends ItemEditorCategoryModel {

    private static final Logger LOGGER = LogManager.getLogger("CAD-Editor/HideFlags");
    private static final Set<DataComponentType<?>> TOOLTIP_TOGGLE_COMPONENTS = Set.of(
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.UNBREAKABLE,
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON,
            DataComponents.DYED_COLOR,
            DataComponents.TRIM,
            DataComponents.JUKEBOX_PLAYABLE
    );

    private final EnumSet<HideFlag> selectedFlags = EnumSet.noneOf(HideFlag.class);

    public ItemHideFlagsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.HIDE_FLAGS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        EnumSet<HideFlag> initial = EnumSet.noneOf(HideFlag.class);
        selectedFlags.clear();
        initial.addAll(TooltipDisplaySupport.INSTANCE.read(stack));
        initial.addAll(readComponentVisibility(stack));
        initial.addAll(readLegacyFlags(stack));

        for (HideFlag flag : HideFlag.values()) {
            boolean selected = initial.contains(flag);
            if (selected) {
                selectedFlags.add(flag);
            }
            getEntries().add(new HideFlagEntryModel(this, flag, selected, value -> setFlag(flag, value)));
        }
    }

    private EnumSet<HideFlag> readLegacyFlags(ItemStack stack) {
        EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
        CompoundTag root = ClientUtil.saveItemStack(ClientUtil.registryAccess(), stack);
        int mask = getTag() != null ? getTag().getIntOr("HideFlags", 0) : 0;
        if (mask != 0) {
            for (HideFlag flag : HideFlag.values()) {
                if ((mask & flag.getValue()) != 0) {
                    flags.add(flag);
                }
            }
        }
        return flags;
    }

    @Override
    public void apply() {
        super.apply();
        refreshSelectedFlags();
        featureLog("apply.selected", () -> "Selected flags: " + selectedFlags);

        ItemStack stack = getParent().getContext().getItemStack();

        TooltipDisplaySupport.INSTANCE.clear(stack);

        EnumSet<HideFlag> componentFlags = EnumSet.copyOf(selectedFlags);
        boolean hideTooltip = componentFlags.remove(HideFlag.OTHER);

        Set<DataComponentType<?>> hiddenComponents = new LinkedHashSet<>();
        for (HideFlag flag : componentFlags) {
            hiddenComponents.addAll(flag.hiddenComponents());
        }
        featureLog("apply.targets", () -> "hideTooltip=" + hideTooltip + ", componentFlags=" + componentFlags
                + ", hiddenComponents=" + describeComponents(hiddenComponents));

        applyComponentVisibility(stack, componentFlags);
        boolean tooltipApplied = TooltipDisplaySupport.INSTANCE.apply(stack, hideTooltip, hiddenComponents);
        if (!tooltipApplied) {
            featureLog("apply.tooltip_display_missing", () -> "TooltipDisplay unavailable; tooltip suppression incomplete");
        }
        if (getTag() != null) {
            getTag().remove("HideFlags");
        }

        syncEntriesWithStack(stack);
    }

    private void refreshSelectedFlags() {
        selectedFlags.clear();
        for (var entry : getEntries()) {
            if (entry instanceof HideFlagEntryModel flagEntry && Boolean.TRUE.equals(flagEntry.getValue())) {
                selectedFlags.add(flagEntry.getHideFlag());
            }
        }
    }

    private void syncEntriesWithStack(ItemStack stack) {
        EnumSet<HideFlag> actual = EnumSet.noneOf(HideFlag.class);
        actual.addAll(TooltipDisplaySupport.INSTANCE.read(stack));
        actual.addAll(readComponentVisibility(stack));
        actual.addAll(readLegacyFlags(stack));

        selectedFlags.clear();
        selectedFlags.addAll(actual);

        for (var entry : getEntries()) {
            if (entry instanceof HideFlagEntryModel flagEntry) {
                flagEntry.syncValue(selectedFlags.contains(flagEntry.getHideFlag()));
            }
        }
    }

    private EnumSet<HideFlag> readComponentVisibility(ItemStack stack) {
        EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
        for (HideFlag flag : HideFlag.values()) {
            if (flag == HideFlag.OTHER) continue;
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                Object value = stack.get(type);
                if (value == null) continue;
                Boolean visible = readShowFlag(type, value);
                if (visible != null && !visible) {
                    flags.add(flag);
                    break;
                }
            }
        }
        return flags;
    }

    private void setFlag(HideFlag flag, boolean value) {
        if (value) {
            selectedFlags.add(flag);
        } else {
            selectedFlags.remove(flag);
        }
    }

    private void applyComponentVisibility(ItemStack stack, Set<HideFlag> flags) {
        for (HideFlag flag : HideFlag.values()) {
            if (flag == HideFlag.OTHER) continue;
            boolean show = !flags.contains(flag);
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                setShowInTooltip(stack, type, show);
            }
        }
    }

    private static void featureLog(String stage, Supplier<String> messageSupplier) {
        if (isFeatureDebugEnabled()) {
            LOGGER.info("[CAD-Editor][HideFlags][{}] {}", stage, messageSupplier.get());
        }
    }

    private static String componentName(DataComponentType<?> type) {
        Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
        return id == null ? String.valueOf(type) : id.toString();
    }

    private static String describeComponents(Collection<DataComponentType<?>> components) {
        if (components == null || components.isEmpty()) {
            return "[]";
        }
        return components.stream()
                .map(ItemHideFlagsCategoryModel::componentName)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static <T> boolean applyKnownTooltipToggle(ItemStack stack, DataComponentType<T> type, T value, boolean show) {
        if (!TOOLTIP_TOGGLE_COMPONENTS.contains(type)) {
            return false;
        }
        Codec<T> codec = type.codec();
        if (codec == null) {
            return false;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, ClientUtil.registryAccess());
        Tag encoded = codec.encodeStart(ops, value).result().orElse(null);
        if (!(encoded instanceof CompoundTag compound)) {
            return false;
        }
        compound.putBoolean("show_in_tooltip", show);
        T decoded = codec.parse(ops, compound).result().orElse(null);
        if (decoded == null) {
            return false;
        }
        stack.set(type, decoded);
        return true;
    }

    private static void featureLog(String stage, String message) {
        if (isFeatureDebugEnabled()) {
            LOGGER.info("[CAD-Editor][HideFlags][{}] {}", stage, message);
        }
    }

    private static boolean isFeatureDebugEnabled() {
        try {
            return ClientConfiguration.INSTANCE != null
                    && ClientConfiguration.INSTANCE.getGuapiDebugMode() == DebugMode.FEATURE;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void setShowInTooltip(ItemStack stack, DataComponentType<?> rawType, boolean show) {
        DataComponentType<Object> type = (DataComponentType<Object>) rawType;
        Object value = stack.get(type);
        if (value == null) {
            return;
        }
        applyKnownTooltipToggle(stack, type, value, show);
    }

    public enum HideFlag {
        ENCHANTMENTS(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS),
        ATTRIBUTE_MODIFIERS(DataComponents.ATTRIBUTE_MODIFIERS),
        UNBREAKABLE(DataComponents.UNBREAKABLE),
        CAN_DESTROY(DataComponents.CAN_BREAK),
        CAN_PLACE_ON(DataComponents.CAN_PLACE_ON),
        OTHER(),
        DYED(DataComponents.DYED_COLOR),
        ARMOR_TRIMS(DataComponents.TRIM),
        JUKEBOX(DataComponents.JUKEBOX_PLAYABLE),
        LORE(DataComponents.LORE);

        private static final Map<DataComponentType<?>, HideFlag> COMPONENT_TO_FLAG = new HashMap<>();
        static {
            for (HideFlag flag : values()) {
                for (DataComponentType<?> type : flag.hiddenComponents) {
                    COMPONENT_TO_FLAG.put(type, flag);
                }
            }
        }

        private final Set<DataComponentType<?>> hiddenComponents;

        HideFlag(DataComponentType<?>... components) {
            this.hiddenComponents = Set.of(components);
        }

        public MutableComponent getName() {
            return ModTexts.gui(name().toLowerCase(Locale.ROOT));
        }

        public int getValue() {
            return 1 << ordinal();
        }

        public Collection<DataComponentType<?>> hiddenComponents() {
            return hiddenComponents;
        }

        public static HideFlag fromComponent(DataComponentType<?> type) {
            return COMPONENT_TO_FLAG.get(type);
        }
    }

    private static final class TooltipDisplaySupport {
        static final TooltipDisplaySupport INSTANCE = new TooltipDisplaySupport();

        private final DataComponentType<Object> type;

        @SuppressWarnings("unchecked")
        private TooltipDisplaySupport() {
            DataComponentType<Object> t = null;
            try {
                t = BuiltInRegistries.DATA_COMPONENT_TYPE
                        .get(Identifier.withDefaultNamespace("tooltip_display"))
                        .map(Holder.Reference::value)
                        .map(value -> (DataComponentType<Object>) value)
                        .orElse(null);
            } catch (Exception ignored) {}
            this.type = t;
        }

        private boolean available() {
            return type != null && type.codec() != null;
        }

        private EnumSet<HideFlag> read(ItemStack stack) {
            EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
            if (!available()) {
                return flags;
            }
            Object value = stack.get(type);
            if (value == null) {
                return flags;
            }
            Codec<Object> codec = type.codec();
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, ClientUtil.registryAccess());
            Tag encoded = codec.encodeStart(ops, value).result().orElse(null);
            if (!(encoded instanceof CompoundTag compound)) {
                return flags;
            }
            if (compound.getBooleanOr("hide_tooltip", false)) {
                flags.add(HideFlag.OTHER);
            }
            ListTag hiddenList = compound.getList("hidden_components").orElse(new ListTag());
            for (Tag entry : hiddenList) {
                if (!(entry instanceof StringTag stringTag)) continue;
                Identifier id = Identifier.tryParse(stringTag.value());
                if (id == null) continue;
                DataComponentType<?> dc = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id)
                        .map(Holder.Reference::value)
                        .orElse(null);
                if (dc == null) continue;
                HideFlag flag = HideFlag.fromComponent(dc);
                if (flag != null) {
                    flags.add(flag);
                }
            }
            return flags;
        }

        private boolean apply(ItemStack stack, boolean hideTooltip, Set<DataComponentType<?>> components) {
            if (!available()) {
                return false;
            }
            Codec<Object> codec = type.codec();
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, ClientUtil.registryAccess());
            if (!hideTooltip && components.isEmpty()) {
                stack.remove(type);
                return true;
            }
            CompoundTag payload = new CompoundTag();
            payload.putBoolean("hide_tooltip", hideTooltip);
            if (!components.isEmpty()) {
                ListTag hidden = new ListTag();
                for (DataComponentType<?> component : components) {
                    Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component);
                    if (id != null) {
                        hidden.add(StringTag.valueOf(id.toString()));
                    }
                }
                payload.put("hidden_components", hidden);
            }
            Object parsed = codec.parse(ops, payload).result().orElse(null);
            if (parsed == null) {
                return false;
            }
            stack.set(type, parsed);
            return true;
        }

        private void clear(ItemStack stack) {
            if (type != null) {
                stack.remove(type);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Boolean readShowFlag(DataComponentType<?> rawType, Object value) {
        DataComponentType<Object> type = (DataComponentType<Object>) rawType;
        if (!TOOLTIP_TOGGLE_COMPONENTS.contains(type)) {
            return null;
        }
        Codec<Object> codec = type.codec();
        if (codec == null) {
            return null;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, ClientUtil.registryAccess());
        Tag encoded = codec.encodeStart(ops, value).result().orElse(null);
        if (!(encoded instanceof CompoundTag compound)) {
            return null;
        }
        if (!compound.contains("show_in_tooltip")) {
            return true;
        }
        return compound.getBooleanOr("show_in_tooltip", true);
    }
}
