package com.github.rinorsi.cadeditor.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    private static final KeyMapping editorKey = new KeyMapping("cadeditor.key.editor", GLFW.GLFW_KEY_I, "cadeditor");
    private static final KeyMapping nbtEditorKey = new KeyMapping("cadeditor.key.nbt_editor", GLFW.GLFW_KEY_N, "cadeditor");
    private static final KeyMapping snbtEditorKey = new KeyMapping("cadeditor.key.snbt_editor", GLFW.GLFW_KEY_R, "cadeditor");
    private static final KeyMapping vaultKey = new KeyMapping("cadeditor.key.vault", GLFW.GLFW_KEY_J, "cadeditor");

    public static KeyMapping getEditorKey() {
        return editorKey;
    }

    public static KeyMapping getNBTEditorKey() {
        return nbtEditorKey;
    }

    public static KeyMapping getSNBTEditorKey() {
        return snbtEditorKey;
    }

    public static KeyMapping getVaultKey() {
        return vaultKey;
    }
}
