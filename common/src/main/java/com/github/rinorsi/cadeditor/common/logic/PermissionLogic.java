package com.github.rinorsi.cadeditor.common.logic;

import com.github.rinorsi.cadeditor.common.CommonConfiguration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class PermissionLogic {
    public static boolean hasPermission(ServerPlayer player) {
        return player != null
                && player.permissions().hasPermission(commandPermission(CommonConfiguration.INSTANCE.getPermissionLevel()))
                && (!CommonConfiguration.INSTANCE.isCreativeOnly() || player.isCreative());
    }

    public static boolean hasPermission(CommandSourceStack source) {
        return source != null
                && source.permissions().hasPermission(commandPermission(CommonConfiguration.INSTANCE.getPermissionLevel()));
    }

    private static Permission commandPermission(int level) {
        int clamped = Math.max(0, Math.min(4, level));
        return new Permission.HasCommandLevel(PermissionLevel.byId(clamped));
    }
}
