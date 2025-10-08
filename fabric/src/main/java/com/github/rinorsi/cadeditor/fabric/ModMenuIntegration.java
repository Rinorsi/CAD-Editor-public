package com.github.rinorsi.cadeditor.fabric;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModScreenHandler.openSettingsScreen();
            return Minecraft.getInstance().screen;
        };
    }
}
