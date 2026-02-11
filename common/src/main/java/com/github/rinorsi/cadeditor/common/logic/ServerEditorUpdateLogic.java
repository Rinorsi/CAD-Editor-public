package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class ServerEditorUpdateLogic {
    private static final Component VERIFICATION_FAILED = Component.literal("Update verification failed; inventory re-synced. Please check the item.");
    private static final Logger LOGGER = LogManager.getLogger();

    //TODO 后面要梳理多端同步和缓存失效策略，顺便把指令落地的安全网织好
    private ServerEditorUpdateLogic() {
    }
    public static void onMainHandItemEditorUpdate(ServerPlayer player, MainHandItemEditorPacket.Update response) {
        ItemStack normalizedStack = normalize(response.getItemStack());
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.errorPermissionDenied(ModTexts.ITEM));
            return;
        }
        try {
            int hotbarIdx = player.getInventory().selected;
            player.getInventory().setItem(hotbarIdx, normalizedStack.copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, normalizedStack.copy());

            player.getInventory().setChanged();
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }

            syncMainHand(player);

            queueMainHandVerification(player, normalizedStack);
        } catch (Exception e) {
            LOGGER.error("Failed to apply main hand item update for {}", player.getGameProfile().getName(), e);
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.Messages.ERROR_GENERIC);
        }
    }

    public static void onPlayerInventoryItemEditorUpdate(ServerPlayer player, PlayerInventoryItemEditorPacket.Update response) {
        ItemStack normalizedStack = normalize(response.getItemStack());
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.errorPermissionDenied(ModTexts.ITEM));
            return;
        }
        try {
            player.getInventory().setItem(response.getSlot(), normalizedStack.copy());
            player.getInventory().setChanged();
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }

            syncInventorySlot(player, response.getSlot());

            queueInventoryVerification(player, response.getSlot(), normalizedStack);
        } catch (Exception e) {
            LOGGER.error("Failed to apply inventory item update for {} (slot {})", player.getGameProfile().getName(), response.getSlot(), e);
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.Messages.ERROR_GENERIC);
        }
    }

    public static void onBlockInventoryItemEditorUpdate(ServerPlayer player, BlockInventoryItemEditorPacket.Update response) {
        ItemStack normalizedStack = normalize(response.getItemStack());
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.errorPermissionDenied(ModTexts.ITEM));
            return;
        }
        var level = player.level();
        var pos = response.getBlockPos();
        var state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof Container container) {
            try {
                container.setItem(response.getSlot(), normalizedStack.copy());
                container.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                CommonUtil.showItemUpdateSuccess(player, normalizedStack);
                if (player.containerMenu != null) {
                    player.containerMenu.broadcastChanges();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to update block inventory at {} (slot {}) for {}", pos, response.getSlot(), player.getGameProfile().getName(), e);
                CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.Messages.ERROR_GENERIC);
            }
        } else {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, Component.translatable("cadeditor.message.no_target_found", ModTexts.ITEM));
        }
    }

    public static void onEntityInventoryItemEditorUpdate(ServerPlayer player, EntityInventoryItemEditorPacket.Update response) {
        ItemStack normalizedStack = normalize(response.getItemStack());
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.errorPermissionDenied(ModTexts.ITEM));
            return;
        }
        var level = player.level();
        if (level.getEntity(response.getEntityId()) instanceof Container container) {
            try {
                container.setItem(response.getSlot(), normalizedStack.copy());
                container.setChanged();
                CommonUtil.showItemUpdateSuccess(player, normalizedStack);
                if (player.containerMenu != null) {
                    player.containerMenu.broadcastChanges();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to update entity inventory for entity {} (slot {})", response.getEntityId(), response.getSlot(), e);
                CommonUtil.showItemUpdateFailure(player, normalizedStack, ModTexts.Messages.ERROR_GENERIC);
            }
        } else {
            CommonUtil.showItemUpdateFailure(player, normalizedStack, Component.translatable("cadeditor.message.no_target_found", ModTexts.ITEM));
        }
    }

    public static void onBlockEditorUpdate(ServerPlayer player, BlockEditorPacket.Update update) {
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showPermissionError(player, ModTexts.BLOCK);
            return;
        }
        var level = player.level();
        var pos = update.getBlockPos();
        var oldState = level.getBlockState(pos);
        try {
            int flags = selectBlockUpdateFlags(oldState, update.getBlockState());
            level.setBlock(pos, update.getBlockState(), flags);
            var currentState = level.getBlockState(pos);
            if (update.getTag() != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) {
                    CommonUtil.showTargetError(player, ModTexts.BLOCK);
                    return;
                }
                blockEntity.loadWithComponents(update.getTag(), player.registryAccess());
                blockEntity.setChanged();
            }
            level.sendBlockUpdated(pos, oldState, currentState, Block.UPDATE_CLIENTS);
            CommonUtil.showUpdateSuccess(player, ModTexts.BLOCK);
        } catch (Exception e) {
            LOGGER.error("Failed to update block at {} for {}", pos, player.getGameProfile().getName(), e);
            CommonUtil.showMessage(player, ModTexts.Messages.ERROR_GENERIC);
        }
    }

    public static void onEntityEditorUpdate(ServerPlayer player, EntityEditorPacket.Update update) {
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showPermissionError(player, ModTexts.ENTITY);
            return;
        }
        var level = player.level();
        var entity = level.getEntity(update.getEntityId());
        if (entity != null) {
            try {
                CompoundTag appliedTag = update.getTag().copy();
                entity.load(appliedTag);
                if (entity instanceof LivingEntity livingEntity) {
                    applyRequestedAttributes(livingEntity, appliedTag);
                    applyRequestedHealth(livingEntity, appliedTag);
                }
                if (entity instanceof ServerPlayer targetPlayer) {
                    applyRequestedPlayerAbilities(targetPlayer, appliedTag);
                }
                if (entity.level() instanceof ServerLevel serverLevel) {
                    reloadPassengers(serverLevel, entity, appliedTag);
                    serverLevel.getChunkSource().broadcastAndSend(entity,
                            new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().getNonDefaultValues()));
                    if (entity instanceof LivingEntity livingEntity) {
                        serverLevel.getChunkSource().broadcastAndSend(entity,
                                new ClientboundUpdateAttributesPacket(entity.getId(), livingEntity.getAttributes().getSyncableAttributes()));
                    }
                }
                if (entity instanceof ServerPlayer targetPlayer) {
                    targetPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().getNonDefaultValues()));
                    targetPlayer.connection.send(new ClientboundSetHealthPacket(
                            targetPlayer.getHealth(),
                            targetPlayer.getFoodData().getFoodLevel(),
                            targetPlayer.getFoodData().getSaturationLevel()
                    ));
                }
                CommonUtil.showUpdateSuccess(player, ModTexts.ENTITY);
            } catch (Exception e) {
                LOGGER.error("Failed to update entity {} for {}", update.getEntityId(), player.getGameProfile().getName(), e);
                CommonUtil.showMessage(player, ModTexts.Messages.ERROR_GENERIC);
            }
        } else {
            CommonUtil.showTargetError(player, ModTexts.ENTITY);
        }
    }

    private static void reloadPassengers(ServerLevel level, Entity entity, CompoundTag sourceTag) {
        clearExistingPassengers(entity);
        if (!sourceTag.contains("Passengers", Tag.TAG_LIST)) {
            return;
        }
        ListTag passengers = sourceTag.getList("Passengers", Tag.TAG_COMPOUND);
        for (Tag tag : passengers) {
            if (tag instanceof CompoundTag passengerTag) {
                spawnPassengerRecursive(level, entity, passengerTag);
            }
        }
    }

    private static void clearExistingPassengers(Entity entity) {
        for (Entity passenger : List.copyOf(entity.getPassengers())) {
            passenger.stopRiding();
            if (!(passenger instanceof ServerPlayer)) {
                passenger.discard();
            }
        }
        entity.ejectPassengers();
    }

    private static void spawnPassengerRecursive(ServerLevel level, Entity vehicle, CompoundTag passengerData) {
        CompoundTag passengerTag = passengerData.copy();
        ListTag nested = passengerTag.contains("Passengers", Tag.TAG_LIST)
                ? passengerTag.getList("Passengers", Tag.TAG_COMPOUND)
                : null;
        passengerTag.remove("Passengers");
        var passenger = EntityType.create(passengerTag, level).orElse(null);
        if (passenger == null) {
            return;
        }
        if (!passengerData.contains("Pos", Tag.TAG_LIST)) {
            passenger.moveTo(vehicle.getX(), vehicle.getY(), vehicle.getZ(),
                    passenger.getYRot(), passenger.getXRot());
        }
        level.addFreshEntity(passenger);
        if (!passenger.startRiding(vehicle, true)) {
            passenger.discard();
            return;
        }
        if (nested != null) {
            for (Tag nestedTag : nested) {
                if (nestedTag instanceof CompoundTag nestedCompound) {
                    spawnPassengerRecursive(level, passenger, nestedCompound);
                }
            }
        }
    }
    private static int toMenuSlotIndex(int invIndex) {
        return (invIndex >= 0 && invIndex < 9) ? 36 + invIndex : invIndex;
    }

    private static void syncMainHand(ServerPlayer player) {
        var menu = (player.containerMenu != null) ? player.containerMenu : player.inventoryMenu;
        int stateId = menu.incrementStateId();
        int hotbarIdx = player.getInventory().selected;
        int menuSlot = 36 + hotbarIdx;

        player.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, stateId, menuSlot, player.getMainHandItem().copy()));

        player.connection.send(new ClientboundSetEquipmentPacket(
                player.getId(),
                List.of(Pair.of(EquipmentSlot.MAINHAND, player.getMainHandItem().copy()))
        ));
    }

    private static void syncInventorySlot(ServerPlayer player, int invIndex) {
        var menu = (player.containerMenu != null) ? player.containerMenu : player.inventoryMenu;
        int stateId = menu.incrementStateId();
        int menuSlot = toMenuSlotIndex(invIndex);

        player.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, stateId, menuSlot, player.getInventory().getItem(invIndex).copy()));
    }
    private static void forceInventorySync(ServerPlayer player) {
        var menu = (player.containerMenu != null) ? player.containerMenu : player.inventoryMenu;
        int stateId = menu.incrementStateId();

        player.connection.send(new ClientboundContainerSetContentPacket(
                menu.containerId,
                stateId,
                menu.getItems(),
                menu.getCarried()));

        player.connection.send(new ClientboundSetEquipmentPacket(
                player.getId(),
                List.of(
                        Pair.of(EquipmentSlot.MAINHAND, player.getMainHandItem().copy()),
                        Pair.of(EquipmentSlot.OFFHAND, player.getOffhandItem().copy())
                )));
    }
    private static void queueMainHandVerification(ServerPlayer player, ItemStack expected) {
        ItemStack expectedCopy = expected.copy();
        scheduleVerification(player, () -> {
            ItemStack current = player.getMainHandItem();
            if (areStacksEquivalent(current, expectedCopy)) {
                CommonUtil.showItemUpdateSuccess(player, expectedCopy);
            } else {
                logStackDiff("MainHand", current, expectedCopy, player);
                forceInventorySync(player);
                CommonUtil.showItemUpdateFailure(player, expectedCopy, VERIFICATION_FAILED);
            }
        });
    }

    private static void queueInventoryVerification(ServerPlayer player, int slot, ItemStack expected) {
        int size = player.getInventory().getContainerSize();
        if (slot < 0 || slot >= size) {
            CommonUtil.showItemUpdateFailure(player, expected, Component.literal("Invalid slot: " + slot));
            return;
        }
        ItemStack expectedCopy = expected.copy();
        scheduleVerification(player, () -> {
            ItemStack current = player.getInventory().getItem(slot);
            if (areStacksEquivalent(current, expectedCopy)) {
                CommonUtil.showItemUpdateSuccess(player, expectedCopy);
            } else {
                logStackDiff("InventorySlot", current, expectedCopy, player);
                forceInventorySync(player);
                CommonUtil.showItemUpdateFailure(player, expectedCopy, VERIFICATION_FAILED);
            }
        });
    }

    private static void scheduleVerification(ServerPlayer player, Runnable action) {
        var server = player.serverLevel().getServer();
        server.execute(() -> server.execute(action));
    }

    private static void logStackDiff(String where, ItemStack actual, ItemStack expected, ServerPlayer player) {
        try {
            var ops = NbtOps.INSTANCE;
            var actualData = ItemStack.CODEC.encodeStart(ops, actual).result().orElse(null);
            var expectedData = ItemStack.CODEC.encodeStart(ops, expected).result().orElse(null);
            LOGGER.warn("[{}] stack mismatch for {}. Actual={}, Expected={}", where, player.getGameProfile().getName(), actualData, expectedData);
        } catch (Exception ex) {
            LOGGER.warn("Failed to diff stacks for {}", player.getGameProfile().getName(), ex);
        }
    }

    private static ItemStack normalize(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var ops = NbtOps.INSTANCE;
        return ItemStack.CODEC.encodeStart(ops, input).result()
                .flatMap(data -> ItemStack.CODEC.parse(ops, data).result())
                .orElse(input.copy());
    }

    private static boolean areStacksEquivalent(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }

    private static int selectBlockUpdateFlags(BlockState oldState, BlockState newState) {
        // Keep edited state values (e.g. grass_block[snowy=true]) from immediate neighbor recomputation
        // when only properties changed and block type stayed the same.
        if (oldState.getBlock() == newState.getBlock()) {
            return Block.UPDATE_CLIENTS;
        }
        return Block.UPDATE_ALL;
    }

    private static void applyRequestedAttributes(LivingEntity livingEntity, CompoundTag updateTag) {
        if (updateTag == null || updateTag.isEmpty()) {
            return;
        }
        ListTag attributes = null;
        if (updateTag.contains("attributes", Tag.TAG_LIST)) {
            attributes = updateTag.getList("attributes", Tag.TAG_COMPOUND);
        } else if (updateTag.contains("Attributes", Tag.TAG_LIST)) {
            attributes = updateTag.getList("Attributes", Tag.TAG_COMPOUND);
        }
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        var lookupOpt = livingEntity.registryAccess().lookup(Registries.ATTRIBUTE);
        if (lookupOpt.isEmpty()) {
            return;
        }
        HolderLookup.RegistryLookup<Attribute> lookup = lookupOpt.get();
        for (Tag element : attributes) {
            if (!(element instanceof CompoundTag attributeTag)) {
                continue;
            }
            String id = readAttributeId(attributeTag);
            if (id.isEmpty()) {
                continue;
            }
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null) {
                continue;
            }
            double baseValue = readAttributeBase(attributeTag);
            if (!Double.isFinite(baseValue)) {
                continue;
            }
            ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, location);
            var holderOpt = lookup.get(key);
            if (holderOpt.isEmpty()) {
                continue;
            }
            AttributeInstance instance = livingEntity.getAttribute(holderOpt.get());
            if (instance != null) {
                instance.setBaseValue(baseValue);
            }
        }
    }

    private static String readAttributeId(CompoundTag attributeTag) {
        String id = attributeTag.contains("id", Tag.TAG_STRING) ? attributeTag.getString("id") : "";
        if (id.isBlank()) {
            id = attributeTag.contains("Name", Tag.TAG_STRING) ? attributeTag.getString("Name") : "";
        }
        if (id.startsWith("minecraft:generic.")) {
            return "minecraft:" + id.substring("minecraft:generic.".length());
        }
        if (id.startsWith("generic.")) {
            return "minecraft:" + id.substring("generic.".length());
        }
        return id;
    }

    private static double readAttributeBase(CompoundTag attributeTag) {
        if (attributeTag.contains("base", Tag.TAG_DOUBLE) || attributeTag.contains("base", Tag.TAG_FLOAT) || attributeTag.contains("base", Tag.TAG_INT)) {
            return attributeTag.getDouble("base");
        }
        if (attributeTag.contains("Base", Tag.TAG_DOUBLE) || attributeTag.contains("Base", Tag.TAG_FLOAT) || attributeTag.contains("Base", Tag.TAG_INT)) {
            return attributeTag.getDouble("Base");
        }
        return Double.NaN;
    }

    private static void applyRequestedHealth(LivingEntity livingEntity, CompoundTag updateTag) {
        if (updateTag == null || !updateTag.contains("Health")) {
            return;
        }
        float requestedHealth = updateTag.getFloat("Health");
        float sanitized = requestedHealth <= 0f ? 0.0001f : requestedHealth;
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(sanitized);
        }
        livingEntity.setHealth(Math.min(sanitized, livingEntity.getMaxHealth()));
    }

    private static void applyRequestedPlayerAbilities(ServerPlayer player, CompoundTag updateTag) {
        if (updateTag == null || updateTag.isEmpty()) {
            return;
        }
        CompoundTag requested = null;
        if (updateTag.contains("abilities", Tag.TAG_COMPOUND)) {
            requested = updateTag.getCompound("abilities");
        } else if (updateTag.contains("Abilities", Tag.TAG_COMPOUND)) {
            requested = updateTag.getCompound("Abilities");
        }
        if (requested == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        if (requested.contains("invulnerable")) {
            abilities.invulnerable = requested.getBoolean("invulnerable");
        }
        if (requested.contains("mayfly")) {
            abilities.mayfly = requested.getBoolean("mayfly");
        }
        if (requested.contains("flying")) {
            abilities.flying = requested.getBoolean("flying") && abilities.mayfly;
        } else if (!abilities.mayfly) {
            abilities.flying = false;
        }
        if (requested.contains("mayBuild")) {
            abilities.mayBuild = requested.getBoolean("mayBuild");
        }
        if (requested.contains("instabuild")) {
            abilities.instabuild = requested.getBoolean("instabuild");
        }
        if (requested.contains("walkSpeed")) {
            abilities.setWalkingSpeed(clampAbilitySpeed(requested.getFloat("walkSpeed")));
        }
        if (requested.contains("flySpeed")) {
            abilities.setFlyingSpeed(clampAbilitySpeed(requested.getFloat("flySpeed")));
        }
        player.onUpdateAbilities();
    }

    private static float clampAbilitySpeed(float value) {
        return Math.max(0f, Math.min(value, 1f));
    }
}
