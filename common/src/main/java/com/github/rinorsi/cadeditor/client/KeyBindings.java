package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.common.ModConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "cadeditor"));
    private static final KeyMapping editorKey = new KeyMapping("cadeditor.key.editor", GLFW.GLFW_KEY_I, CATEGORY);
    private static final KeyMapping nbtEditorKey = new KeyMapping("cadeditor.key.nbt_editor", GLFW.GLFW_KEY_N, CATEGORY);
    private static final KeyMapping snbtEditorKey = new KeyMapping("cadeditor.key.snbt_editor", GLFW.GLFW_KEY_R, CATEGORY);
    private static final KeyMapping vaultKey = new KeyMapping("cadeditor.key.vault", GLFW.GLFW_KEY_J, CATEGORY);

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
