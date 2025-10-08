package com.github.rinorsi.cadeditor.fabric;

import com.github.rinorsi.cadeditor.common.CommonInit;
import com.github.rinorsi.cadeditor.common.ServerCommandHandler;
import com.github.rinorsi.cadeditor.common.ServerEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class FabricCADEditorMod implements ModInitializer {
    public static final String MOD_ID = "cadeditor";

    @Override
    public void onInitialize() {
        CommonInit.init();
        CommonInit.setup();
        com.github.rinorsi.cadeditor.fabric.loot.FabricLootTableTracker.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ServerCommandHandler.registerCommand(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            ServerEventHandler.onPlayerJoin(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerEventHandler.onPlayerLeave(handler.player));
    }
}
