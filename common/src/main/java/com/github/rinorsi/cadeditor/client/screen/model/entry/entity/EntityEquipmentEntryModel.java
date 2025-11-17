package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel.Slot;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@SuppressWarnings("this-escape")
public class EntityEquipmentEntryModel extends EntryModel {
    private final EntityEquipmentCategoryModel category;
    private final Slot slot;
    private final ObjectProperty<ItemStack> itemProperty;
    private final ObjectProperty<Float> dropChanceProperty;
    private ItemStack defaultItem;
    private float defaultDropChance;

    public EntityEquipmentEntryModel(EntityEquipmentCategoryModel category, Slot slot, ItemStack stack, float dropChance) {
        super(category);
        this.category = category;
        this.slot = slot;
        this.defaultItem = sanitize(stack);
        this.defaultDropChance = dropChance;
        this.itemProperty = ObjectProperty.create(this.defaultItem.copy());
        this.dropChanceProperty = ObjectProperty.create(dropChance);
        this.itemProperty.addListener(value -> invalidateIfNecessary());
        this.dropChanceProperty.addListener(value -> invalidateIfNecessary());
        invalidateIfNecessary();
    }

    public Slot getSlot() {
        return slot;
    }

    public MutableComponent getSlotLabel() {
        return slot.label();
    }

    public ItemStack getItemStack() {
        ItemStack current = itemProperty.getValue();
        return current == null ? ItemStack.EMPTY : current;
    }

    public ObjectProperty<ItemStack> itemStackProperty() {
        return itemProperty;
    }

    public void setItemStack(ItemStack stack) {
        ItemStack sanitized = sanitize(stack);
        itemProperty.setValue(sanitized);
        invalidateIfNecessary();
    }

    private ItemStack sanitize(ItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        // Preserve placeholder stacks for empty-slot editing flow
        if (stack.getItem() == net.minecraft.world.item.Items.STICK) {
            return new ItemStack(net.minecraft.world.item.Items.STICK);
        }
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (copy.getCount() <= 0) {
            copy.setCount(1);
        }
        return copy;
    }

    public float getDropChance() {
        Float value = dropChanceProperty.getValue();
        return value == null ? slot.defaultDropChance() : value;
    }

    public ObjectProperty<Float> dropChanceProperty() {
        return dropChanceProperty;
    }

    public void setDropChance(float chance) {
        float clamped = Math.max(0f, Math.min(1f, chance));
        dropChanceProperty.setValue(clamped);
        invalidateIfNecessary();
    }

    public String formatDropChance() {
        return category.formatDropChance(getDropChance());
    }

    public boolean isDropChanceTextValid(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            Float.parseFloat(text);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public void setDropChanceFromText(String text) {
        if (!isDropChanceTextValid(text)) {
            setValid(false);
            return;
        }
        try {
            setDropChance(Float.parseFloat(text));
        } catch (NumberFormatException ex) {
            setValid(false);
        }
    }

    public CompoundTag createItemTag() {
        ItemStack stack = getItemStack();
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        return ClientUtil.saveItemStack(ClientUtil.registryAccess(), stack);
    }

    public void markAsDefault() {
        this.defaultItem = getItemStack().copy();
        this.defaultDropChance = getDropChance();
    }

    public float getDefaultDropChance() {
        return defaultDropChance;
    }

    @Override
    public void reset() {
        setItemStack(defaultItem.copy());
        setDropChance(defaultDropChance);
    }

    @Override
    public void apply() {
        markAsDefault();
    }

    @Override
    public Type getType() {
        return Type.ENTITY_EQUIPMENT;
    }

    private void invalidateIfNecessary() {
        Float value = dropChanceProperty.getValue();
        boolean valid = value != null && Float.isFinite(value) && value >= 0f && value <= 1f;
        setValid(valid);
    }

    public boolean isDefaultDropChance() {
        return Math.abs(getDropChance() - slot.defaultDropChance()) < EntityEquipmentCategoryModel.DROP_EPSILON;
    }

    @Override
    public boolean isResetable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityEquipmentEntryModel that)) return false;
        return slot == that.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot);
    }
}
