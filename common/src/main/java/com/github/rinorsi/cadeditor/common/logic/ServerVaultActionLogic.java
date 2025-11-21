package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.network.GiveVaultItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public class ServerVaultActionLogic {
    public static void onGiveVaultItem(ServerPlayer player, GiveVaultItemPacket response) {
        int slot = Math.max(0, Math.min(Inventory.INVENTORY_SIZE - 1, response.slot()));
        player.getInventory().setItem(slot, response.itemStack());
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        CommonUtil.showVaultItemGiveSuccess(player);
    }
}
