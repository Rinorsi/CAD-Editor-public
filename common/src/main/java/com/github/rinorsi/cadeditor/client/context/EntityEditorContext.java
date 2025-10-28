package com.github.rinorsi.cadeditor.client.context;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

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
        return EntityType.create(tag, Minecraft.getInstance().level).orElse(null);
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
}
