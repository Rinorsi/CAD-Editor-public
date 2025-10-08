package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.context.BlockEditorContext;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.network.*;

public class ClientEditorUpdateLogic {
    public static void updateMainHandItem(MainHandItemEditorPacket.Response response, ItemEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.MAIN_HAND_ITEM_EDITOR_UPDATE, new MainHandItemEditorPacket.Update(response, context.getItemStack().copy()));
        DebugLog.infoKey("cadeditor.debug.update.mainhand", response.getEditorType());
    }

    public static void updatePlayerInventoryItem(PlayerInventoryItemEditorPacket.Response response, ItemEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.PLAYER_INVENTORY_ITEM_EDITOR_UPDATE, new PlayerInventoryItemEditorPacket.Update(response, context.getItemStack().copy()));
        DebugLog.infoKey("cadeditor.debug.update.player_inventory", response.getSlot());
    }

    public static void updateBlockInventoryItem(BlockInventoryItemEditorPacket.Response response, ItemEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.BLOCK_INVENTORY_ITEM_EDITOR_UPDATE, new BlockInventoryItemEditorPacket.Update(response, context.getItemStack().copy()));
        DebugLog.infoKey("cadeditor.debug.update.block_inventory", response.getSlot());
    }

    public static void updateEntityInventoryItem(EntityInventoryItemEditorPacket.Response response, ItemEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.ENTITY_INVENTORY_ITEM_EDITOR_UPDATE, new EntityInventoryItemEditorPacket.Update(response, context.getItemStack().copy()));
        DebugLog.infoKey("cadeditor.debug.update.entity_inventory", response.getSlot());
    }

    public static void updateBlock(BlockEditorPacket.Response response, BlockEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.BLOCK_EDITOR_UPDATE, new BlockEditorPacket.Update(response, context.getBlockState(), context.getTag()));
        DebugLog.infoKey("cadeditor.debug.update.block", response.getBlockPos());
    }

    public static void updateEntity(EntityEditorPacket.Response response, EntityEditorContext context) {
        NetworkManager.sendToServer(NetworkManager.ENTITY_EDITOR_UPDATE, new EntityEditorPacket.Update(response, context.getTag()));
        DebugLog.infoKey("cadeditor.debug.update.entity");
    }
}