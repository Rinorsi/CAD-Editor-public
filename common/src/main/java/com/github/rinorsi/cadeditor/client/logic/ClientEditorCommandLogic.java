package com.github.rinorsi.cadeditor.client.logic;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.network.EditorCommandPacket;

public final class ClientEditorCommandLogic {
    public static void onEditorCommand(EditorCommandPacket command) {
        DebugLog.infoKey("cadeditor.debug.command.received", command.target(), command.editorType());
        switch (command.target()) {
            case WORLD -> ClientEditorRequestLogic.requestWorldEditor(command.editorType());
            case ITEM -> {
                if (!ClientEditorRequestLogic.requestMainHandItemEditor(command.editorType())) {
                    ClientUtil.showMessage(ModTexts.Messages.errorNoTargetFound(ModTexts.ITEM));
                    DebugLog.infoKey("cadeditor.debug.command.mainhand.empty");
                }
            }
            case BLOCK -> {
                if (!ClientEditorRequestLogic.requestBlockEditor(command.editorType())) {
                    ClientUtil.showMessage(ModTexts.Messages.errorNoTargetFound(ModTexts.BLOCK));
                    DebugLog.infoKey("cadeditor.debug.command.block.missing");
                }
            }
            case ENTITY -> {
                if (!ClientEditorRequestLogic.requestEntityEditor(command.editorType())) {
                    ClientUtil.showMessage(ModTexts.Messages.errorNoTargetFound(ModTexts.ENTITY));
                    DebugLog.infoKey("cadeditor.debug.command.entity.missing");
                }
            }
            case SELF -> ClientEditorRequestLogic.requestSelfEditor(command.editorType());
        }
    }
}
