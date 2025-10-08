package com.github.rinorsi.cadeditor.fabric;

import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientEventHandler;
import com.github.rinorsi.cadeditor.client.ClientInit;
import com.github.rinorsi.cadeditor.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class FabricCADEditorModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientInit.init();
        ClientInit.setup();

        KeyBindingHelper.registerKeyBinding(KeyBindings.getEditorKey());
        KeyBindingHelper.registerKeyBinding(KeyBindings.getNBTEditorKey());
        KeyBindingHelper.registerKeyBinding(KeyBindings.getSNBTEditorKey());
        KeyBindingHelper.registerKeyBinding(KeyBindings.getVaultKey());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.screen == null) {
                ClientEventHandler.onKeyInput();
            }
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> ClientContext.setModInstalledOnServer(false));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientContext.setModInstalledOnServer(false));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                registerContainerScreenKeyHandler(screen));
    }

    private void registerContainerScreenKeyHandler(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> container) {
            ScreenKeyboardEvents.allowKeyPress(screen).register((current, key, scancode, modifiers) ->
                    !ClientEventHandler.onScreenEvent(container, key, scancode));
        }
    }
}
