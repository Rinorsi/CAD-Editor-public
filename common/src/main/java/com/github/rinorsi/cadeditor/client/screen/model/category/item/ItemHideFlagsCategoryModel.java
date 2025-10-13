package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.HideFlagEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ItemHideFlagsCategoryModel extends ItemEditorCategoryModel {

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
        initial.addAll(readLegacyFlags(stack));
        if (initial.isEmpty()) {
            initial.addAll(readComponentVisibilityFallback(stack));
        }
        initial.addAll(readRemovedComponents(stack));

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
        if (stack.has(DataComponents.HIDE_TOOLTIP)) {
            flags.add(HideFlag.OTHER);
        } else if (stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            for (HideFlag flag : HideFlag.values()) {
                if (flag != HideFlag.OTHER) {
                    flags.add(flag);
                }
            }
        } else {
            int mask = getTag() != null ? getTag().getInt("HideFlags") : 0;
            if (mask != 0) {
                for (HideFlag flag : HideFlag.values()) {
                    if ((mask & flag.getValue()) != 0) {
                        flags.add(flag);
                    }
                }
            }
        }
        return flags;
    }

    @Override
    public void apply() {
        super.apply();
        refreshSelectedFlags();

        ItemStack stack = getParent().getContext().getItemStack();

        TooltipDisplaySupport.INSTANCE.clear(stack);
        stack.remove(DataComponents.HIDE_TOOLTIP);
        stack.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP);

        EnumSet<HideFlag> storedFlags = EnumSet.copyOf(selectedFlags);
        EnumSet<HideFlag> componentFlags = EnumSet.copyOf(storedFlags);
        boolean hideTooltip = componentFlags.remove(HideFlag.OTHER);

        Set<DataComponentType<?>> hiddenComponents = new LinkedHashSet<>();
        for (HideFlag flag : componentFlags) {
            hiddenComponents.addAll(flag.hiddenComponents());
        }

        boolean tooltipApplied = TooltipDisplaySupport.INSTANCE.apply(stack, hideTooltip, hiddenComponents);

        if (!tooltipApplied) {
            applyLegacyComponentVisibility(stack, componentFlags, hideTooltip);
            writeLegacyMask(storedFlags);
        } else if (getTag() != null) {
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
        actual.addAll(readLegacyFlags(stack));
        if (actual.isEmpty()) {
            actual.addAll(readComponentVisibilityFallback(stack));
        }
        actual.addAll(readRemovedComponents(stack));

        selectedFlags.clear();
        selectedFlags.addAll(actual);

        for (var entry : getEntries()) {
            if (entry instanceof HideFlagEntryModel flagEntry) {
                flagEntry.syncValue(selectedFlags.contains(flagEntry.getHideFlag()));
            }
        }
    }

    private EnumSet<HideFlag> readRemovedComponents(ItemStack stack) {
        Tag saved = stack.save(ClientUtil.registryAccess(), new CompoundTag());
        CompoundTag root = saved instanceof CompoundTag compound ? compound : new CompoundTag();
        if (!root.contains("components", Tag.TAG_COMPOUND)) {
            return EnumSet.noneOf(HideFlag.class);
        }
        CompoundTag components = root.getCompound("components");
        EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
        for (HideFlag flag : HideFlag.values()) {
            if (flag == HideFlag.OTHER) continue;
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
                if (id == null) continue;
                String key = "!" + id;
                if (components.contains(key)) {
                    flags.add(flag);
                    break;
                }
            }
        }
        return flags;
    }

    private void applyLegacyComponentVisibility(ItemStack stack, EnumSet<HideFlag> componentFlags, boolean hideTooltip) {
        if (hideTooltip) {
            stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.HIDE_TOOLTIP);
        }

        applyComponentVisibilityFallback(stack, componentFlags);
    }

    private EnumSet<HideFlag> readComponentVisibilityFallback(ItemStack stack) {
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

    private void writeLegacyMask(EnumSet<HideFlag> flags) {
        int mask = 0;
        for (HideFlag flag : flags) {
            mask |= flag.getValue();
        }
        if (mask != 0) {
            getOrCreateTag().putInt("HideFlags", mask);
        } else if (getTag() != null) {
            getTag().remove("HideFlags");
        }
    }

    private void setFlag(HideFlag flag, boolean value) {
        if (value) {
            selectedFlags.add(flag);
        } else {
            selectedFlags.remove(flag);
        }
    }

    private void applyComponentVisibilityFallback(ItemStack stack, Set<HideFlag> flags) {
        for (HideFlag flag : HideFlag.values()) {
            if (flag == HideFlag.OTHER) continue;
            boolean show = !flags.contains(flag);
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                setShowInTooltip(stack, type, show);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setShowInTooltip(ItemStack stack, DataComponentType type, boolean show) {
        Object value = stack.get(type);
        if (value == null) return;
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
            if (current != null && current == show) return;

            try {
                var method = clazz.getMethod("withShowInTooltip", boolean.class);
                if (clazz.isAssignableFrom(method.getReturnType())) {
                    Object newVal = method.invoke(value, show);
                    if (newVal != null) {
                        stack.set(type, newVal);
                        return;
                    }
                }
            } catch (NoSuchMethodException ignored) {}

            for (var method : clazz.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != boolean.class) {
                    continue;
                }
                if (!clazz.isAssignableFrom(method.getReturnType())) {
                    continue;
                }
                try {
                    Object newVal = method.invoke(value, show);
                    if (newVal != null) {
                        stack.set(type, newVal);
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
                }
            }
        } catch (ReflectiveOperationException ignored) {}
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
