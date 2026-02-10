package com.github.rinorsi.cadeditor.common;

import static com.github.franckyi.guapi.api.GuapiHelper.text;
import static com.github.franckyi.guapi.api.GuapiHelper.translated;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemUseAnimation;

public final class ModTexts {

    public static final MutableComponent ADD = translated("cadeditor.gui.add").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent AMBIENT = translated("cadeditor.gui.ambient");
    public static final MutableComponent AMOUNT = translated("cadeditor.gui.amount");
    public static final MutableComponent AMOUNT_NONFINITE_COMMAND_HINT = translated("cadeditor.gui.amount_nonfinite_command_hint").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent AMPLIFIER = translated("cadeditor.gui.amplifier");
    public static final MutableComponent AQUA = translated("cadeditor.gui.aqua").withStyle(ChatFormatting.AQUA);
    public static final MutableComponent ARMOR_COLOR = translated("cadeditor.gui.armor_color");
    public static final MutableComponent ATTRIBUTE = translated("cadeditor.gui.attribute");
    public static final MutableComponent ATTRIBUTE_MODIFIERS = translated("cadeditor.gui.attribute_modifiers");
    public static final MutableComponent ATTRIBUTE_NAME = translated("cadeditor.gui.attribute_name");
    public static final MutableComponent BLACK = translated("cadeditor.gui.black").withStyle(ChatFormatting.AQUA);
    public static final MutableComponent BLOCK = translated("cadeditor.text.block");
    public static final MutableComponent BLUE = translated("cadeditor.gui.blue").withStyle(ChatFormatting.BLUE);
    public static final MutableComponent BLUE_COLOR = translated("cadeditor.gui.blue");
    public static final MutableComponent BOLD = translated("cadeditor.gui.bold");
    public static final MutableComponent CANCEL = translated("gui.cancel").withStyle(ChatFormatting.RED);
    public static final MutableComponent CAN_DESTROY = translated("cadeditor.gui.can_destroy");
    public static final MutableComponent CAN_PLACE_ON = translated("cadeditor.gui.can_place_on");
    public static final MutableComponent CLIENT = translated("cadeditor.gui.client");
    public static final MutableComponent CLOSE = translated("cadeditor.gui.close_without_saving").withStyle(ChatFormatting.RED);
    public static final MutableComponent COLLAPSE = translated("cadeditor.gui.collapse");
    public static final MutableComponent COPY = translated("cadeditor.gui.copy");
    public static final MutableComponent COUNT = translated("cadeditor.gui.count");
    public static final MutableComponent CUSTOM_COLOR = translated("cadeditor.gui.custom_color");
    public static final MutableComponent CUSTOM_NAME = translated("cadeditor.gui.custom_name");
    public static final MutableComponent CUT = translated("cadeditor.gui.cut");
    public static final MutableComponent DAMAGE = translated("cadeditor.gui.damage");
    public static final MutableComponent DARK_AQUA = translated("cadeditor.gui.dark_aqua").withStyle(ChatFormatting.AQUA);
    public static final MutableComponent DARK_BLUE = translated("cadeditor.gui.dark_blue").withStyle(ChatFormatting.BLUE);
    public static final MutableComponent DARK_GRAY = translated("cadeditor.gui.dark_gray").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent DARK_GREEN = translated("cadeditor.gui.dark_green").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent DARK_PURPLE = translated("cadeditor.gui.dark_purple").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final MutableComponent DARK_RED = translated("cadeditor.gui.dark_red").withStyle(ChatFormatting.RED);
    public static final MutableComponent DEBUG_MODE = translated("cadeditor.gui.debug_mode");
    public static final MutableComponent DEFAULT_POTION = translated("cadeditor.gui.default_potion");
    public static final MutableComponent DISPLAY = translated("cadeditor.gui.display");
    public static final MutableComponent ITEM_FRAME = translated("cadeditor.gui.item_frame");
    public static final MutableComponent ITEM_FRAME_ITEM = translated("cadeditor.gui.item_frame_item");
    public static final MutableComponent ITEM_FRAME_DROP_CHANCE = translated("cadeditor.gui.item_frame_drop_chance");
    public static final MutableComponent ITEM_FRAME_ROTATION = translated("cadeditor.gui.item_frame_rotation");
    public static final MutableComponent ITEM_FRAME_FIXED = translated("cadeditor.gui.item_frame_fixed");
    public static final MutableComponent ITEM_FRAME_INVISIBLE = translated("cadeditor.gui.item_frame_invisible");
    public static final MutableComponent ITEM_FRAME_FACING = translated("cadeditor.gui.item_frame_facing");
    public static final MutableComponent DONE = translated("gui.done").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent DURATION = translated("cadeditor.gui.duration");
    public static final MutableComponent EFFECT = translated("cadeditor.gui.effect");
    public static final MutableComponent EFFECTS = translated("cadeditor.gui.effect");
    public static final MutableComponent ENCHANTMENT = translated("cadeditor.gui.enchantment");
    public static final MutableComponent ENCHANTMENTS = translated("cadeditor.gui.enchantments");
    public static final MutableComponent ENTITY = translated("cadeditor.text.entity");
    public static final MutableComponent ENTITY_ATTRIBUTES = translated("cadeditor.gui.entity_attributes");
    public static final MutableComponent ENTITY_EQUIPMENT = translated("cadeditor.gui.entity_equipment");
    public static final MutableComponent ENTITY_SPAWN = translated("cadeditor.gui.entity_spawn");
    public static final MutableComponent ENTITY_TAMING = translated("cadeditor.gui.entity_taming");
    public static final MutableComponent ENTITY_MOUNT = translated("cadeditor.gui.entity_mount");
    public static final MutableComponent TAME = translated("cadeditor.gui.tame");
    public static final MutableComponent OWNER_NAME = translated("cadeditor.gui.owner");
    public static final MutableComponent OWNER_UUID = translated("cadeditor.gui.owner_uuid");
    public static final MutableComponent SITTING = translated("cadeditor.gui.sitting");
    public static final MutableComponent IN_SITTING_POSE = translated("cadeditor.gui.in_sitting_pose");
    public static final MutableComponent SADDLED = translated("cadeditor.gui.saddled");
    public static final MutableComponent SADDLE_ITEM = translated("cadeditor.gui.saddle_item");
    public static final MutableComponent CHESTED_HORSE = translated("cadeditor.gui.chested_horse");
    public static final MutableComponent PASSENGERS = translated("cadeditor.gui.passengers");
    public static final MutableComponent LEASH_HOLDER = translated("cadeditor.gui.leash_holder");
    public static final MutableComponent LEASH_ANCHOR = translated("cadeditor.gui.leash_anchor");
    public static final MutableComponent LEASH_POS_X = translated("cadeditor.gui.leash_pos_x");
    public static final MutableComponent LEASH_POS_Y = translated("cadeditor.gui.leash_pos_y");
    public static final MutableComponent LEASH_POS_Z = translated("cadeditor.gui.leash_pos_z");
    public static final MutableComponent USE_SELF_UUID = translated("cadeditor.gui.use_self_uuid").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent MOUNT_TEMPER = translated("cadeditor.gui.mount_temper");
    public static final MutableComponent MOUNT_STRENGTH = translated("cadeditor.gui.mount_strength");
    public static final MutableComponent VILLAGER_DATA = translated("cadeditor.gui.villager_data");
    public static final MutableComponent VILLAGER_PROFESSION = translated("cadeditor.gui.villager_profession");
    public static final MutableComponent VILLAGER_TYPE = translated("cadeditor.gui.villager_type");
    public static final MutableComponent VILLAGER_LEVEL = translated("cadeditor.gui.villager_level");
    public static final MutableComponent VILLAGER_TRADES = translated("cadeditor.gui.villager_trades");
    public static final MutableComponent TRADE_INPUT_PRIMARY = translated("cadeditor.gui.trade_input_primary");
    public static final MutableComponent TRADE_INPUT_SECONDARY = translated("cadeditor.gui.trade_input_secondary");
    public static final MutableComponent TRADE_OUTPUT = translated("cadeditor.gui.trade_output");
    public static final MutableComponent TRADE_MAX_USES = translated("cadeditor.gui.trade_max_uses");
    public static final MutableComponent TRADE_USES = translated("cadeditor.gui.trade_uses");
    public static final MutableComponent TRADE_DEMAND = translated("cadeditor.gui.trade_demand");
    public static final MutableComponent TRADE_SPECIAL_PRICE = translated("cadeditor.gui.trade_special_price");
    public static final MutableComponent TRADE_PRICE_MULTIPLIER = translated("cadeditor.gui.trade_price_multiplier");
    public static final MutableComponent TRADE_REWARD_EXP = translated("cadeditor.gui.trade_reward_exp");
    public static final MutableComponent TRADE_XP = translated("cadeditor.gui.trade_xp");
    public static final MutableComponent TRADE_ADD = translated("cadeditor.gui.trade_add");
    public static final MutableComponent LOOT_TABLE = translated("cadeditor.gui.loot_table");
    public static final MutableComponent CONTAINER_LOOT = translated("cadeditor.gui.container_loot");
    public static final MutableComponent SEED = translated("cadeditor.gui.seed");
    public static final MutableComponent CONTAINER_GRID = translated("cadeditor.gui.container_grid");
    public static final MutableComponent EXPAND = translated("cadeditor.gui.expand");
    public static final MutableComponent FIX_ERRORS = translated("cadeditor.gui.fix_errors").withStyle(ChatFormatting.RED);
    public static final MutableComponent GENERAL = translated("cadeditor.gui.general");
    public static final MutableComponent GOLD = translated("cadeditor.gui.gold").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent GRAY = translated("cadeditor.gui.gray").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent GREEN = translated("cadeditor.gui.green").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent GREEN_COLOR = translated("cadeditor.gui.green");
    public static final MutableComponent HIDE_FLAGS = translated("cadeditor.gui.hide_flags");
    public static final MutableComponent FIRE_RESISTANT = translated("cadeditor.gui.fire_resistant");
    public static final MutableComponent INTANGIBLE_PROJECTILE = translated("cadeditor.gui.intangible_projectile");
    public static final MutableComponent MAX_STACK_SIZE = translated("cadeditor.gui.max_stack_size");
    public static final MutableComponent MAX_DAMAGE = translated("cadeditor.gui.max_damage");
    public static final MutableComponent[] HIDE_OTHER_TOOLTIP = arrayText("cadeditor.gui.hide_other_tooltip", 8);
    public static final MutableComponent ITALIC = translated("cadeditor.gui.italic");
    public static final MutableComponent ITEM = translated("cadeditor.text.item");
    public static final MutableComponent ITEM_NAME = translated("cadeditor.gui.item_name");
    public static final MutableComponent ITEM_ID = translated("cadeditor.gui.item_id");
    public static final MutableComponent LEVEL_ADD = translated("cadeditor.gui.level_add", text("+1")).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent LEVEL_REMOVE = translated("cadeditor.gui.level_add", text("-1")).withStyle(ChatFormatting.GREEN);
    public static final MutableComponent LIGHT_PURPLE = translated("cadeditor.gui.light_purple").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final MutableComponent LORE_ADD = translated("cadeditor.gui.lore_add");
    public static final MutableComponent MODIFIER = translated("cadeditor.gui.modifier");
    public static final MutableComponent MOVE_DOWN = translated("cadeditor.gui.move_down");
    public static final MutableComponent MOVE_UP = translated("cadeditor.gui.move_up");
    public static final MutableComponent OBFUSCATED = translated("cadeditor.gui.obfuscated");
    public static final MutableComponent PASTE = translated("cadeditor.gui.paste");
    public static final MutableComponent POTION = translated("cadeditor.gui.potion");
    public static final MutableComponent POTION_COLOR = translated("cadeditor.gui.potion_color");
    public static final MutableComponent POTION_EFFECTS = translated("cadeditor.gui.potion_effects");
    public static final MutableComponent RED = translated("cadeditor.gui.red").withStyle(ChatFormatting.RED);
    public static final MutableComponent RED_COLOR = translated("cadeditor.gui.red");
    public static final MutableComponent RELOAD_CONFIG = translated("cadeditor.gui.reload_config").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent REMOVE = translated("cadeditor.gui.remove").withStyle(ChatFormatting.RED);
    public static final MutableComponent REMOVE_CUSTOM_COLOR = translated("cadeditor.gui.remove_custom_color").withStyle(ChatFormatting.RED);
    public static final MutableComponent RESET = translated("cadeditor.gui.reset").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent RESET_COLOR = translated("cadeditor.gui.reset_color");
    public static final MutableComponent SAVE = translated("cadeditor.gui.save").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent SAVE_EDIT = translated("cadeditor.gui.save_edit").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent SCROLL_FOCUSED = translated("cadeditor.gui.scroll_focused");
    public static final MutableComponent SEARCH = translated("cadeditor.gui.search");
    public static final MutableComponent LOAD_ALL = translated("cadeditor.gui.load_all");
    public static final MutableComponent SHOWING_ALL = translated("cadeditor.gui.showing_all").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent SETTINGS = translated("cadeditor.gui.settings");
    public static final MutableComponent SECONDS = translated("cadeditor.gui.seconds");
    public static final MutableComponent UPDATE_LOG = translated("cadeditor.gui.update_log");
    public static final MutableComponent UPDATE_LOG_TITLE = title(translated("cadeditor.gui.update_log_title"));
    public static final MutableComponent UPDATE_LOG_RECENT = translated("cadeditor.gui.update_log_recent").withStyle(ChatFormatting.GOLD);
    public static final MutableComponent UPDATE_LOG_NEW = translated("cadeditor.gui.update_log_new").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
    public static final MutableComponent UPDATE_LOG_MARK_READ = translated("cadeditor.gui.update_log_mark_read");
    public static final MutableComponent UPDATE_LOG_MARK_ALL_READ = translated("cadeditor.gui.update_log_mark_all_read");
    public static final MutableComponent UPDATE_LOG_COPY = translated("cadeditor.gui.update_log_copy");
    public static final MutableComponent UPDATE_LOG_EMPTY = translated("cadeditor.gui.update_log_empty").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent UPDATE_LOG_NEW_TOOLTIP = translated("cadeditor.gui.update_log_new_tooltip").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent SHOW_ICON = translated("cadeditor.gui.show_icon");
    public static final MutableComponent SHOW_PARTICLES = translated("cadeditor.gui.show_particles");
    public static final MutableComponent SLOT = translated("cadeditor.gui.slot");
    public static final MutableComponent DROP_CHANCE = translated("cadeditor.gui.drop_chance");
    public static final MutableComponent CHOOSE_ITEM = translated("cadeditor.gui.choose_item");
    public static final MutableComponent MAIN_HAND = translated("cadeditor.gui.mainhand");
    public static final MutableComponent OFF_HAND = translated("cadeditor.gui.offhand");
    public static final MutableComponent HEAD = translated("cadeditor.gui.head");
    public static final MutableComponent CHEST = translated("cadeditor.gui.chest");
    public static final MutableComponent LEGS = translated("cadeditor.gui.legs");
    public static final MutableComponent FEET = translated("cadeditor.gui.feet");
    public static final MutableComponent STRIKETHROUGH = translated("cadeditor.gui.strikethrough");
    public static final MutableComponent TICKS = translated("cadeditor.gui.ticks");
    ;
    public static final MutableComponent THEME = translated("cadeditor.gui.theme");
    public static final MutableComponent SYNTAX_HIGHLIGHTING_PRESET = translated("cadeditor.gui.syntax_highlighting_preset");
    public static final MutableComponent UNBREAKABLE = translated("cadeditor.gui.unbreakable");
    public static final MutableComponent UNDERLINED = translated("cadeditor.gui.underline");
    public static final MutableComponent WHITE = translated("cadeditor.gui.white").withStyle(ChatFormatting.WHITE);
    public static final MutableComponent YELLOW = translated("cadeditor.gui.yellow").withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent ZOOM_IN = translated("cadeditor.gui.zoom_in");
    public static final MutableComponent ZOOM_OUT = translated("cadeditor.gui.zoom_out");
    public static final MutableComponent ZOOM_RESET = translated("cadeditor.gui.zoom_reset");
    public static final MutableComponent SELECTION_SCREEN_MAX_ITEMS = translated("cadeditor.gui.selection_screen_max_items");
    public static final MutableComponent ATTRIBUTE_TOOLTIP_INFO = translated("cadeditor.gui.attribute_tooltip_info").withStyle(ChatFormatting.GRAY);
    public static final MutableComponent LEVEL = translated("cadeditor.gui.level");
    public static final MutableComponent COMMON = translated("cadeditor.gui.common");
    public static final MutableComponent SNBT_PREVIEW = translated("cadeditor.gui.snbt_preview");
    public static final MutableComponent SNBT_PREVIEW_INVALID = translated("cadeditor.gui.snbt_preview_invalid").withStyle(ChatFormatting.RED);
    public static final MutableComponent SNBT_PREVIEW_TOGGLE = translated("cadeditor.gui.snbt_preview_toggle");
    public static final MutableComponent EQUIPMENT_ASSET = translated("cadeditor.gui.equipment_asset");
    public static final MutableComponent PERMISSION_LEVEL = translated("cadeditor.gui.permission_level");
    public static final MutableComponent CREATIVE_ONLY = translated("cadeditor.gui.creative_only");
    public static final MutableComponent BLOCK_STATE = translated("cadeditor.gui.block_state");
    public static final MutableComponent SPAWN_EGG = translated("cadeditor.gui.spawn_egg");
    public static final MutableComponent CONTAINER = translated("cadeditor.gui.container");
    public static final MutableComponent LOCK_CODE = translated("cadeditor.gui.lock_code");
    public static final MutableComponent VAULT = translated("cadeditor.text.vault");
    public static final MutableComponent HEALTH = translated("cadeditor.gui.health");
    public static final MutableComponent ALWAYS_SHOW_NAME = translated("cadeditor.gui.always_show_name");
    public static final MutableComponent INVULNERABLE = translated("cadeditor.gui.invulnerable");
    public static final MutableComponent SILENT = translated("cadeditor.gui.silent");
    public static final MutableComponent NO_GRAVITY = translated("cadeditor.gui.no_gravity");
    public static final MutableComponent GLOWING = translated("cadeditor.gui.glowing");
    public static final MutableComponent FIRE = translated("cadeditor.gui.fire");
    public static final MutableComponent SAVE_VAULT = translated("cadeditor.gui.save_vault");
    public static final MutableComponent LOAD_VAULT = translated("cadeditor.gui.load_vault");
    public static final MutableComponent CAN_PICK_UP_LOOT = translated("cadeditor.gui.can_pick_up_loot");
    public static final MutableComponent PERSISTENCE_REQUIRED = translated("cadeditor.gui.persistence_required");
    public static final MutableComponent NO_AI = translated("cadeditor.gui.no_ai");
    public static final MutableComponent LEFT_HANDED = translated("cadeditor.gui.left_handed");
    public static final MutableComponent TEAM = translated("cadeditor.gui.team");
    public static final MutableComponent SPAWN_REINFORCEMENTS_CHANCE = translated("cadeditor.gui.spawn_reinforcements_chance");
    public static final MutableComponent CAN_JOIN_RAID = translated("cadeditor.gui.can_join_raid");
    public static final MutableComponent PATROL_LEADER = translated("cadeditor.gui.patrol_leader");
    public static final MutableComponent SAVE_VAULT_GREEN = translated("cadeditor.gui.save_vault").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent OPEN_EDITOR = translated("cadeditor.key.editor");
    public static final MutableComponent OPEN_NBT_EDITOR = translated("cadeditor.key.nbt_editor");
    public static final MutableComponent OPEN_SNBT_EDITOR = translated("cadeditor.key.snbt_editor");
    public static final MutableComponent COPY_COMMAND_GREEN = translated("cadeditor.gui.copy_command_alt").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent FORMAT = translated("cadeditor.gui.format");

