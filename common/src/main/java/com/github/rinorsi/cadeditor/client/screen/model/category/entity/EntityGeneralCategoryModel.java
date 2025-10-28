package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ReadOnlyStringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

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
        getEntries().add(new FloatEntryModel(this, ModTexts.HEALTH, getData().getFloat(HEALTH_TAG), this::setHealth));
        getEntries().add(new TextEntryModel(this, ModTexts.CUSTOM_NAME, getCustomName(), this::setCustomName));

        String playerUuid = resolvePlayerUuid();
        if (playerUuid != null) {
            getEntries().add(new ReadOnlyStringEntryModel(this, ModTexts.gui("uuid"), playerUuid));
        }

        getEntries().add(new BooleanEntryModel(this, ModTexts.ALWAYS_SHOW_NAME, getData().getBoolean("CustomNameVisible"), b -> putBooleanOrRemove("CustomNameVisible", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.INVULNERABLE, getData().getBoolean("Invulnerable"), b -> getData().putBoolean("Invulnerable", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.SILENT, getData().getBoolean("Silent"), b -> putBooleanOrRemove("Silent", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.NO_GRAVITY, getData().getBoolean("NoGravity"), b -> putBooleanOrRemove("NoGravity", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.GLOWING, getData().getBoolean("Glowing"), b -> putBooleanOrRemove("Glowing", b)));
        getEntries().add(new IntegerEntryModel(this, ModTexts.FIRE, getData().getShort("Fire"), s -> getData().putShort("Fire", s.shortValue())));
    }

    private String resolvePlayerUuid() {
        if (getEntity() instanceof Player player) {
            return player.getUUID().toString();
        }
        CompoundTag data = getData();
        if (data != null) {
            if (data.hasUUID("UUID")) {
                return data.getUUID("UUID").toString();
            }
            if (data.contains("UUIDMost", Tag.TAG_LONG) && data.contains("UUIDLeast", Tag.TAG_LONG)) {
                return new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")).toString();
            }
            if (data.contains("components", Tag.TAG_COMPOUND)) {
                CompoundTag components = data.getCompound("components");
                if (components.contains("minecraft:profile", Tag.TAG_COMPOUND)) {
                    CompoundTag profile = components.getCompound("minecraft:profile");
                    if (profile.contains("id", Tag.TAG_STRING)) {
                        String uuid = profile.getString("id");
                        if (!uuid.isBlank()) {
                            return uuid;
                        }
                    }
                }
            }
            String id = data.getString("id");
            if (("minecraft:player".equals(id) || "player".equals(id)) && Minecraft.getInstance().player != null) {
                return Minecraft.getInstance().player.getUUID().toString();
            }
        }
        return null;
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
