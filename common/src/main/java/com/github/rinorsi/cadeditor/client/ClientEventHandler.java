package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.client.logic.ClientEditorRequestLogic;
import com.github.rinorsi.cadeditor.client.logic.ClientVaultActionLogic;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.mixin.CreativeModeInventoryScreenMixin;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void onKeyInput() {
        try {
            if (KeyBindings.getEditorKey().consumeClick()) {
                ClientEditorRequestLogic.requestWorldEditor(EditorType.STANDARD);
            } else if (KeyBindings.getNBTEditorKey().consumeClick()) {
                ClientEditorRequestLogic.requestWorldEditor(EditorType.NBT);
            } else if (KeyBindings.getSNBTEditorKey().consumeClick()) {
                ClientEditorRequestLogic.requestWorldEditor(EditorType.SNBT);
            } else if (KeyBindings.getVaultKey().consumeClick()) {
                ModScreenHandler.openVault();
            }
        } catch (Exception e) {
            ClientUtil.showMessage(ModTexts.Messages.ERROR_GENERIC);
            LOGGER.error("处理游戏内按键时出错（CAD Editor）", e);
        }
    }

    public static boolean onScreenEvent(AbstractContainerScreen<?> screen, int keyCode, int scanCode, int modifiers) {
        try {
            KeyEvent keyEvent = new KeyEvent(keyCode, scanCode, modifiers);
            if (KeyBindings.getEditorKey().matches(keyEvent)) {
                return ClientEditorRequestLogic.requestInventoryItemEditor(EditorType.STANDARD, screen);
            } else if (KeyBindings.getNBTEditorKey().matches(keyEvent)) {
                return ClientEditorRequestLogic.requestInventoryItemEditor(EditorType.NBT, screen);
            } else if (KeyBindings.getSNBTEditorKey().matches(keyEvent)) {
                return ClientEditorRequestLogic.requestInventoryItemEditor(EditorType.SNBT, screen);
            } else if (KeyBindings.getVaultKey().matches(keyEvent)) {
                if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                    CreativeModeTab.Type type = ((CreativeModeInventoryScreenMixin) creativeScreen).getSelectedTab().getType();
                    if (type == CreativeModeTab.Type.SEARCH) return false;
                }
                if (!ClientVaultActionLogic.openVaultSelection(screen)) {
                    ModScreenHandler.openVault();
                }
                return true;
            }
        } catch (Exception e) {
            ClientUtil.showMessage(ModTexts.Messages.ERROR_GENERIC);
            LOGGER.error("处理界面按键时出错（CAD Editor）", e);
        }
        return false;
    }
}