    public static final MutableComponent TOOL = translated("cadeditor.gui.tool");
    public static final MutableComponent TOOL_MINING_SPEED = translated("cadeditor.gui.tool_default_speed");
    public static final MutableComponent TOOL_DAMAGE_PER_BLOCK = translated("cadeditor.gui.tool_damage_per_block");
    public static final MutableComponent TOOL_RULE = translated("cadeditor.gui.tool_rule");
    public static final MutableComponent TOOL_RULE_HELP = translated("cadeditor.gui.tool_rule_help");
    public static final MutableComponent MAP = translated("cadeditor.gui.map");
    public static final MutableComponent MAP_ID_TOGGLE = translated("cadeditor.gui.map_id_toggle");
    public static final MutableComponent MAP_ID_VALUE = translated("cadeditor.gui.map_id_value");
    public static final MutableComponent MAP_COLOR = translated("cadeditor.gui.map_color");
    public static final MutableComponent MAP_LOCK = translated("cadeditor.gui.map_lock");
    public static final MutableComponent MAP_DECORATION = translated("cadeditor.gui.map_decoration");
    public static final MutableComponent MAP_DECORATION_NAME = translated("cadeditor.gui.map_decoration_name");
    public static final MutableComponent MAP_DECORATION_TYPE = translated("cadeditor.gui.map_decoration_type");
    public static final MutableComponent MAP_DECORATION_X = translated("cadeditor.gui.map_decoration_x");
    public static final MutableComponent MAP_DECORATION_Z = translated("cadeditor.gui.map_decoration_z");
    public static final MutableComponent MAP_DECORATION_ROTATION = translated("cadeditor.gui.map_decoration_rotation");
    public static final MutableComponent MAP_DECORATION_PRESET = translated("cadeditor.gui.map_decoration_preset");
    public static final MutableComponent CROSSBOW = translated("cadeditor.gui.crossbow");
    public static final MutableComponent CROSSBOW_PROJECTILE = translated("cadeditor.gui.crossbow_projectile");
    public static final MutableComponent CROSSBOW_PROJECTILE_HELP = translated("cadeditor.gui.crossbow_projectile_help");
    public static final MutableComponent LODESTONE = translated("cadeditor.gui.lodestone");
    public static final MutableComponent LODESTONE_TARGET_TOGGLE = translated("cadeditor.gui.lodestone_target");
    public static final MutableComponent DIMENSION = translated("cadeditor.gui.dimension");
    public static final MutableComponent POSITION_X = translated("cadeditor.gui.position_x");
    public static final MutableComponent POSITION_Y = translated("cadeditor.gui.position_y");
    public static final MutableComponent POSITION_Z = translated("cadeditor.gui.position_z");
    public static final MutableComponent LODESTONE_TRACKED = translated("cadeditor.gui.lodestone_tracked");
    public static final MutableComponent CONTAINER_CONTENTS = translated("cadeditor.gui.container_contents");
    public static final MutableComponent CONTAINER_SLOT = translated("cadeditor.gui.container_slot_data");
    public static final MutableComponent BUNDLE_CONTENTS = translated("cadeditor.gui.bundle_contents");
    public static final MutableComponent POT_DECORATIONS = translated("cadeditor.gui.pot_decorations");
    public static final MutableComponent POT_BACK = translated("cadeditor.gui.pot_back");
    public static final MutableComponent POT_LEFT = translated("cadeditor.gui.pot_left");
    public static final MutableComponent POT_RIGHT = translated("cadeditor.gui.pot_right");
    public static final MutableComponent POT_FRONT = translated("cadeditor.gui.pot_front");
    public static final MutableComponent TRIM = translated("cadeditor.gui.trim");
    public static final MutableComponent TRIM_PATTERN = translated("cadeditor.gui.trim_pattern");
    public static final MutableComponent TRIM_MATERIAL = translated("cadeditor.gui.trim_material");
    public static final MutableComponent TRIM_SHOW_TOOLTIP = translated("cadeditor.gui.trim_show_tooltip");
    public static final MutableComponent INSTRUMENT = translated("cadeditor.gui.instrument");
    public static final MutableComponent OMINOUS_BOTTLE = translated("cadeditor.gui.ominous_bottle");
    public static final MutableComponent OMINOUS_BOTTLE_AMPLIFIER = translated("cadeditor.gui.ominous_bottle_amplifier");
    public static final MutableComponent BANNER = translated("cadeditor.gui.banner");
    public static final MutableComponent BANNER_BASE_COLOR = translated("cadeditor.gui.banner_base_color");
    public static final MutableComponent BANNER_LAYER = translated("cadeditor.gui.banner_layer");
    public static final MutableComponent BUCKET_ENTITY = translated("cadeditor.gui.bucket_entity");
    public static final MutableComponent BUCKET_USE_ADVANCED = translated("cadeditor.gui.bucket_use_advanced");
    public static final MutableComponent BUCKET_AXOLOTL_VARIANT = translated("cadeditor.gui.bucket_axolotl_variant");
    public static final MutableComponent BUCKET_TROPICAL_PATTERN = translated("cadeditor.gui.bucket_tropical_pattern");
    public static final MutableComponent BUCKET_TROPICAL_BODY_COLOR = translated("cadeditor.gui.bucket_tropical_body_color");
    public static final MutableComponent BUCKET_TROPICAL_PATTERN_COLOR = translated("cadeditor.gui.bucket_tropical_pattern_color");
    public static final MutableComponent BUCKET_ENTITY_DATA = translated("cadeditor.gui.bucket_entity_data");
    public static final MutableComponent NOTE_BLOCK_SOUND_CATEGORY = translated("cadeditor.gui.note_block_sound_category");
    public static final MutableComponent NOTE_BLOCK_SOUND = translated("cadeditor.gui.note_block_sound");
    public static final MutableComponent SOUND_EVENT = translated("cadeditor.gui.sound_event");
    public static final MutableComponent SOUND_FILTER_ALL = translated("cadeditor.gui.sound_filter_all");
    public static final MutableComponent FIREWORK_STAR = translated("cadeditor.gui.firework_star");
    public static final MutableComponent FIREWORK_ROCKET = translated("cadeditor.gui.firework_rocket");
    public static final MutableComponent FIREWORK_SHAPE = translated("cadeditor.gui.firework_shape");
    public static final MutableComponent FIREWORK_TRAIL = translated("cadeditor.gui.firework_trail");
    public static final MutableComponent FIREWORK_TWINKLE = translated("cadeditor.gui.firework_twinkle");
    public static final MutableComponent FIREWORK_ADD_PRIMARY_COLOR = translated("cadeditor.gui.firework_add_primary_color");
    public static final MutableComponent FIREWORK_ADD_FADE_COLOR = translated("cadeditor.gui.firework_add_fade_color");
    public static final MutableComponent FIREWORK_ADD_EXPLOSION = translated("cadeditor.gui.firework_add_explosion");
    public static final MutableComponent FIREWORK_FLIGHT_DURATION = translated("cadeditor.gui.firework_flight_duration");
    public static MutableComponent errorServerModRequired(MutableComponent with) {
        return translated("cadeditor.message.error_server_mod", with).withStyle(ChatFormatting.RED);
    }

