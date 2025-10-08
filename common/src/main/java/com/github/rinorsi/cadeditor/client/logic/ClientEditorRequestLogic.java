package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.BlockEditorContext;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.*;
import com.github.rinorsi.cadeditor.mixin.AbstractContainerScreenMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ClientEditorRequestLogic {
    public static void requestWorldEditor(EditorType editorType) {
        DebugLog.infoKey("cadeditor.debug.request.world.start", editorType);
        if (!(requestEntityEditor(editorType) || requestBlockEditor(editorType) || requestMainHandItemEditor(editorType))) {
            DebugLog.infoKey("cadeditor.debug.request.world.fallback", editorType);
            requestSelfEditor(editorType);
        }
    }

    public static boolean requestEntityEditor(EditorType editorType) {
        DebugLog.infoKey("cadeditor.debug.request.entity.start", editorType);
        if (Minecraft.getInstance().hitResult instanceof EntityHitResult res) {
            var entity = res.getEntity();
            if (ClientContext.isModInstalledOnServer()) {
                DebugLog.infoKey("cadeditor.debug.request.entity.server", entity.getId());
                NetworkManager.sendToServer(NetworkManager.ENTITY_EDITOR_REQUEST, new EntityEditorPacket.Request(editorType, entity.getId()));
            } else {
                var tag = new CompoundTag();
                entity.save(tag);
                DebugLog.infoKey("cadeditor.debug.request.entity.local", entity.getName().getString());
                ModScreenHandler.openEditor(editorType, new EntityEditorContext(tag, ModTexts.errorServerModRequired(ModTexts.ENTITY), true, null));
            }
            return true;
        }
        DebugLog.infoKey("cadeditor.debug.request.entity.missing");
        return false;
    }

    public static boolean requestBlockEditor(EditorType editorType) {
        DebugLog.infoKey("cadeditor.debug.request.block.start", editorType);
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult res && res.getType() != HitResult.Type.MISS) {
            var blockPos = res.getBlockPos();
            if (ClientContext.isModInstalledOnServer()) {
                DebugLog.infoKey("cadeditor.debug.request.block.server", blockPos);
                NetworkManager.sendToServer(NetworkManager.BLOCK_EDITOR_REQUEST, new BlockEditorPacket.Request(editorType, blockPos));
            } else {
                var level = Minecraft.getInstance().level;
                var blockState = level.getBlockState(blockPos);
                var blockEntity = level.getBlockEntity(blockPos);
                CompoundTag tag = null;
                if (blockEntity != null) {
                    tag = blockEntity.saveWithId(ClientUtil.registryAccess());
                }
                DebugLog.infoKey("cadeditor.debug.request.block.local", blockState.getBlock().getName().getString());
                ModScreenHandler.openEditor(editorType, new BlockEditorContext(blockState, tag, ModTexts.errorServerModRequired(ModTexts.BLOCK), null));
            }
            return true;
        }
        DebugLog.infoKey("cadeditor.debug.request.block.missing");
        return false;
    }

    public static boolean requestMainHandItemEditor(EditorType editorType) {
        DebugLog.infoKey("cadeditor.debug.request.mainhand.start", editorType);
        var item = Minecraft.getInstance().player.getMainHandItem();
        if (item.isEmpty()) {
            DebugLog.infoKey("cadeditor.debug.request.mainhand.empty");
            return false;
        }
        if (ClientContext.isModInstalledOnServer()) {
            DebugLog.infoKey("cadeditor.debug.request.mainhand.server", item.getDisplayName().getString());
            NetworkManager.sendToServer(NetworkManager.MAIN_HAND_ITEM_EDITOR_REQUEST, new MainHandItemEditorPacket.Request(editorType));
        } else {
            if (Minecraft.getInstance().player.isCreative()) {
                DebugLog.infoKey("cadeditor.debug.request.mainhand.local_creative", item.getDisplayName().getString());
                ModScreenHandler.openEditor(editorType, new ItemEditorContext(item, null, true, context ->
                        Minecraft.getInstance().player.connection.send(new ServerboundSetCreativeModeSlotPacket(Minecraft.getInstance().player.getInventory().selected + Inventory.INVENTORY_SIZE, context.getItemStack().copy()))));
            } else {
                DebugLog.infoKey("cadeditor.debug.request.mainhand.local", item.getDisplayName().getString());
                ModScreenHandler.openEditor(editorType, new ItemEditorContext(item, ModTexts.errorServerModRequired(ModTexts.ITEM), true, null));
            }
        }
        return true;
    }

    public static void requestSelfEditor(EditorType editorType) {
        var entity = Minecraft.getInstance().player;
        if (ClientContext.isModInstalledOnServer()) {
            DebugLog.infoKey("cadeditor.debug.request.self.server", entity.getGameProfile().getName());
            NetworkManager.sendToServer(NetworkManager.ENTITY_EDITOR_REQUEST, new EntityEditorPacket.Request(editorType, entity.getId()));
        } else {
            DebugLog.infoKey("cadeditor.debug.request.self.missing");
        }
    }

    public static boolean requestInventoryItemEditor(EditorType editorType, AbstractContainerScreen<?> screen) {
        var slot = ((AbstractContainerScreenMixin) screen).getHoveredSlot();
        if (slot != null && slot.hasItem()) {
            int slotIndex = slot.getContainerSlot();
            boolean creativeInventoryScreen = false;
            if (slot.container instanceof Inventory) {
                if (screen instanceof CreativeModeInventoryScreen creative) {
                    creativeInventoryScreen = true;
                    if (creative.isInventoryOpen()) {
                        slotIndex = ClientUtil.convertCreativeInventorySlot(slotIndex);
                    }
                }
                final int finalSlot = slotIndex;
                final boolean finalCreative = creativeInventoryScreen;
                DebugLog.infoKey("cadeditor.debug.request.inventory.start", finalSlot, editorType);
                if (ClientContext.isModInstalledOnServer()) {
                    DebugLog.infoKey("cadeditor.debug.request.inventory.server", finalSlot, finalCreative);
                    NetworkManager.sendToServer(NetworkManager.PLAYER_INVENTORY_ITEM_EDITOR_REQUEST, new PlayerInventoryItemEditorPacket.Request(editorType, finalSlot, finalCreative));
                } else {
                    if (Minecraft.getInstance().player.isCreative()) {
                        DebugLog.infoKey("cadeditor.debug.request.inventory.local_creative", finalSlot);
                        ModScreenHandler.openEditor(editorType, new ItemEditorContext(slot.getItem(), null, true, context -> slot.set(context.getItemStack().copy())));
                    } else {
                        DebugLog.infoKey("cadeditor.debug.request.inventory.local", finalSlot);
                        ModScreenHandler.openEditor(editorType, new ItemEditorContext(slot.getItem(), ModTexts.errorServerModRequired(ModTexts.ITEM), true, null));
                    }
                }
                return true;
            } else if (Minecraft.getInstance().hitResult instanceof BlockHitResult res && Minecraft.getInstance().level.getBlockEntity(res.getBlockPos()) instanceof Container) {
                final int finalSlot = slotIndex;
                if (ClientContext.isModInstalledOnServer()) {
                    DebugLog.infoKey("cadeditor.debug.request.inventory.block", res.getBlockPos(), finalSlot);
                    NetworkManager.sendToServer(NetworkManager.BLOCK_INVENTORY_ITEM_EDITOR_REQUEST, new BlockInventoryItemEditorPacket.Request(editorType, finalSlot, res.getBlockPos()));
                }
                return true;
            } else if (Minecraft.getInstance().hitResult instanceof EntityHitResult res && res.getEntity() instanceof Container) {
                final int finalSlot = slotIndex;
                if (ClientContext.isModInstalledOnServer()) {
                    DebugLog.infoKey("cadeditor.debug.request.inventory.entity", res.getEntity().getId(), finalSlot);
                    NetworkManager.sendToServer(NetworkManager.ENTITY_INVENTORY_ITEM_EDITOR_REQUEST, new EntityInventoryItemEditorPacket.Request(editorType, finalSlot, res.getEntity().getId()));
                }
                return true;
            }
        }
        DebugLog.infoKey("cadeditor.debug.request.inventory.missing");
        return false;
    }
}