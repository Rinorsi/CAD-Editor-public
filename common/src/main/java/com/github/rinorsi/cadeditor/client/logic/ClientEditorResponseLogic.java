package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.BlockEditorContext;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ClientEditorResponseLogic {
    public static void onMainHandItemEditorResponse(MainHandItemEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.mainhand", response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new ItemEditorContext(response.getItemStack(),
                getErrorTooltip(response.hasPermission(), ModTexts.ITEM), true,
                context -> ClientEditorUpdateLogic.updateMainHandItem(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.ITEM);
    }

    public static void onPlayerInventoryItemEditorResponse(PlayerInventoryItemEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.player_inventory", response.getSlot(), response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new ItemEditorContext(response.getItemStack(),
                getErrorTooltip(response.hasPermission(), ModTexts.ITEM), true,
                context -> ClientEditorUpdateLogic.updatePlayerInventoryItem(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.ITEM);
    }

    public static void onBlockInventoryItemEditorResponse(BlockInventoryItemEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.block_inventory", response.getSlot(), response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new ItemEditorContext(response.getItemStack(),
                getErrorTooltip(response.hasPermission(), ModTexts.ITEM), true,
                context -> ClientEditorUpdateLogic.updateBlockInventoryItem(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.ITEM);
    }

    public static void onEntityInventoryItemEditorResponse(EntityInventoryItemEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.entity_inventory", response.getSlot(), response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new ItemEditorContext(response.getItemStack(),
                getErrorTooltip(response.hasPermission(), ModTexts.ITEM), true,
                context -> ClientEditorUpdateLogic.updateEntityInventoryItem(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.ITEM);
    }

    public static void onBlockEditorResponse(BlockEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.block", response.getBlockPos(), response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new BlockEditorContext(response.getBlockState(), response.getTag(),
                getErrorTooltip(response.hasPermission(), ModTexts.BLOCK),
                context -> ClientEditorUpdateLogic.updateBlock(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.BLOCK);
    }

    public static void onEntityEditorResponse(EntityEditorPacket.Response response) {
        DebugLog.infoKey("cadeditor.debug.response.entity", response.hasPermission());
        ModScreenHandler.openEditor(response.getEditorType(), new EntityEditorContext(response.getTag(),
                getErrorTooltip(response.hasPermission(), ModTexts.ENTITY), true,
                context -> ClientEditorUpdateLogic.updateEntity(response, context)));
        notifyPermission(response.hasPermission(), ModTexts.ENTITY);
    }

    private static Component getErrorTooltip(boolean hasPermission, MutableComponent arg) {
        return hasPermission ? null : ModTexts.errorPermissionDenied(arg);
    }

    private static void notifyPermission(boolean hasPermission, MutableComponent target) {
        if (!hasPermission) {
            ClientUtil.showMessage(ModTexts.Messages.errorPermissionDenied(target));
            DebugLog.infoKey("cadeditor.debug.response.permission_denied", target.getString());
        }
    }
}