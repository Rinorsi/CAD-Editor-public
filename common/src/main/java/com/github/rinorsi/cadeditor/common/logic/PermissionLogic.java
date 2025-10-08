package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonConfiguration;
import net.minecraft.server.level.ServerPlayer;

public class PermissionLogic {
    public static boolean hasPermission(ServerPlayer player) {
        return player.hasPermissions(CommonConfiguration.INSTANCE.getPermissionLevel()) && (!CommonConfiguration.INSTANCE.isCreativeOnly() || player.isCreative());
    }
}
