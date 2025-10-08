package com.github.rinorsi.cadeditor.neoforge;

import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientEventHandler;
import com.github.rinorsi.cadeditor.client.ClientInit;
import com.github.rinorsi.cadeditor.client.KeyBindings;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.common.CommonInit;
import com.github.rinorsi.cadeditor.common.ServerCommandHandler;
import com.github.rinorsi.cadeditor.common.ServerEventHandler;
import com.github.rinorsi.cadeditor.neoforge.loot.ForgeLootTableTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ForgeCADEditorMod.MOD_ID)
public final class ForgeCADEditorMod {

    public static final String MOD_ID = "cadeditor";

    public ForgeCADEditorMod(IEventBus modBus, ModContainer container) {
        CommonInit.init();
        ForgeLootTableTracker.register();
        modBus.addListener(this::onCommonInit);
        modBus.addListener(PlatformUtilImpl::registerPayloadHandlers);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init();
            modBus.addListener(this::onClientInit);
            modBus.addListener(this::onRegisterKeybindings);

            container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, previous) -> {
                    ModScreenHandler.openSettingsScreen();
                    return Minecraft.getInstance().screen;
                }
            );

            NeoForge.EVENT_BUS.addListener(this::onKeyInput);
            NeoForge.EVENT_BUS.addListener(this::onKeyPressed);
            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggingIn);
            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggingOut);
        }
    }

    private void onCommonInit(final FMLCommonSetupEvent event) {
        CommonInit.setup();
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
    }

    private void onClientInit(final FMLClientSetupEvent event) {
        ClientInit.setup();
    }

    private void onRegisterKeybindings(final RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.getEditorKey());
        event.register(KeyBindings.getNBTEditorKey());
        event.register(KeyBindings.getSNBTEditorKey());
        event.register(KeyBindings.getVaultKey());
    }

    private void onServerStarting(final ServerStartingEvent event) {
        ServerCommandHandler.registerCommand(event.getServer().getCommands().getDispatcher());
    }

    private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerEventHandler.onPlayerJoin(serverPlayer);
        }
    }

    private void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerEventHandler.onPlayerLeave(serverPlayer);
        }
    }

    private void onPlayerLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        ClientContext.setModInstalledOnServer(false);
    }

    private void onPlayerLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        ClientContext.setModInstalledOnServer(false);
    }

    private void onKeyInput(final InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null) {
            ClientEventHandler.onKeyInput();
        }
    }

    private void onKeyPressed(final ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            event.setCanceled(ClientEventHandler.onScreenEvent(screen, event.getKeyCode(), event.getScanCode()));
        }
    }
}
