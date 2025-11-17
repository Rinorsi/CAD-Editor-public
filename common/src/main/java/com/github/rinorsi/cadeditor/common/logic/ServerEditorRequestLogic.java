package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.TagValueOutput;

public final class ServerEditorRequestLogic {
    public static void onMainHandItemEditorRequest(ServerPlayer player, MainHandItemEditorPacket.Request request) {
        NetworkManager.sendToClient(player, NetworkManager.MAIN_HAND_ITEM_EDITOR_RESPONSE, new MainHandItemEditorPacket.Response(request, PermissionLogic.hasPermission(player), player.getMainHandItem()));
    }

    public static void onPlayerInventoryItemEditorRequest(ServerPlayer player, PlayerInventoryItemEditorPacket.Request request) {
        NetworkManager.sendToClient(player, NetworkManager.PLAYER_INVENTORY_ITEM_EDITOR_RESPONSE, new PlayerInventoryItemEditorPacket.Response(request, PermissionLogic.hasPermission(player), player.getInventory().getItem(request.getSlot())));
    }

    public static void onBlockInventoryItemEditorRequest(ServerPlayer player, BlockInventoryItemEditorPacket.Request request) {
        if (player.level().getBlockEntity(request.getBlockPos()) instanceof Container container) {
            NetworkManager.sendToClient(player, NetworkManager.BLOCK_INVENTORY_ITEM_EDITOR_RESPONSE, new BlockInventoryItemEditorPacket.Response(request, PermissionLogic.hasPermission(player), container.getItem(request.getSlot())));
        } else {
            CommonUtil.showTargetError(player, ModTexts.ITEM);
        }
    }

    public static void onEntityInventoryItemEditorRequest(ServerPlayer player, EntityInventoryItemEditorPacket.Request request) {
        if (player.level().getEntity(request.getEntityId()) instanceof Container container) {
            NetworkManager.sendToClient(player, NetworkManager.ENTITY_INVENTORY_ITEM_EDITOR_RESPONSE, new EntityInventoryItemEditorPacket.Response(request, PermissionLogic.hasPermission(player), container.getItem(request.getSlot())));
        } else {
            CommonUtil.showTargetError(player, ModTexts.ITEM);
        }
    }

    public static void onBlockEditorRequest(ServerPlayer player, BlockEditorPacket.Request request) {
        var level = player.level();
        var blockState = level.getBlockState(request.getBlockPos());
        var blockEntity = level.getBlockEntity(request.getBlockPos());
        CompoundTag tag = null;
        if (blockEntity != null) {
            TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            blockEntity.saveWithId(writer);
            tag = writer.buildResult();
        }
        NetworkManager.sendToClient(player, NetworkManager.BLOCK_EDITOR_RESPONSE, new BlockEditorPacket.Response(request, PermissionLogic.hasPermission(player), blockState, tag));
    }

    public static void onEntityEditorRequest(ServerPlayer player, EntityEditorPacket.Request request) {
        var entity = player.level().getEntity(request.getEntityId());
        var tag = (net.minecraft.nbt.CompoundTag) null;
        if (entity != null) {
            TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            if (!entity.save(writer)) {
                writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
                entity.saveWithoutId(writer);
            }
            tag = writer.buildResult();
        }
        NetworkManager.sendToClient(player, NetworkManager.ENTITY_EDITOR_RESPONSE, new EntityEditorPacket.Response(request, PermissionLogic.hasPermission(player), tag));
    }
}
