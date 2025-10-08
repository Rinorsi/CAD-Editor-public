package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class EntityGeneralCategoryModel extends EntityCategoryModel {
    private static final String HEALTH_TAG = "Health";
    private static final String ATTRIBUTES_TAG = "attributes";
    private static final String ATTRIBUTE_ID_TAG = "id";
    private static final String ATTRIBUTE_BASE_TAG = "base";
    private static final String MAX_HEALTH_ATTRIBUTE_ID = "minecraft:generic.max_health";

    public EntityGeneralCategoryModel(EntityEditorModel model) {
        super(ModTexts.GENERAL, model);
    }

    @Override
    protected void setupEntries() {
        getEntries().addAll(
                new FloatEntryModel(this, ModTexts.HEALTH, getData().getFloat(HEALTH_TAG), this::setHealth),
                new TextEntryModel(this, ModTexts.CUSTOM_NAME, getCustomName(), this::setCustomName),
                new BooleanEntryModel(this, ModTexts.ALWAYS_SHOW_NAME, getData().getBoolean("CustomNameVisible"), b -> putBooleanOrRemove("CustomNameVisible", b)),
                new BooleanEntryModel(this, ModTexts.INVULNERABLE, getData().getBoolean("Invulnerable"), b -> getData().putBoolean("Invulnerable", b)),
                new BooleanEntryModel(this, ModTexts.SILENT, getData().getBoolean("Silent"), b -> putBooleanOrRemove("Silent", b)),
                new BooleanEntryModel(this, ModTexts.NO_GRAVITY, getData().getBoolean("NoGravity"), b -> putBooleanOrRemove("NoGravity", b)),
                new BooleanEntryModel(this, ModTexts.GLOWING, getData().getBoolean("Glowing"), b -> putBooleanOrRemove("Glowing", b)),
                new IntegerEntryModel(this, ModTexts.FIRE, getData().getShort("Fire"), s -> getData().putShort("Fire", s.shortValue()))
        );
    }

    private MutableComponent getCustomName() {
        String s = getData().getString("CustomName");
        if (s.isEmpty()) {
            return null;
        }
        Component component = Component.Serializer.fromJson(s, ClientUtil.registryAccess());
        return component == null ? null : component.copy();
    }

    private void setCustomName(MutableComponent value) {
        if (value != null && !value.getString().isEmpty()) {
            getData().putString("CustomName", Component.Serializer.toJson(value, ClientUtil.registryAccess()));
        } else if (getData().getString("CustomName").isEmpty()) {
            getData().remove("CustomName");
        } else {
            getData().putString("CustomName", "");
        }
    }

    private void putBooleanOrRemove(String tagName, boolean value) {
        if (value) {
            getData().putBoolean(tagName, true);
        } else {
            getData().remove(tagName);
        }
    }

    private void setHealth(float health) {
        getData().putFloat(HEALTH_TAG, health);
        updateMaxHealthAttribute(health);
    }

    private void updateMaxHealthAttribute(float health) {
        CompoundTag data = getData();
        ListTag attributes = data.getList(ATTRIBUTES_TAG, Tag.TAG_COMPOUND);

        CompoundTag maxHealthAttribute = null;
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompound(i);
            if (MAX_HEALTH_ATTRIBUTE_ID.equals(attribute.getString(ATTRIBUTE_ID_TAG))) {
                maxHealthAttribute = attribute;
                break;
            }
        }

        if (maxHealthAttribute == null) {
            maxHealthAttribute = new CompoundTag();
            maxHealthAttribute.putString(ATTRIBUTE_ID_TAG, MAX_HEALTH_ATTRIBUTE_ID);
            attributes.add(maxHealthAttribute);
        }

        maxHealthAttribute.putDouble(ATTRIBUTE_BASE_TAG, health);
        data.put(ATTRIBUTES_TAG, attributes);
    }
}