    public static MutableComponent errorPermissionDenied(MutableComponent with) {
        return translated("cadeditor.message.error_permission_denied", with).withStyle(ChatFormatting.RED);
    }

    public static MutableComponent choose(MutableComponent with) {
        return translated("cadeditor.gui.choose", with);
    }

    public static MutableComponent updateLogIntro(String version) {
        return translated("cadeditor.gui.update_log_intro", text(version)).withStyle(ChatFormatting.GRAY);
    }
    public static MutableComponent updateLogSectionTitle(String sectionKey) {
        return translated("cadeditor.gui.update_log.section." + sectionKey).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    public static MutableComponent updateLogDate(String date) {
        return translated("cadeditor.gui.update_log_date", text(date)).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent updateLogNoDate() {
        return translated("cadeditor.gui.update_log_no_date").withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent updateLogAboutTitle() {
        return translated("cadeditor.gui.update_log_about_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static MutableComponent updateLogAboutSubtitle() {
        return translated("cadeditor.gui.update_log_about_subtitle").withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent updateVersion(String version, String type) {
        MutableComponent versionText = text(version);
        MutableComponent header = type == null || type.isEmpty()
                ? translated("cadeditor.gui.update_version_simple", versionText)
                : translated("cadeditor.gui.update_version_with_type", versionText, versionType(type));
        return header.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static MutableComponent updateBullet(String line) {
        return translated("cadeditor.gui.update_bullet", text(line)).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent versionType(String type) {
        if (type == null || type.isEmpty()) {
            return text("");
        }
        String norm = type.trim().toLowerCase(Locale.ROOT);

        String key = switch (norm) {
            case "alpha", "a" -> "cadeditor.gui.version_type.alpha";
            case "beta", "b"  -> "cadeditor.gui.version_type.beta";
            case "release", "r" -> "cadeditor.gui.version_type.release";
            default -> null;
        };

        return (key != null) ? translated(key) : text(type);
    }

    public static MutableComponent addTag(String color, String with) {
        var text = translated("cadeditor.gui.add_tag", text(with));
        TextColor.parseColor(color).result().ifPresent(c -> text.withStyle(style -> style.withColor(c)));
        return text;
    }

    public static MutableComponent editorTitle(MutableComponent type) {
        return title(translated("cadeditor.gui.editor_title", type));
    }

    public static MutableComponent editorTitle(String type) {
        return editorTitle(text(type));
    }

    public static MutableComponent title(MutableComponent text) {
        return text.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    public static MutableComponent addListEntry(MutableComponent with) {
        return translated("cadeditor.gui.add", with);
    }

    public static MutableComponent lore(int i) {
        return translated("cadeditor.gui.lore", text(Integer.toString(i)));
    }

    public static MutableComponent trade(int i) {
        return translated("cadeditor.gui.trade", text(Integer.toString(i)));
    }

    public static MutableComponent useAnimationOption(ItemUseAnimation animation) {
        return translated("cadeditor.gui.use_animation.option." + animation.getSerializedName());
    }

    public static MutableComponent equipmentSlot(net.minecraft.world.entity.EquipmentSlot slot) {
        return translated("cadeditor.gui.equipment_slot." + slot.getName());
    }

    public static MutableComponent direction(Direction direction) {
        return translated("cadeditor.gui.direction." + direction.getSerializedName());
    }

    public static MutableComponent gui(String s) {
        return translated("cadeditor.gui." + s);
    }

    public static MutableComponent attributeModifierOperationText(int value) {
        return text("OP: " + value);
    }

    public static MutableComponent attributeModifierOperationTooltip(int value) {
        return translated("cadeditor.gui.operation", translated("cadeditor.gui.operation." + value));
    }

    public static MutableComponent hide(MutableComponent with) {
        return translated("cadeditor.gui.hide", with);
    }

    public static MutableComponent[] savedVault(MutableComponent arg) {
        return arrayTextWithArg("cadeditor.gui.saved_vault", 4, arg);
    }

    public static MutableComponent copyCommand(String command) {
        return translated("cadeditor.gui.copy_command", command);
    }

    public static MutableComponent todoPlaceholder(Component feature) {
        return translated("cadeditor.gui.todo_placeholder", feature);
    }

    public static MutableComponent[] commandCopied(String arg) {
        return arrayTextWithArg("cadeditor.gui.command_copied", 4, arg);
    }

    public static MutableComponent give(MutableComponent with) {
        return translated("cadeditor.gui.give", with);
    }

    public static MutableComponent soundFilterNamespace(String namespace) {
        return translated("cadeditor.gui.sound_filter_namespace", namespace);
    }

    public static MutableComponent fireworkPrimaryColor(int index) {
        return translated("cadeditor.gui.firework_color_primary", index);
    }

    public static MutableComponent fireworkFadeColor(int index) {
        return translated("cadeditor.gui.firework_color_fade", index);
    }

    public static MutableComponent fireworkExplosion(int index) {
        return translated("cadeditor.gui.firework_explosion", index);
    }

    public static MutableComponent fireworkRemoveExplosion(int index) {
        return translated("cadeditor.gui.firework_remove_explosion", index);
    }

    private static MutableComponent[] arrayText(String key, int size) {
        MutableComponent[] array = new MutableComponent[size];
        for (int i = 0; i < size; i++) {
            array[i] = translated(key + "." + i);
        }
        return array;
    }

    public static MutableComponent[] arrayTextWithArg(String key, int size, Object arg) {
        MutableComponent[] array = new MutableComponent[size];
        for (int i = 0; i < size; i++) {
            array[i] = i == 0 ? translated(key + "." + i, arg) : translated(key + "." + i);
        }
        return array;
    }

    public static class Messages {

        public static final MutableComponent ERROR_GENERIC = prefixed(translated("cadeditor.message.error_generic")).withStyle(ChatFormatting.RED);
        public static final MutableComponent NO_PERMISSION = prefixed(translated("cadeditor.message.no_permission")).withStyle(ChatFormatting.RED);
        public static final MutableComponent NO_BLOCK_DATA = prefixed(translated("cadeditor.message.no_block_data")).withStyle(ChatFormatting.RED);
        public static final MutableComponent VAULT_ITEM_GIVE_SUCCESS = prefixed(translated("cadeditor.message.vault_item_give_success")).withStyle(ChatFormatting.GREEN);

        public static MutableComponent successUpdate(MutableComponent arg) {
            return prefixed(translated("cadeditor.message.success_update", arg)).withStyle(ChatFormatting.GREEN);
        }

        public static MutableComponent errorServerModRequired(MutableComponent arg) {
            return prefixed(ModTexts.errorServerModRequired(arg)).withStyle(ChatFormatting.RED);
        }

        public static MutableComponent errorPermissionDenied(MutableComponent arg) {
            return prefixed(ModTexts.errorPermissionDenied(arg)).withStyle(ChatFormatting.RED);
        }

        public static MutableComponent errorNoTargetFound(MutableComponent arg) {
            return prefixed(translated("cadeditor.message.no_target_found", arg)).withStyle(ChatFormatting.RED);
        }

        public static MutableComponent warnNotImplemented(MutableComponent arg) {
            return prefixed(translated("cadeditor.message.not_implemented", arg)).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent successSavedVault(MutableComponent arg) {
            return prefixed(translated("cadeditor.message.saved_vault", arg)).withStyle(ChatFormatting.GREEN);
        }

        public static MutableComponent warnNotSavedVault(MutableComponent arg) {
            return prefixed(translated("cadeditor.message.not_saved_vault", arg)).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent successCopyClipboard(String arg) {
            return prefixed(translated("cadeditor.message.copied_clipboard", arg)).withStyle(ChatFormatting.GREEN);
        }

        public static MutableComponent successCopyGiveCommand() {
            return prefixed(translated("cadeditor.message.copied_give_clipboard")).withStyle(ChatFormatting.GREEN);
        }

        public static MutableComponent toolRuleHelp() {
            return prefixed(translated("cadeditor.message.tool_rule_help")).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent crossbowProjectileHelp() {
            return prefixed(translated("cadeditor.message.crossbow_projectile_help")).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent containerLootHint() {
            return prefixed(translated("cadeditor.message.container_loot_hint")).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent containerLootExample() {
            return prefixed(translated("cadeditor.message.container_loot_example")).withStyle(ChatFormatting.YELLOW);
        }

        public static MutableComponent snbtInvalidCannotApply() {
            return prefixed(translated("cadeditor.message.snbt_invalid_cannot_apply")).withStyle(ChatFormatting.RED);
        }

        public static MutableComponent potDecorationInvalid() {
            return prefixed(translated("cadeditor.message.pot_decoration_invalid")).withStyle(ChatFormatting.RED);
        }

        private static MutableComponent prefixed(MutableComponent arg) {
            return translated("chat.type.announcement", translated("cadeditor"), arg);
        }
    }

    public static class Literal {

        public static final MutableComponent BYTE = text("Byte").withStyle(ChatFormatting.BLUE);
        public static final MutableComponent BYTE_ARRAY = text("Byte Array").withStyle(ChatFormatting.BLUE);
        public static final MutableComponent COMPOUND = text("Compound").withStyle(ChatFormatting.LIGHT_PURPLE);
        public static final MutableComponent DOUBLE = text("Double").withStyle(ChatFormatting.YELLOW);
        public static final MutableComponent FLOAT = text("Float").withStyle(ChatFormatting.LIGHT_PURPLE);
    public static final MutableComponent GREEN = text("List").withStyle(ChatFormatting.GREEN);
    public static final MutableComponent HEX = text("Hex");
    public static final MutableComponent INT = text("Int").withStyle(ChatFormatting.AQUA);
        public static final MutableComponent INT_ARRAY = text("Int Array").withStyle(ChatFormatting.AQUA);
        public static final MutableComponent LONG = text("Long").withStyle(ChatFormatting.RED);
        public static final MutableComponent LONG_ARRAY = text("Long Array").withStyle(ChatFormatting.RED);
        public static final MutableComponent SHORT = text("Short").withStyle(ChatFormatting.GREEN);
        public static final MutableComponent STRING = text("String").withStyle(ChatFormatting.GRAY);
    }
}
