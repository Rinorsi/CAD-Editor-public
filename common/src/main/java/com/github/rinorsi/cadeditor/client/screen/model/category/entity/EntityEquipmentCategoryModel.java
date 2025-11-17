package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.EntityEquipmentEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class EntityEquipmentCategoryModel extends EntityCategoryModel {
    private static final String HAND_ITEMS_TAG = "HandItems";
    private static final String HAND_DROPS_TAG = "HandDropChances";
    private static final String ARMOR_ITEMS_TAG = "ArmorItems";
    private static final String ARMOR_DROPS_TAG = "ArmorDropChances";
    public static final float DEFAULT_DROP_CHANCE = 0.085f;
    public static final float DROP_EPSILON = 1.0e-4f;

    private final List<EntityEquipmentEntryModel> equipmentEntries = new ArrayList<>();

    public EntityEquipmentCategoryModel(EntityEditorModel editor) {
        super(ModTexts.ENTITY_EQUIPMENT, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().clear();
        equipmentEntries.clear();
        for (Slot slot : Slot.values()) {
            ItemStack stack = readItem(slot);
            float dropChance = readDropChance(slot);
            EntityEquipmentEntryModel entry = new EntityEquipmentEntryModel(this, slot, stack, dropChance);
            equipmentEntries.add(entry);
            getEntries().add(entry);
        }
    }

    private ItemStack readItem(Slot slot) {
        CompoundTag data = getData();
        if (data == null || !data.contains(slot.itemListTag)) {
            return ItemStack.EMPTY;
        }
        ListTag list = data.getListOrEmpty(slot.itemListTag);
        if (slot.index >= list.size()) {
            return ItemStack.EMPTY;
        }
        Tag tag = list.get(slot.index);
        if (!(tag instanceof CompoundTag compound)) {
            return ItemStack.EMPTY;
        }
        return ClientUtil.parseItemStack(ClientUtil.registryAccess(), compound);
    }

    private float readDropChance(Slot slot) {
        CompoundTag data = getData();
        if (data == null || !data.contains(slot.dropChanceListTag)) {
            return slot.defaultDropChance;
        }
        ListTag list = data.getListOrEmpty(slot.dropChanceListTag);
        if (slot.index >= list.size()) {
            return slot.defaultDropChance;
        }
        Tag tag = list.get(slot.index);
        if (tag instanceof FloatTag floatTag) {
            return floatTag.floatValue();
        }
        return slot.defaultDropChance;
    }

    @Override
    public void apply() {
        super.apply();
        writeToTag();
    }

    private void writeToTag() {
        ListTag handItems = new ListTag();
        ListTag handDropChances = new ListTag();
        ListTag armorItems = new ListTag();
        ListTag armorDropChances = new ListTag();

        boolean hasHandItems = false;
        boolean hasArmorItems = false;
        boolean customHandDropChance = false;
        boolean customArmorDropChance = false;

        for (EntityEquipmentEntryModel entry : equipmentEntries) {
            Slot slot = entry.getSlot();
            // Treat placeholder stick as empty (ignore when writing)
            CompoundTag itemTag;
            if (entry.getItemStack().is(net.minecraft.world.item.Items.STICK)) {
                itemTag = new CompoundTag();
            } else {
                itemTag = entry.createItemTag();
            }
            FloatTag dropTag = FloatTag.valueOf(entry.getDropChance());
            if (slot.isHand()) {
                handItems.add(itemTag);
                handDropChances.add(dropTag);
                if (!itemTag.isEmpty()) {
                    hasHandItems = true;
                }
                if (!entry.isDefaultDropChance()) {
                    customHandDropChance = true;
                }
            } else {
                armorItems.add(itemTag);
                armorDropChances.add(dropTag);
                if (!itemTag.isEmpty()) {
                    hasArmorItems = true;
                }
                if (!entry.isDefaultDropChance()) {
                    customArmorDropChance = true;
                }
            }
        }

        CompoundTag data = getData();
        if (hasHandItems) {
            data.put(HAND_ITEMS_TAG, handItems);
        } else {
            data.remove(HAND_ITEMS_TAG);
        }
        if (customHandDropChance && hasHandItems) {
            data.put(HAND_DROPS_TAG, handDropChances);
        } else {
            data.remove(HAND_DROPS_TAG);
        }

        if (hasArmorItems) {
            data.put(ARMOR_ITEMS_TAG, armorItems);
        } else {
            data.remove(ARMOR_ITEMS_TAG);
        }
        if (customArmorDropChance && hasArmorItems) {
            data.put(ARMOR_DROPS_TAG, armorDropChances);
        } else {
            data.remove(ARMOR_DROPS_TAG);
        }
    }

    public String formatDropChance(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    public enum Slot {
        MAIN_HAND(HAND_ITEMS_TAG, HAND_DROPS_TAG, 0, true, () -> ModTexts.MAIN_HAND.copy()),
        OFF_HAND(HAND_ITEMS_TAG, HAND_DROPS_TAG, 1, true, () -> ModTexts.OFF_HAND.copy()),
        FEET(ARMOR_ITEMS_TAG, ARMOR_DROPS_TAG, 0, false, () -> ModTexts.FEET.copy()),
        LEGS(ARMOR_ITEMS_TAG, ARMOR_DROPS_TAG, 1, false, () -> ModTexts.LEGS.copy()),
        CHEST(ARMOR_ITEMS_TAG, ARMOR_DROPS_TAG, 2, false, () -> ModTexts.CHEST.copy()),
        HEAD(ARMOR_ITEMS_TAG, ARMOR_DROPS_TAG, 3, false, () -> ModTexts.HEAD.copy());

        private final String itemListTag;
        private final String dropChanceListTag;
        private final int index;
        private final boolean hand;
        private final Supplier<MutableComponent> labelSupplier;
        private final float defaultDropChance;

        Slot(String itemListTag, String dropChanceListTag, int index, boolean hand, Supplier<MutableComponent> labelSupplier) {
            this(itemListTag, dropChanceListTag, index, hand, labelSupplier, DEFAULT_DROP_CHANCE);
        }

        Slot(String itemListTag, String dropChanceListTag, int index, boolean hand, Supplier<MutableComponent> labelSupplier, float defaultDropChance) {
            this.itemListTag = itemListTag;
            this.dropChanceListTag = dropChanceListTag;
            this.index = index;
            this.hand = hand;
            this.labelSupplier = labelSupplier;
            this.defaultDropChance = defaultDropChance;
        }

        public boolean isHand() {
            return hand;
        }

        public MutableComponent label() {
            return labelSupplier.get();
        }

        public float defaultDropChance() {
            return defaultDropChance;
        }
    }
}
