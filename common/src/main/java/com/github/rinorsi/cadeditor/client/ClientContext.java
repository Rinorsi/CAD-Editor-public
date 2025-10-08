package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.common.network.ModNotificationPacket;
import com.github.rinorsi.cadeditor.common.network.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientContext {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean modInstalledOnServer;

    public static boolean isModInstalledOnServer() {
        return modInstalledOnServer;
    }

    public static void setModInstalledOnServer(boolean value) {
        LOGGER.debug("Setting 'modInstalledOnServer' to {}", value);
        modInstalledOnServer = value;
    }

    public static void onServerNotification(ModNotificationPacket.Server packet) {
        setModInstalledOnServer(true);
        NetworkManager.sendToServer(NetworkManager.CLIENT_NOTIFICATION, ModNotificationPacket.Client.INSTANCE);
    }
}
