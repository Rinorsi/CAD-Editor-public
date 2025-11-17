package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityAttributesCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityGeneralCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityMountCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntitySpawnSettingsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityVillagerDataCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityVillagerTradeCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityTamingCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerAbilitiesCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerEnderChestCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerInventoryCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerStatsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerPositionCategoryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.tags.EntityTypeTags;

public class EntityEditorModel extends StandardEditorModel {
    public EntityEditorModel(EntityEditorContext context) {
        super(context);
    }

    @Override
    public EntityEditorContext getContext() {
        return (EntityEditorContext) super.getContext();
    }


    @Override
    public void save() {
        boolean hadPermission = getContext().hasPermission();
        super.save();
        if (hadPermission && getContext().hasPermission()) {
            ClientUtil.showMessage(ModTexts.Messages.successUpdate(ModTexts.ENTITY));
        }
    }
    @Override
    protected void setupCategories() {
        getCategories().add(new EntityGeneralCategoryModel(this));

        Entity entity = getContext().getEntity();
        if (entity instanceof LivingEntity) {
            getCategories().add(new EntityAttributesCategoryModel(this));
            getCategories().add(new EntityEquipmentCategoryModel(this));
            if (isTamable(entity)) {
                getCategories().add(new EntityTamingCategoryModel(this));
            }
            if (hasMountData(entity)) {
                getCategories().add(new EntityMountCategoryModel(this));
            }
        }
        if (entity instanceof Mob) {
            getCategories().add(new EntitySpawnSettingsCategoryModel(this));
        }
        if (entity instanceof AbstractVillager) {
            getCategories().add(new EntityVillagerDataCategoryModel(this));
            getCategories().add(new EntityVillagerTradeCategoryModel(this));
        }
        if (isPlayerEntity()) {
            getCategories().add(new EntityPlayerInventoryCategoryModel(this));
            getCategories().add(new EntityPlayerEnderChestCategoryModel(this));
            getCategories().add(new EntityPlayerStatsCategoryModel(this));
            getCategories().add(new EntityPlayerAbilitiesCategoryModel(this));
            getCategories().add(new EntityPlayerPositionCategoryModel(this));
        }
    }

    private boolean isPlayerEntity() {
        Entity entity = getContext().getEntity();
        if (entity instanceof Player) {
            return true;
        }
        CompoundTag tag = getContext().getTag();
        if (tag == null) {
            return false;
        }
        String id = tag.getStringOr("id", "");
        if ("minecraft:player".equals(id) || "player".equals(id)) {
            return true;
        }
        if (tag.contains("EnderItems")
                || tag.contains("abilities")
                || tag.contains("playerGameType")
                || tag.contains("recipeBook")) {
            return true;
        }
        if (tag.contains("components")) {
            CompoundTag components = tag.getCompound("components").orElse(null);
            if (components != null && components.contains("minecraft:profile")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMountData(Entity entity) {
        if (entity != null && (entity.getType().is(EntityTypeTags.CAN_EQUIP_SADDLE) || entity.getType().is(EntityTypeTags.CAN_EQUIP_HARNESS))) {
            return true;
        }
        CompoundTag tag = getContext().getTag();
        if (tag == null) {
            return false;
        }
        return tag.contains("Saddle")
                || tag.contains("Saddled")
                || tag.contains("SaddleItem")
                || tag.contains("Passengers")
                || tag.contains("Leash")
                || tag.contains("LeashHolder")
                || tag.contains("ChestedHorse");
    }

    private boolean isTamable(Entity entity) {
        if (entity instanceof TamableAnimal || entity instanceof OwnableEntity) {
            return true;
        }
        CompoundTag tag = getContext().getTag();
        if (tag == null) {
            return false;
        }
        return tag.contains("Tame")
                || tag.contains("Owner")
                || tag.contains("OwnerUUID")
                || tag.contains("OwnerUUIDMost")
                || tag.contains("OwnerUUIDLeast");
    }

    public void handleEntityReplaced(CompoundTag newTag) {
        if (newTag == null) {
            ClientUtil.showMessage(ModTexts.Messages.errorNoTargetFound(ModTexts.ENTITY));
            return;
        }
        CompoundTag copy = newTag.copy();
        CompoundTag current = getContext().getTag();
        if (current != null) {
            copyListTag(current, copy, "Pos", Tag.TAG_DOUBLE);
            copyListTag(current, copy, "Rotation", Tag.TAG_FLOAT);
            copyListTag(current, copy, "Motion", Tag.TAG_DOUBLE);
            copyIntArrayTag(current, copy, "UUID");
            copyLongTag(current, copy, "UUIDMost");
            copyLongTag(current, copy, "UUIDLeast");
        }
        if (!getContext().replaceEntity(copy)) {
            ClientUtil.showMessage(ModTexts.Messages.errorNoTargetFound(ModTexts.ENTITY));
            return;
        }
        Class<?> previousCategory = getSelectedCategory() != null ? getSelectedCategory().getClass() : null;
        getCategories().clear();
        setupCategories();
        getCategories().forEach(category -> category.initalize());
        if (!getCategories().isEmpty()) {
            var toSelect = getCategories().get(0);
            if (previousCategory != null) {
                for (var category : getCategories()) {
                    if (category.getClass() == previousCategory) {
                        toSelect = category;
                        break;
                    }
                }
            }
            setSelectedCategory(toSelect);
        }
    }

    private static void copyListTag(CompoundTag source, CompoundTag target, String key, int elementType) {
        if (source.contains(key)) {
            ListTag list = source.getListOrEmpty(key);
            target.put(key, list.copy());
        } else {
            target.remove(key);
        }
    }

    private static void copyIntArrayTag(CompoundTag source, CompoundTag target, String key) {
        source.getIntArray(key).ifPresentOrElse(
                values -> target.putIntArray(key, values),
                () -> target.remove(key)
        );
    }

    private static void copyLongTag(CompoundTag source, CompoundTag target, String key) {
        source.getLong(key).ifPresentOrElse(
                value -> target.putLong(key, value),
                () -> target.remove(key)
        );
    }
}
