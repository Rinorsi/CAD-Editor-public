package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.HideFlagEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Modifier;
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
        CompoundTag components = root.getCompound("components").orElse(null);
        if (components != null) {
            if (components.contains("minecraft:hide_tooltip")) {
                flags.add(HideFlag.OTHER);
                return flags;
            }
            if (components.contains("minecraft:hide_additional_tooltip")) {
                for (HideFlag flag : HideFlag.values()) {
                    if (flag != HideFlag.OTHER) {
                        flags.add(flag);
                    }
                }
                return flags;
            }
        }
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
        try {
            for (HideFlag flag : HideFlag.values()) {
                if (flag == HideFlag.OTHER) continue;
                for (DataComponentType<?> type : flag.hiddenComponents()) {
                    Object value = stack.get(type);
                    if (value == null) continue;
                    boolean show = true;
                    try {
                        var getter = value.getClass().getMethod("showInTooltip");
                        show = (boolean) getter.invoke(value);
                    } catch (ReflectiveOperationException ignored) {
                        try {
                            var field = value.getClass().getDeclaredField("showInTooltip");
                            field.setAccessible(true);
                            show = field.getBoolean(value);
                        } catch (ReflectiveOperationException ignored2) {}
                    }
                    if (!show) {
                        flags.add(flag);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
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
        ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
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

    private static boolean applyKnownTooltipToggle(ItemStack stack, DataComponentType<?> type, Object value, boolean show) {
        return false;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setShowInTooltip(ItemStack stack, DataComponentType type, boolean show) {
        ensureToggleTarget(stack, type);
        Object value = stack.get(type);
        if (value == null) {
            featureLog("toggle.skip", () -> componentName(type) + " missing; cannot toggle");
            return;
        }
        if (applyKnownTooltipToggle(stack, type, value, show)) {
            featureLog("toggle.vanilla", () -> componentName(type) + " -> " + (show ? "show" : "hide") + " via withTooltip");
            return;
        }
        try {
            Class<?> clazz = value.getClass();
            Boolean current = null;
            try {
                var getter = clazz.getMethod("showInTooltip");
                current = (Boolean) getter.invoke(value);
            } catch (NoSuchMethodException ignored) {
                try {
                    var field = clazz.getDeclaredField("showInTooltip");
                    field.setAccessible(true);
                    current = field.getBoolean(value);
                } catch (NoSuchFieldException | IllegalAccessException ignored2) {}
            }
            if (current != null && current == show) {
                featureLog("toggle.reflect.skip", () -> componentName(type) + " already " + (show ? "visible" : "hidden"));
                return;
            }

            try {
                var method = clazz.getDeclaredMethod("withTooltip", boolean.class);
                method.setAccessible(true);
                Object newVal = method.invoke(value, show);
                if (newVal != null) {
                    stack.set(type, newVal);
                    featureLog("toggle.reflect.withTooltip", () -> componentName(type) + " -> " + (show ? "show" : "hide"));
                    return;
                }
            } catch (NoSuchMethodException ignored) {
                try {
                    var method = clazz.getDeclaredMethod("withTooltip", Boolean.class);
                    method.setAccessible(true);
                    Object newVal = method.invoke(value, show);
                    if (newVal != null) {
                        stack.set(type, newVal);
                        featureLog("toggle.reflect.withTooltip", () -> componentName(type) + " -> " + (show ? "show" : "hide"));
                        return;
                    }
                } catch (NoSuchMethodException ignored2) {}
            }

            try {
                var method = clazz.getMethod("withShowInTooltip", boolean.class);
                if (clazz.isAssignableFrom(method.getReturnType())) {
                    Object newVal = method.invoke(value, show);
                    if (newVal != null) {
                        stack.set(type, newVal);
                        featureLog("toggle.reflect.direct", () -> componentName(type) + " -> " + (show ? "show" : "hide"));
                        return;
                    }
                }
            } catch (NoSuchMethodException ignored) {}

            for (var method : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> paramType = method.getParameterTypes()[0];
                if (paramType != boolean.class && paramType != Boolean.class) {
                    continue;
                }
                if (!clazz.isAssignableFrom(method.getReturnType())) {
                    continue;
                }
                try {
                    method.setAccessible(true);
                    Object newVal = method.invoke(value, show);
                    if (newVal != null) {
                        stack.set(type, newVal);
                        featureLog("toggle.reflect.method", () -> componentName(type) + " -> " + (show ? "show" : "hide") + " via " + method.getName());
                        return;
                    }
                } catch (ReflectiveOperationException ignored) {}
            }

            if (clazz.isRecord()) {
                var components = clazz.getRecordComponents();
                Object[] args = new Object[components.length];
                Class<?>[] types = new Class<?>[components.length];
                boolean hasShowField = false;
                for (int i = 0; i < components.length; i++) {
                    var rc = components[i];
                    types[i] = rc.getType();
                    Object arg = rc.getAccessor().invoke(value);
                    if (rc.getName().equals("showInTooltip")
                            && (types[i] == boolean.class || types[i] == Boolean.class)) {
                        arg = show;
                        hasShowField = true;
                    }
                    args[i] = arg;
                }
                if (hasShowField) {
                    var ctor = clazz.getDeclaredConstructor(types);
                    ctor.setAccessible(true);
                    Object newVal = ctor.newInstance(args);
                    stack.set(type, newVal);
                    featureLog("toggle.reflect.record", () -> componentName(type) + " rebuilt via record ctor");
                } else {
                    featureLog("toggle.reflect.record.missing", () -> "Record " + clazz.getName() + " lacks showInTooltip field");
                }
            } else {
                featureLog("toggle.reflect.unhandled", () -> "No setter path for " + clazz.getName());
            }
        } catch (ReflectiveOperationException ex) {
            featureLog("toggle.reflect.error", () -> "Failed to toggle " + componentName(type) + ": " + ex.getMessage());
        }
    }

    private static void ensureToggleTarget(ItemStack stack, DataComponentType<?> type) {
        if (stack.get(type) != null) {
            return;
        }
        if (type == DataComponents.ENCHANTMENTS) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        } else if (type == DataComponents.STORED_ENCHANTMENTS) {
            stack.set(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        } else if (type == DataComponents.ATTRIBUTE_MODIFIERS) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        }
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
        private final java.lang.reflect.Constructor<?> ctor;
        private final java.lang.reflect.Method hideTooltipGetter;
        private final java.lang.reflect.Method hiddenComponentsGetter;

        @SuppressWarnings("unchecked")
        private TooltipDisplaySupport() {
            DataComponentType<Object> t = null;
            java.lang.reflect.Constructor<?> c = null;
            java.lang.reflect.Method mHide = null;
            java.lang.reflect.Method mHidden = null;
            try {
                var field = DataComponents.class.getDeclaredField("TOOLTIP_DISPLAY");
                t = (DataComponentType<Object>) field.get(null);
                Class<?> displayClass = Class.forName("net.minecraft.world.item.component.TooltipDisplay");
                c = displayClass.getDeclaredConstructor(boolean.class, Set.class);
                mHide = displayClass.getMethod("hideTooltip");
                mHidden = displayClass.getMethod("hiddenComponents");
            } catch (ReflectiveOperationException ignored) {}
            this.type = t;
            this.ctor = c;
            this.hideTooltipGetter = mHide;
            this.hiddenComponentsGetter = mHidden;
        }

        private boolean available() {
            return type != null && ctor != null && hideTooltipGetter != null && hiddenComponentsGetter != null;
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
            try {
                if ((boolean) hideTooltipGetter.invoke(value)) {
                    flags.add(HideFlag.OTHER);
                }
                Object hidden = hiddenComponentsGetter.invoke(value);
                if (hidden instanceof Iterable<?> it) {
                    for (Object o : it) {
                        if (o instanceof DataComponentType<?> dc) {
                            HideFlag flag = HideFlag.fromComponent(dc);
                            if (flag != null) {
                                flags.add(flag);
                            }
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {}
            return flags;
        }

        private boolean apply(ItemStack stack, boolean hideTooltip, Set<DataComponentType<?>> components) {
            if (!available()) {
                return false;
            }
            try {
                if (!hideTooltip && components.isEmpty()) {
                    stack.remove(type);
                } else {
                    Object value = ctor.newInstance(hideTooltip, Set.copyOf(components));
                    stack.set(type, value);
                }
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private void clear(ItemStack stack) {
            if (type != null) {
                stack.remove(type);
            }
        }
    }
}
