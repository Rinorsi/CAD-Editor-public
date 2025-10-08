package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.network.GiveVaultItemPacket;
import net.minecraft.server.level.ServerPlayer;

public class ServerVaultActionLogic {
    public static void onGiveVaultItem(ServerPlayer player, GiveVaultItemPacket response) {
        player.getInventory().setItem(response.slot(), response.itemStack());
        CommonUtil.showVaultItemGiveSuccess(player);
    }
}
