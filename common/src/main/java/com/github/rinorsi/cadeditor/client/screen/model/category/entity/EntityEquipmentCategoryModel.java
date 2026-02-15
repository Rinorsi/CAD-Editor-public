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
    private static final String EQUIPMENT_TAG = "equipment";
    private static final String DROP_CHANCES_TAG = "drop_chances";
    private static final String LEGACY_HAND_ITEMS_TAG = "HandItems";
    private static final String LEGACY_HAND_DROPS_TAG = "HandDropChances";
    private static final String LEGACY_ARMOR_ITEMS_TAG = "ArmorItems";
    private static final String LEGACY_ARMOR_DROPS_TAG = "ArmorDropChances";
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
        if (data == null) {
            return ItemStack.EMPTY;
        }

        CompoundTag equipment = data.getCompound(EQUIPMENT_TAG).orElse(null);
        if (equipment != null && equipment.contains(slot.equipmentKey)) {
            CompoundTag itemTag = equipment.getCompound(slot.equipmentKey).orElse(null);
            if (itemTag != null) {
                return ClientUtil.parseItemStack(ClientUtil.registryAccess(), itemTag);
            }
        }

        if (!data.contains(slot.legacyItemListTag)) {
            return ItemStack.EMPTY;
        }
        ListTag list = data.getListOrEmpty(slot.legacyItemListTag);
        if (slot.legacyIndex >= list.size()) {
            return ItemStack.EMPTY;
        }
        Tag tag = list.get(slot.legacyIndex);
        if (!(tag instanceof CompoundTag compound)) {
            return ItemStack.EMPTY;
        }
        return ClientUtil.parseItemStack(ClientUtil.registryAccess(), compound);
    }

    private float readDropChance(Slot slot) {
        CompoundTag data = getData();
        if (data == null) {
            return slot.defaultDropChance;
        }

        CompoundTag dropChances = data.getCompound(DROP_CHANCES_TAG).orElse(null);
        if (dropChances != null && dropChances.contains(slot.equipmentKey)) {
            return dropChances.getFloatOr(slot.equipmentKey, slot.defaultDropChance);
        }

        if (!data.contains(slot.legacyDropChanceListTag)) {
            return slot.defaultDropChance;
        }
        ListTag list = data.getListOrEmpty(slot.legacyDropChanceListTag);
        if (slot.legacyIndex >= list.size()) {
            return slot.defaultDropChance;
        }
        Tag tag = list.get(slot.legacyIndex);
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
        CompoundTag data = getData();
        CompoundTag equipment = data.getCompound(EQUIPMENT_TAG).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        CompoundTag dropChances = data.getCompound(DROP_CHANCES_TAG).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        for (Slot slot : Slot.values()) {
            equipment.remove(slot.equipmentKey);
            dropChances.remove(slot.equipmentKey);
        }

        boolean managedEquipment = false;
        boolean managedDropChances = false;

        for (EntityEquipmentEntryModel entry : equipmentEntries) {
            Slot slot = entry.getSlot();
            CompoundTag itemTag = entry.createItemTag();
            if (!itemTag.isEmpty()) {
                equipment.put(slot.equipmentKey, itemTag);
                managedEquipment = true;
            }
            if (!itemTag.isEmpty() && !entry.isDefaultDropChance()) {
                dropChances.putFloat(slot.equipmentKey, entry.getDropChance());
                managedDropChances = true;
            }
        }

        if (managedEquipment || !equipment.isEmpty()) {
            data.put(EQUIPMENT_TAG, equipment);
        } else {
            data.remove(EQUIPMENT_TAG);
        }
        if (managedDropChances || !dropChances.isEmpty()) {
            data.put(DROP_CHANCES_TAG, dropChances);
        } else {
            data.remove(DROP_CHANCES_TAG);
        }

        data.remove(LEGACY_HAND_ITEMS_TAG);
        data.remove(LEGACY_HAND_DROPS_TAG);
        data.remove(LEGACY_ARMOR_ITEMS_TAG);
        data.remove(LEGACY_ARMOR_DROPS_TAG);
    }

    public String formatDropChance(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    public enum Slot {
        MAIN_HAND("mainhand", LEGACY_HAND_ITEMS_TAG, LEGACY_HAND_DROPS_TAG, 0, true, () -> ModTexts.MAIN_HAND.copy()),
        OFF_HAND("offhand", LEGACY_HAND_ITEMS_TAG, LEGACY_HAND_DROPS_TAG, 1, true, () -> ModTexts.OFF_HAND.copy()),
        FEET("feet", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 0, false, () -> ModTexts.FEET.copy()),
        LEGS("legs", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 1, false, () -> ModTexts.LEGS.copy()),
        CHEST("chest", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 2, false, () -> ModTexts.CHEST.copy()),
        HEAD("head", LEGACY_ARMOR_ITEMS_TAG, LEGACY_ARMOR_DROPS_TAG, 3, false, () -> ModTexts.HEAD.copy());

        private final String equipmentKey;
        private final String legacyItemListTag;
        private final String legacyDropChanceListTag;
        private final int legacyIndex;
        private final boolean hand;
        private final Supplier<MutableComponent> labelSupplier;
        private final float defaultDropChance;

        Slot(String equipmentKey, String legacyItemListTag, String legacyDropChanceListTag, int legacyIndex, boolean hand, Supplier<MutableComponent> labelSupplier) {
            this(equipmentKey, legacyItemListTag, legacyDropChanceListTag, legacyIndex, hand, labelSupplier, DEFAULT_DROP_CHANCE);
        }

        Slot(String equipmentKey, String legacyItemListTag, String legacyDropChanceListTag, int legacyIndex, boolean hand, Supplier<MutableComponent> labelSupplier, float defaultDropChance) {
            this.equipmentKey = equipmentKey;
            this.legacyItemListTag = legacyItemListTag;
            this.legacyDropChanceListTag = legacyDropChanceListTag;
            this.legacyIndex = legacyIndex;
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
