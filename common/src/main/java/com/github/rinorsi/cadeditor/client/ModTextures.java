package com.github.rinorsi.cadeditor.client;


import net.minecraft.resources.Identifier;
import com.github.rinorsi.cadeditor.common.ModConstants;

public class ModTextures {
    public static final Identifier ADD = gui("add");
    public static final Identifier BYTE_ARRAY_TAG = gui("byte_array_tag");
    public static final Identifier BYTE_ARRAY_TAG_ADD = gui("byte_array_tag_add");
    public static final Identifier BYTE_TAG = gui("byte_tag");
    public static final Identifier BYTE_TAG_ADD = gui("byte_tag_add");
    public static final Identifier COLLAPSE = gui("collapse_all");
    public static final Identifier COLOR_AQUA = gui("color_aqua");
    public static final Identifier COLOR_BLACK = gui("color_black");
    public static final Identifier COLOR_BLUE = gui("color_blue");
    public static final Identifier COLOR_CUSTOM = gui("color_custom");
    public static final Identifier COLOR_DARK_AQUA = gui("color_dark_aqua");
    public static final Identifier COLOR_DARK_BLUE = gui("color_dark_blue");
    public static final Identifier COLOR_DARK_GRAY = gui("color_dark_gray");
    public static final Identifier COLOR_DARK_GREEN = gui("color_dark_green");
    public static final Identifier COLOR_DARK_PURPLE = gui("color_dark_purple");
    public static final Identifier COLOR_DARK_RED = gui("color_dark_red");
    public static final Identifier COLOR_GOLD = gui("color_gold");
    public static final Identifier COLOR_GRAY = gui("color_gray");
    public static final Identifier COLOR_GREEN = gui("color_green");
    public static final Identifier COLOR_LIGHT_PURPLE = gui("color_light_purple");
    public static final Identifier COLOR_RED = gui("color_red");
    public static final Identifier COLOR_WHITE = gui("color_white");
    public static final Identifier COLOR_YELLOW = gui("color_yellow");
    public static final Identifier COMPOUND_TAG = gui("compound_tag");
    public static final Identifier COMPOUND_TAG_ADD = gui("compound_tag_add");
    public static final Identifier COPY = gui("copy");
    public static final Identifier CUT = gui("cut");
    public static final Identifier DOUBLE_TAG = gui("double_tag");
    public static final Identifier DOUBLE_TAG_ADD = gui("double_tag_add");
    public static final Identifier EXPAND = gui("expand_all");
    public static final Identifier FLOAT_TAG = gui("float_tag");
    public static final Identifier FLOAT_TAG_ADD = gui("float_tag_add");
    public static final Identifier INT_ARRAY_TAG = gui("int_array_tag");
    public static final Identifier INT_ARRAY_TAG_ADD = gui("int_array_tag_add");
    public static final Identifier INT_TAG = gui("int_tag");
    public static final Identifier INT_TAG_ADD = gui("int_tag_add");
    public static final Identifier LEVEL_ADD = gui("level_add");
    public static final Identifier LEVEL_REMOVE = gui("level_remove");
    public static final Identifier LIST_TAG = gui("list_tag");
    public static final Identifier LIST_TAG_ADD = gui("list_tag_add");
    public static final Identifier UPDATE_LOG = LIST_TAG_ADD;
    public static final Identifier LONG_ARRAY_TAG = gui("long_array_tag");
    public static final Identifier LONG_ARRAY_TAG_ADD = gui("long_array_tag_add");
    public static final Identifier LONG_TAG = gui("long_tag");
    public static final Identifier LONG_TAG_ADD = gui("long_tag_add");
    public static final Identifier MOVE_DOWN = gui("move_down");
    public static final Identifier MOVE_UP = gui("move_up");
    public static final Identifier PASTE = gui("paste");
    public static final Identifier REMOVE = gui("remove");
    public static final Identifier RESET = gui("reset");
    public static final Identifier RESET_COLOR = gui("reset_color");
    public static final Identifier SCROLL_FOCUSED = gui("scroll_focused");
    public static final Identifier SEARCH = gui("search");
    public static final Identifier SETTINGS = gui("settings");
    public static final Identifier SHORT_TAG = gui("short_tag");
    public static final Identifier SHORT_TAG_ADD = gui("short_tag_add");
    public static final Identifier STRING_TAG = gui("string_tag");
    public static final Identifier STRING_TAG_ADD = gui("string_tag_add");
    public static final Identifier TEXT_BOLD = gui("text_bold");
    public static final Identifier TEXT_ITALIC = gui("text_italic");
    public static final Identifier TEXT_OBFUSCATED = gui("text_obfuscated");
    public static final Identifier TEXT_STRIKETHROUGH = gui("text_strikethrough");
    public static final Identifier TEXT_UNDERLINED = gui("text_underline");
    public static final Identifier ZOOM_IN = gui("zoom_in");
    public static final Identifier ZOOM_OUT = gui("zoom_out");
    public static final Identifier ZOOM_RESET = gui("zoom_reset");
    public static final Identifier SAVE = gui("save");
    public static final Identifier EDITOR = gui("editor");
    public static final Identifier NBT_EDITOR = gui("nbt_editor");
    public static final Identifier SNBT_EDITOR = gui("snbt_editor");
    public static final Identifier COPY_COMMAND = gui("copy_command");
    public static final Identifier FORMAT = gui("format");

    public static Identifier gui(String textureName) {
        return Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, String.format("textures/gui/%s.png", textureName));
    }
}
