package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityGeneralCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityMountCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntitySpawnSettingsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityVillagerDataCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityVillagerTradeCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityStatusEffectsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityTamingCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerAbilitiesCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerEnderChestCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerInventoryCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerStatsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerStatusEffectsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.player.EntityPlayerPositionCategoryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Saddleable;

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
            getCategories().add(new EntityEquipmentCategoryModel(this));
            if (!(entity instanceof Player)) {
                getCategories().add(new EntityStatusEffectsCategoryModel(this));
            }
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
            getCategories().add(new EntityPlayerStatusEffectsCategoryModel(this));
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
        String id = tag.getString("id");
        if ("minecraft:player".equals(id) || "player".equals(id)) {
            return true;
        }
        if (tag.contains("EnderItems", Tag.TAG_LIST)
                || tag.contains("abilities", Tag.TAG_COMPOUND)
                || tag.contains("playerGameType", Tag.TAG_INT)
                || tag.contains("recipeBook", Tag.TAG_COMPOUND)) {
            return true;
        }
        if (tag.contains("components", Tag.TAG_COMPOUND)) {
            CompoundTag components = tag.getCompound("components");
            if (components.contains("minecraft:profile", Tag.TAG_COMPOUND)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMountData(Entity entity) {
        if (entity instanceof Saddleable) {
            return true;
        }
        CompoundTag tag = getContext().getTag();
        if (tag == null) {
            return false;
        }
        return tag.contains("Saddle")
                || tag.contains("Saddled")
                || tag.contains("SaddleItem", Tag.TAG_COMPOUND)
                || tag.contains("Passengers", Tag.TAG_LIST)
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
        return tag.contains("Tame", Tag.TAG_BYTE)
                || tag.contains("Owner", Tag.TAG_STRING)
                || tag.contains("OwnerUUID")
                || tag.contains("OwnerUUIDMost", Tag.TAG_LONG)
                || tag.contains("OwnerUUIDLeast", Tag.TAG_LONG);
    }
}
