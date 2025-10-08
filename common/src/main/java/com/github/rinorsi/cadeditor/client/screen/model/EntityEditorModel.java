package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityGeneralCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntitySpawnSettingsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityVillagerTradeCategoryModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;

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
        }
        if (entity instanceof Mob) {
            getCategories().add(new EntitySpawnSettingsCategoryModel(this));
        }
        if (entity instanceof Villager) {
            getCategories().add(new EntityVillagerTradeCategoryModel(this));
        }
    }
}
