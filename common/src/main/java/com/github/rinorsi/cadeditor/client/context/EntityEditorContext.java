package com.github.rinorsi.cadeditor.client.context;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.List;
import java.util.function.Consumer;

public class EntityEditorContext extends EditorContext<EntityEditorContext> {
    private Entity entity;

    public EntityEditorContext(CompoundTag tag, Component errorTooltip, boolean canSaveToVault, Consumer<EntityEditorContext> action) {
        super(tag, errorTooltip, canSaveToVault, action);
        entity = createEntity(tag);
        if (entity == null) {
            this.canSaveToVault = false;
        }
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public void saveToVault() {
        Vault.getInstance().saveEntity(getTag());
        ClientUtil.showMessage(ModTexts.Messages.successSavedVault(ModTexts.ENTITY));
    }

    @Override
    public MutableComponent getTargetName() {
        return ModTexts.ENTITY;
    }

    @Override
    public String getCommandName() {
        return "/summon";
    }

    @Override
    protected String getCommand() {
        return String.format("/summon %s ~ ~ ~ %s", getTag().getString("id"), getSimpleTag());
    }

    @Override
    public List<String> getStringSuggestions(List<String> path) {
        List<String> suggestions = super.getStringSuggestions(path);
        if (!suggestions.isEmpty()) {
            return suggestions;
        }
        if (path == null || path.isEmpty()) {
            return List.of();
        }
        String key = lastKey(path);
        if (key == null) {
            return List.of();
        }
        String parent = previousNamedKey(path, 1);
        String grandParent = previousNamedKey(path, 2);

        if ("id".equals(key)) {
            if (path.size() == 1) {
                return ClientCache.getEntitySuggestions();
            }
            if (isItemSlot(parent, grandParent)) {
                return ClientCache.getItemSuggestions();
            }
            if (isEffectContainer(parent, grandParent)) {
                return ClientCache.getEffectSuggestions();
            }
        }

        if (equalsAnyIgnoreCase(key, "item", "Item")) {
            return ClientCache.getItemSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "potion")) {
            return ClientCache.getPotionSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "effect")) {
            return ClientCache.getEffectSuggestions();
        }

        return List.of();
    }

    private static boolean isItemSlot(String parent, String grandParent) {
        return equalsAnyIgnoreCase(parent, "HandItems", "ArmorItems", "Items", "Inventory", "item", "Item", "SaddleItem")
                || (equalsAnyIgnoreCase(parent, "stack") && equalsAnyIgnoreCase(grandParent, "minecraft:equipment"));
    }

    private static boolean isEffectContainer(String parent, String grandParent) {
        return equalsAnyIgnoreCase(parent, "ActiveEffects", "effects")
                || equalsAnyIgnoreCase(grandParent, "ActiveEffects");
    }

    private CompoundTag getSimpleTag() {
        CompoundTag tag = getTag().copy();
        tag.remove("UUID");
        tag.remove("Pos");
        tag.remove("Rotation");
        return tag;
    }

    private Entity createEntity(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        CompoundTag copy = tag.copy();
        clearIdentityData(copy);
        ensureDefaultPositionData(copy);
        ensureDefaultRotationData(copy);
        ensureDefaultMotionData(copy);
        Entity entity = EntityType.create(copy, level, EntitySpawnReason.COMMAND).orElse(null);
        if (entity == null) {
            EntityType<?> fallbackType = EntityType.byString(copy.getString("id")).orElse(null);
            if (fallbackType != null) {
                entity = fallbackType.create(level, EntitySpawnReason.COMMAND);
            }
        }
        if (entity != null) {
            entity.load(copy);
        }
        return entity;
    }

    public boolean replaceEntity(CompoundTag tag) {
        Entity newEntity = createEntity(tag);
        if (newEntity == null) {
            return false;
        }
        setTag(tag);
        entity = newEntity;
        return true;
    }

    private static void clearIdentityData(CompoundTag tag) {
        tag.remove("UUID");
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
        tag.remove("OwnerUUID");
        tag.remove("OwnerUUIDMost");
        tag.remove("OwnerUUIDLeast");
    }

    private static void ensureDefaultPositionData(CompoundTag tag) {
        if (!tag.contains("Pos", Tag.TAG_LIST) || tag.getList("Pos", Tag.TAG_DOUBLE).size() != 3) {
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(0d));
            pos.add(DoubleTag.valueOf(0d));
            pos.add(DoubleTag.valueOf(0d));
            tag.put("Pos", pos);
        }
    }

    private static void ensureDefaultRotationData(CompoundTag tag) {
        if (!tag.contains("Rotation", Tag.TAG_LIST) || tag.getList("Rotation", Tag.TAG_FLOAT).size() != 2) {
            ListTag rot = new ListTag();
            rot.add(FloatTag.valueOf(0f));
            rot.add(FloatTag.valueOf(0f));
            tag.put("Rotation", rot);
        }
    }

    private static void ensureDefaultMotionData(CompoundTag tag) {
        if (!tag.contains("Motion", Tag.TAG_LIST) || tag.getList("Motion", Tag.TAG_DOUBLE).size() != 3) {
            ListTag motion = new ListTag();
            motion.add(DoubleTag.valueOf(0d));
            motion.add(DoubleTag.valueOf(0d));
            motion.add(DoubleTag.valueOf(0d));
            tag.put("Motion", motion);
        }
    }
}
