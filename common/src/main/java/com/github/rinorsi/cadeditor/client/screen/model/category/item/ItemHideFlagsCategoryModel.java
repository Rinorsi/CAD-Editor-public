package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.HideFlagEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

public class ItemHideFlagsCategoryModel extends ItemEditorCategoryModel {

    private final EnumSet<HideFlag> selectedFlags = EnumSet.noneOf(HideFlag.class);

    public ItemHideFlagsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.HIDE_FLAGS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        EnumSet<HideFlag> initial = readPerComponentFlags(stack);
        if (initial == null || initial.isEmpty()) {
            initial = readLegacyFlags(stack);
        }
        //BUG 隐藏逻辑还是有问题，会出现不该被隐藏的东西被隐藏了
        for (HideFlag flag : HideFlag.values()) {
            boolean selected = initial.contains(flag);
            getEntries().add(new HideFlagEntryModel(this, flag, selected, value -> setFlag(flag, value)));
        }
    }

    private EnumSet<HideFlag> readLegacyFlags(ItemStack stack) {
        EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
        if (stack.has(DataComponents.HIDE_TOOLTIP)) {
            flags.add(HideFlag.OTHER);
        } else if (stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            for (HideFlag flag : HideFlag.values()) {
                if (flag != HideFlag.OTHER) flags.add(flag);
            }
        } else {
            int mask = getTag() != null ? getTag().getInt("HideFlags") : 0;
            for (HideFlag flag : HideFlag.values()) {
                if ((mask & flag.getValue()) != 0) flags.add(flag);
            }
        }
        return flags;
    }

    @Override
    public void apply() {
        selectedFlags.clear();

        super.apply();

        ItemStack stack = getParent().getContext().getItemStack();

        TooltipDisplaySupport.INSTANCE.clear(stack);
        stack.remove(DataComponents.HIDE_TOOLTIP);
        stack.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP);

        EnumSet<HideFlag> appliedFlags = EnumSet.copyOf(selectedFlags);
        boolean hideTooltip = appliedFlags.remove(HideFlag.OTHER);

        applyPerComponentFlags(stack, appliedFlags);

        Set<DataComponentType<?>> hiddenComponents = new LinkedHashSet<>();
        for (HideFlag flag : appliedFlags) {
            hiddenComponents.addAll(flag.hiddenComponents());
        }

        boolean tooltipApplied = TooltipDisplaySupport.INSTANCE.apply(stack, hideTooltip, hiddenComponents);

        if (!tooltipApplied) {
            applyLegacy(stack, hideTooltip, appliedFlags);
        } else if (getTag() != null) {
            getTag().remove("HideFlags");
        }
    }

    private EnumSet<HideFlag> readPerComponentFlags(ItemStack stack) {
        EnumSet<HideFlag> tooltipFlags = TooltipDisplaySupport.INSTANCE.read(stack);
        if (tooltipFlags != null) {
            if (stack.has(DataComponents.HIDE_TOOLTIP)) {
                tooltipFlags.add(HideFlag.OTHER);
            }
            return tooltipFlags;
        }

        EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
        try {
            for (HideFlag flag : HideFlag.values()) {
                if (flag == HideFlag.OTHER) continue;
                for (DataComponentType<?> type : flag.hiddenComponents()) {
                    Object value = stack.get(type);
                    if (value == null) continue;
                    boolean show = true;
                    try {
                        var m = value.getClass().getMethod("showInTooltip");
                        show = (boolean) m.invoke(value);
                    } catch (ReflectiveOperationException ignored) {
                        try {
                            var field = value.getClass().getDeclaredField("showInTooltip");
                            field.setAccessible(true);
                            show = field.getBoolean(value);
                        } catch (ReflectiveOperationException ignored2) {}
                    }
                    if (!show) { flags.add(flag); break; }
                }
            }
            if (stack.has(DataComponents.HIDE_TOOLTIP)) {
                flags.add(HideFlag.OTHER);
            }
        } catch (Exception ignored) {}
        return flags;
    }

    private void applyLegacy(ItemStack stack, boolean hideTooltip, EnumSet<HideFlag> flags) {
        if (hideTooltip) {
            stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.HIDE_TOOLTIP);
        }

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

    public enum HideFlag {
        ENCHANTMENTS(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS),
        ATTRIBUTE_MODIFIERS(DataComponents.ATTRIBUTE_MODIFIERS),
        UNBREAKABLE(DataComponents.UNBREAKABLE),
        CAN_DESTROY(DataComponents.CAN_BREAK),
        CAN_PLACE_ON(DataComponents.CAN_PLACE_ON),
        OTHER(),
        DYED(DataComponents.DYED_COLOR),
        ARMOR_TRIMS(DataComponents.TRIM),
        JUKEBOX(DataComponents.JUKEBOX_PLAYABLE);

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

    private void applyPerComponentFlags(ItemStack stack, Set<HideFlag> flags) {
        for (HideFlag flag : HideFlag.values()) {
            if (flag == HideFlag.OTHER) continue;
            boolean show = !flags.contains(flag);
            for (DataComponentType<?> type : flag.hiddenComponents()) {
                setShowInTooltip(stack, type, show);
            }
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static void setShowInTooltip(ItemStack stack, DataComponentType type, boolean show) {
        Object value = stack.get(type);
        if (value == null) return;
        try {
            Class<?> c = value.getClass();
            try {
                var acc = c.getMethod("showInTooltip");
                boolean cur = (boolean) acc.invoke(value);
                if (cur == show) return;
            } catch (ReflectiveOperationException ignored) {}

            for (var m : c.getMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == boolean.class
                        && c.isAssignableFrom(m.getReturnType())) {
                    try {
                        Object newVal = m.invoke(value, show);
                        if (newVal != null) {
                            stack.set(type, newVal);
                            return;
                        }
                    } catch (ReflectiveOperationException ignored) {}
                }
            }

            if (c.isRecord()) {
                var comps = c.getRecordComponents();
                var args = new Object[comps.length];
                var types = new Class<?>[comps.length];
                for (int i = 0; i < comps.length; i++) {
                    var rc = comps[i];
                    types[i] = rc.getType();
                    var getter = rc.getAccessor();
                    Object arg = getter.invoke(value);
                    if (rc.getName().equals("showInTooltip") &&
                        (types[i] == boolean.class || types[i] == Boolean.class)) {
                        arg = show;
                    }
                    args[i] = arg;
                }
                var ctor = c.getDeclaredConstructor(types);
                ctor.setAccessible(true);
                Object newVal = ctor.newInstance(args);
                stack.set(type, newVal);
            }
        } catch (ReflectiveOperationException ignored) {}
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
            if (!available()) return null;
            Object value = stack.get(type);
            if (value == null) return null;
            EnumSet<HideFlag> flags = EnumSet.noneOf(HideFlag.class);
            try {
                if ((boolean) hideTooltipGetter.invoke(value)) flags.add(HideFlag.OTHER);
                Object hidden = hiddenComponentsGetter.invoke(value);
                if (hidden instanceof Iterable<?> it) {
                    for (Object o : it) {
                        if (o instanceof DataComponentType<?> dc) {
                            HideFlag flag = HideFlag.fromComponent(dc);
                            if (flag != null && flag != HideFlag.OTHER) flags.add(flag);
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
            return flags;
        }

        private boolean apply(ItemStack stack, boolean hideTooltip, Set<DataComponentType<?>> components) {
            if (!available()) return false;
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
            if (type != null) stack.remove(type);
        }
    }
}
