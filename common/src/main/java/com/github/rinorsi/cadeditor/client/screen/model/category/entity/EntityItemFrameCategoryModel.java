package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.ItemFrameItemEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Provides a dedicated category for editing item frame and glow item frame properties.
 */
public class EntityItemFrameCategoryModel extends EntityCategoryModel {
    private ItemFrameItemEntryModel itemEntry;
    private float itemDropChance;
    private int itemRotation;

    public EntityItemFrameCategoryModel(EntityEditorModel editor) {
        super(ModTexts.ITEM_FRAME, editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();

        Direction facing = Direction.from3DDataValue(Byte.toUnsignedInt(data.getByteOr("Facing", (byte) 0)));
        EnumEntryModel<Direction> facingEntry = new EnumEntryModel<>(this, ModTexts.ITEM_FRAME_FACING, Direction.values(), facing, this::setFacing);
        facingEntry.withTextFactory(ModTexts::direction);
        getEntries().add(facingEntry);

        getEntries().add(new BooleanEntryModel(this, ModTexts.ITEM_FRAME_FIXED, data.getBooleanOr("Fixed", false), this::setFixed));
        getEntries().add(new BooleanEntryModel(this, ModTexts.ITEM_FRAME_INVISIBLE, data.getBooleanOr("Invisible", false), this::setInvisible));

        ItemStack currentItem = readDisplayedItem();
        itemEntry = new ItemFrameItemEntryModel(this, currentItem, ModTexts.ITEM_FRAME_ITEM);
        itemEntry.itemStackProperty().addListener(stack -> updateItemData());
        getEntries().add(itemEntry);

        itemDropChance = data.contains("ItemDropChance") ? data.getFloatOr("ItemDropChance", 1f) : 1f;
        getEntries().add(new FloatEntryModel(this, ModTexts.ITEM_FRAME_DROP_CHANCE, itemDropChance, this::setItemDropChance));

        itemRotation = Byte.toUnsignedInt(data.getByteOr("ItemRotation", (byte) 0));
        getEntries().add(new IntegerEntryModel(this, ModTexts.ITEM_FRAME_ROTATION, itemRotation, this::setItemRotation, value -> value != null && value >= 0 && value <= 7));

        updateItemData();
    }

    private void setFacing(Direction direction) {
        getData().putByte("Facing", (byte) direction.get3DDataValue());
    }

    private void setFixed(boolean value) {
        if (value) {
            getData().putBoolean("Fixed", true);
        } else {
            getData().remove("Fixed");
        }
    }

    private void setInvisible(boolean value) {
        if (value) {
            getData().putBoolean("Invisible", true);
        } else {
            getData().remove("Invisible");
        }
    }

    private void setItemDropChance(Float value) {
        itemDropChance = value == null ? 1f : Math.max(0f, Math.min(1f, value));
        updateItemData();
    }

    private void setItemRotation(Integer value) {
        itemRotation = value == null ? 0 : Math.floorMod(value, 8);
        updateItemData();
    }

    private ItemStack readDisplayedItem() {
        CompoundTag itemTag = getData().getCompound("Item").orElse(null);
        if (itemTag == null || itemTag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ClientUtil.parseItemStack(ClientUtil.registryAccess(), itemTag);
    }

    private void updateItemData() {
        if (itemEntry == null) {
            return;
        }
        ItemStack stack = itemEntry.getItemStack();
        CompoundTag data = getData();
        if (stack.isEmpty()) {
            data.remove("Item");
            data.remove("ItemDropChance");
            data.remove("ItemRotation");
            return;
        }
        data.put("Item", ClientUtil.saveItemStack(ClientUtil.registryAccess(), stack));
        if (Math.abs(itemDropChance - 1f) > 1.0e-6f) {
            data.putFloat("ItemDropChance", itemDropChance);
        } else {
            data.remove("ItemDropChance");
        }
        data.putByte("ItemRotation", (byte) itemRotation);
    }

    @Override
    public void apply() {
        super.apply();
        if (itemEntry != null) {
            itemEntry.apply();
        }
        updateItemData();
    }
}
