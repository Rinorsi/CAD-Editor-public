package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ReadOnlyStringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.client.util.ComponentJsonHelper;
import com.github.rinorsi.cadeditor.client.util.NbtUuidHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
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
        float health = getData().getFloatOr(HEALTH_TAG, 0f);
        getEntries().add(new FloatEntryModel(this, ModTexts.HEALTH, health, this::setHealth));
        getEntries().add(new TextEntryModel(this, ModTexts.CUSTOM_NAME, getCustomName(), this::setCustomName));

        String playerUuid = resolvePlayerUuid();
        if (playerUuid != null) {
            getEntries().add(new ReadOnlyStringEntryModel(this, ModTexts.gui("uuid"), playerUuid));
        }

        getEntries().add(new BooleanEntryModel(this, ModTexts.ALWAYS_SHOW_NAME, getData().getBooleanOr("CustomNameVisible", false), b -> putBooleanOrRemove("CustomNameVisible", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.INVULNERABLE, getData().getBooleanOr("Invulnerable", false), b -> getData().putBoolean("Invulnerable", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.SILENT, getData().getBooleanOr("Silent", false), b -> putBooleanOrRemove("Silent", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.NO_GRAVITY, getData().getBooleanOr("NoGravity", false), b -> putBooleanOrRemove("NoGravity", b)));
        getEntries().add(new BooleanEntryModel(this, ModTexts.GLOWING, getData().getBooleanOr("Glowing", false), b -> putBooleanOrRemove("Glowing", b)));
        short fire = getData().getShortOr("Fire", (short) 0);
        getEntries().add(new IntegerEntryModel(this, ModTexts.FIRE, (int) fire, s -> getData().putShort("Fire", s.shortValue())));
    }

    private String resolvePlayerUuid() {
        if (getEntity() instanceof Player player) {
            return player.getUUID().toString();
        }
        CompoundTag data = getData();
        if (data != null) {
            UUID uuid = NbtUuidHelper.getUuid(data, "UUID");
            if (uuid != null) {
                return uuid.toString();
            }
            if (data.getLong("UUIDMost").isPresent() && data.getLong("UUIDLeast").isPresent()) {
                long most = data.getLongOr("UUIDMost", 0L);
                long least = data.getLongOr("UUIDLeast", 0L);
                return new UUID(most, least).toString();
            }
            if (data.contains("components")) {
                CompoundTag components = data.getCompound("components").orElse(null);
                if (components != null) {
                    CompoundTag profile = components.getCompound("minecraft:profile").orElse(null);
                    if (profile != null) {
                        String uuidString = profile.getString("id").orElse("");
                        if (!uuidString.isBlank()) {
                            return uuidString;
                        }
                    }
                }
            }
            String id = data.getString("id").orElse("");
            if (("minecraft:player".equals(id) || "player".equals(id)) && Minecraft.getInstance().player != null) {
                return Minecraft.getInstance().player.getUUID().toString();
            }
        }
        return null;
    }

    private MutableComponent getCustomName() {
        Tag encoded = getData().get("CustomName");
        return ComponentJsonHelper.decode(encoded, ClientUtil.registryAccess());
    }

    private void setCustomName(MutableComponent value) {
        if (value != null && !value.getString().isEmpty()) {
            Tag encoded = ComponentJsonHelper.encodeToTag(value, ClientUtil.registryAccess());
            if (encoded != null) {
                getData().put("CustomName", encoded);
            }
        } else {
            getData().remove("CustomName");
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
        ListTag attributes = data.getList(ATTRIBUTES_TAG).orElseGet(ListTag::new);

        CompoundTag maxHealthAttribute = null;
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompound(i).orElse(null);
            if (attribute == null) continue;
            if (MAX_HEALTH_ATTRIBUTE_ID.equals(attribute.getString(ATTRIBUTE_ID_TAG).orElse(""))) {
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
