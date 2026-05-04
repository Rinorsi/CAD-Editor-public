package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.GiveVaultItemPacket;
import net.minecraft.server.level.ServerPlayer;

public class ServerVaultActionLogic {
    public static void onGiveVaultItem(ServerPlayer player, GiveVaultItemPacket response) {
        if (!PermissionLogic.hasPermission(player)) {
            CommonUtil.showPermissionError(player, ModTexts.VAULT);
            return;
        }
        int slot = response.slot();
        if (slot < 0 || slot >= player.getInventory().getContainerSize()) {
            CommonUtil.showMessage(player, ModTexts.Messages.ERROR_GENERIC);
            return;
        }

        player.getInventory().setItem(slot, response.itemStack());
        CommonUtil.showVaultItemGiveSuccess(player);
    }
}
