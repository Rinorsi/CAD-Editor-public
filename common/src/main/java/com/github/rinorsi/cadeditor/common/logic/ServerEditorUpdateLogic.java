package com.github.rinorsi.cadeditor.common.logic;

import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
            int hotbarIdx = player.getInventory().getSelectedSlot();
            player.getInventory().setItem(hotbarIdx, normalizedStack.copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, normalizedStack.copy());

            player.getInventory().setChanged();
            if (player.containerMenu != null) {
                player.containerMenu.broadcastChanges();
            }

            syncMainHand(player);

            queueMainHandVerification(player, normalizedStack);
        } catch (Exception e) {
            LOGGER.error("Failed to apply main hand item update for {}", player.getName().getString(), e);
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
            LOGGER.error("Failed to apply inventory item update for {} (slot {})", player.getName().getString(), response.getSlot(), e);
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
                LOGGER.error("Failed to update block inventory at {} (slot {}) for {}", pos, response.getSlot(), player.getName().getString(), e);
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
        if (isInfoDebugEnabled()) {
            LOGGER.info("[CAD-Editor][BlockUpdate][incoming] player={} pos={} state={} {}",
                    player.getName().getString(),
                    pos,
                    update.getBlockState(),
                    summarizeBlockTag(update.getTag()));
        }
        try {
            level.setBlock(pos, update.getBlockState(), Block.UPDATE_ALL);
            var currentState = level.getBlockState(pos);
            if (update.getTag() != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) {
                    CommonUtil.showTargetError(player, ModTexts.BLOCK);
                    return;
                }
                var input = TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), update.getTag());
                blockEntity.loadWithComponents(input);
                blockEntity.setChanged();
                if (isInfoDebugEnabled()) {
                    CompoundTag appliedTag = saveBlockEntityTag(player, blockEntity);
                    LOGGER.info("[CAD-Editor][BlockUpdate][applied] player={} pos={} {}",
                            player.getName().getString(),
                            pos,
                            summarizeBlockTag(appliedTag));
                }
            }
            level.sendBlockUpdated(pos, oldState, currentState, Block.UPDATE_CLIENTS);
            CommonUtil.showUpdateSuccess(player, ModTexts.BLOCK);
        } catch (Exception e) {
            LOGGER.error("Failed to update block at {} for {}", pos, player.getName().getString(), e);
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
                boolean passengersDefined = false;
                ListTag requestedPassengers = null;
                if (update.getTag() != null) {
                    var passengersOpt = update.getTag().getList("Passengers");
                    if (passengersOpt.isPresent()) {
                        passengersDefined = true;
                        ListTag list = passengersOpt.get();
                        requestedPassengers = list.isEmpty() ? null : list.copy();
                    }
                }
                float requestedHealth = update.getTag() == null ? Float.NaN : update.getTag().getFloatOr("Health", Float.NaN);
                if (entity instanceof LivingEntity livingBefore) {
                    logLivingEntityState("before_load", livingBefore, requestedHealth);
                }
                var input = TagValueInput.create(ProblemReporter.DISCARDING, player.level().registryAccess(), update.getTag());
                entity.load(input);
                if (passengersDefined && entity.level() instanceof ServerLevel serverLevel) {
                    if (requestedPassengers != null) {
                        rebuildPassengers(serverLevel, entity, requestedPassengers);
                    } else {
                        clearPassengers(entity);
                    }
                }
                if (entity instanceof LivingEntity livingAfterLoad) {
                    applyRequestedAttributes(livingAfterLoad, update.getTag());
                    logLivingEntityState("after_load", livingAfterLoad, requestedHealth);
                    if (!Float.isNaN(requestedHealth)) {
                        applyRequestedHealth(livingAfterLoad, requestedHealth);
                        logLivingEntityState("after_applied", livingAfterLoad, requestedHealth);
                    }
                }
                ClientboundSetEntityDataPacket dataPacket =
                        new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().getNonDefaultValues());
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(entity, dataPacket);
                }
                if (entity instanceof ServerPlayer targetPlayer) {
                    targetPlayer.connection.send(dataPacket);
                }

                if (entity instanceof LivingEntity livingEntity) {
                    ClientboundUpdateAttributesPacket attributesPacket =
                            new ClientboundUpdateAttributesPacket(
                                    entity.getId(),
                                    livingEntity.getAttributes().getSyncableAttributes());
                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(entity, attributesPacket);
                    }
                    if (entity instanceof ServerPlayer targetPlayer) {
                        targetPlayer.connection.send(attributesPacket);
                        targetPlayer.connection.send(new ClientboundSetHealthPacket(
                                targetPlayer.getHealth(),
                                targetPlayer.getFoodData().getFoodLevel(),
                                targetPlayer.getFoodData().getSaturationLevel()
                        ));
                    }
                    logLivingEntityState("after_packets", livingEntity, requestedHealth);
                }
                CommonUtil.showUpdateSuccess(player, ModTexts.ENTITY);
            } catch (Exception e) {
                LOGGER.error("Failed to update entity {} for {}", update.getEntityId(), player.getName().getString(), e);
                CommonUtil.showMessage(player, ModTexts.Messages.ERROR_GENERIC);
            }
        } else {
            CommonUtil.showTargetError(player, ModTexts.ENTITY);
        }
    }
    private static int toMenuSlotIndex(int invIndex) {
        return (invIndex >= 0 && invIndex < 9) ? 36 + invIndex : invIndex;
    }

    private static void syncMainHand(ServerPlayer player) {
        var menu = (player.containerMenu != null) ? player.containerMenu : player.inventoryMenu;
        int stateId = menu.incrementStateId();
        int hotbarIdx = player.getInventory().getSelectedSlot();
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
        var server = player.level().getServer();
        server.execute(() -> server.execute(action));
    }

    private static void logStackDiff(String where, ItemStack actual, ItemStack expected, ServerPlayer player) {
        try {
            var ops = NbtOps.INSTANCE;
            var actualData = ItemStack.CODEC.encodeStart(ops, actual).result().orElse(null);
            var expectedData = ItemStack.CODEC.encodeStart(ops, expected).result().orElse(null);
            LOGGER.warn("[{}] stack mismatch for {}. Actual={}, Expected={}", where, player.getName().getString(), actualData, expectedData);
        } catch (Exception ex) {
            LOGGER.warn("Failed to diff stacks for {}", player.getName().getString(), ex);
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

    private static void logLivingEntityState(String stage, LivingEntity livingEntity, float requestedHealth) {
        if (!isFeatureDebugEnabled()) {
            return;
        }
        try {
            LOGGER.info("[CAD-Editor][EntityUpdate][{}] entity='{}' id={} uuid={} health={} max={} requestedHealth={}",
                    stage,
                    livingEntity.getName().getString(),
                    livingEntity.getId(),
                    livingEntity.getUUID(),
                    livingEntity.getHealth(),
                    livingEntity.getMaxHealth(),
                    requestedHealth);
        } catch (Exception ignored) {
        }
    }

    private static void applyRequestedHealth(LivingEntity livingEntity, float requestedHealth) {
        float sanitized = requestedHealth <= 0f ? 0.0001f : requestedHealth;
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(sanitized);
        }
        livingEntity.setHealth(Math.min(sanitized, livingEntity.getMaxHealth()));
    }

    private static void applyRequestedAttributes(LivingEntity livingEntity, CompoundTag updateTag) {
        if (updateTag == null) {
            return;
        }
        ListTag attributes = readRequestedAttributes(updateTag);
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
            applyRequestedAttribute(livingEntity, lookup, attributeTag);
        }
    }

    private static void applyRequestedAttribute(
            LivingEntity livingEntity,
            HolderLookup.RegistryLookup<Attribute> lookup,
            CompoundTag attributeTag
    ) {
        String idString = readAttributeId(attributeTag);
        if (idString.isEmpty()) {
            return;
        }
        Identifier id = Identifier.tryParse(idString);
        if (id == null) {
            return;
        }
        double baseValue = readAttributeBase(attributeTag);
        if (!Double.isFinite(baseValue)) {
            return;
        }
        ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, id);
        var holderOpt = lookup.get(key);
        if (holderOpt.isEmpty()) {
            return;
        }
        AttributeInstance instance = livingEntity.getAttribute(holderOpt.get());
        if (instance == null) {
            return;
        }
        instance.setBaseValue(baseValue);
    }

    private static ListTag readRequestedAttributes(CompoundTag root) {
        if (root.contains("attributes")) {
            return root.getList("attributes").orElse(null);
        }
        if (root.contains("Attributes")) {
            return root.getList("Attributes").orElse(null);
        }
        return null;
    }

    private static String readAttributeId(CompoundTag attributeTag) {
        String id = attributeTag.getStringOr("id", "");
        if (id.isBlank()) {
            id = attributeTag.getStringOr("Name", "");
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
        if (attributeTag.contains("base")) {
            return attributeTag.getDoubleOr("base", Double.NaN);
        }
        return attributeTag.getDoubleOr("Base", Double.NaN);
    }

    private static void rebuildPassengers(ServerLevel level, Entity root, ListTag passengers) {
        clearPassengers(root);
        if (passengers == null || passengers.isEmpty()) {
            return;
        }
        for (int i = 0; i < passengers.size(); i++) {
            if (!(passengers.get(i) instanceof CompoundTag passengerTag)) {
                continue;
            }
            Entity passenger = EntityType.loadEntityRecursive(passengerTag, level, EntitySpawnReason.COMMAND, entity -> {
                level.addFreshEntity(entity);
                return entity;
            });
            if (passenger != null) {
                passenger.startRiding(root, true, true);
            }
        }
    }

    private static void clearPassengers(Entity root) {
        if (root.getPassengers().isEmpty()) {
            return;
        }
        for (Entity passenger : List.copyOf(root.getPassengers())) {
            passenger.stopRiding();
            passenger.discard();
        }
    }

    private static CompoundTag saveBlockEntityTag(ServerPlayer player, BlockEntity blockEntity) {
        if (blockEntity == null) {
            return null;
        }
        try {
            TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            blockEntity.saveWithId(writer);
            return writer.buildResult();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String summarizeBlockTag(CompoundTag tag) {
        if (tag == null) {
            return "tag=null";
        }
        String blockEntityId = tag.getStringOr("id", "");
        CompoundTag spawnData = readCompound(tag, "spawn_data", "SpawnData");
        CompoundTag entityData = readCompound(spawnData, "entity", "Entity");
        String entityId = entityData.getStringOr("id", "");
        return "tagKeys=" + tag.size()
                + " blockEntityId=" + (blockEntityId.isEmpty() ? "-" : blockEntityId)
                + " spawnEntityId=" + (entityId.isEmpty() ? "-" : entityId)
                + " hasSpawnData=" + !spawnData.isEmpty();
    }

    private static CompoundTag readCompound(CompoundTag tag, String primary, String secondary) {
        if (tag == null) {
            return new CompoundTag();
        }
        if (tag.contains(primary)) {
            return tag.getCompound(primary).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        }
        if (tag.contains(secondary)) {
            return tag.getCompound(secondary).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        }
        return new CompoundTag();
    }

    private static boolean isInfoDebugEnabled() {
        try {
            if (ClientConfiguration.INSTANCE == null) {
                return false;
            }
            DebugMode mode = ClientConfiguration.INSTANCE.getGuapiDebugMode();
            return mode == DebugMode.INFO || mode == DebugMode.FEATURE;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isFeatureDebugEnabled() {
        try {
            return ClientConfiguration.INSTANCE != null
                    && ClientConfiguration.INSTANCE.getGuapiDebugMode() == DebugMode.FEATURE;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
