package com.github.rinorsi.cadeditor.common;

import com.github.rinorsi.cadeditor.common.network.ModNotificationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ServerContext {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<UUID> moddedClients = new HashSet<>();

    public static void removeModdedClient(ServerPlayer player) {
        LOGGER.debug("Removing {} from modded clients", player.getName().getString());
        moddedClients.remove(player.getUUID());
    }

    public static void addModdedClient(ServerPlayer player, ModNotificationPacket.Client packet) {
        LOGGER.debug("Adding {} to modded clients", player.getName().getString());
        moddedClients.add(player.getUUID());
    }

    public static boolean isClientModded(ServerPlayer player) {
        return moddedClients.contains(player.getUUID());
    }
}
