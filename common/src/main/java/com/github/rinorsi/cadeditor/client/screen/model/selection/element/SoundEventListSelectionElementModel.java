package com.github.rinorsi.cadeditor.client.screen.model.selection.element;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class SoundEventListSelectionElementModel extends ListSelectionElementModel {
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[\\.:/_\\-]+");

    public enum SoundCategory {
        COMBAT("combat", "combat 战斗 攻击 命中 伤害"),
        ENTITY("entity", "entity mob 生物 实体 村民 怪物"),
        BLOCK("block", "block 方块 放置 破坏 脚步"),
        ITEM("item", "item 物品 使用 穿戴 消耗"),
        AMBIENT("ambient", "ambient 环境 天气 洞穴 自然"),
        UI("ui", "ui 界面 按钮 点击 菜单"),
        MUSIC("music", "music 音乐 唱片 背景"),
        MECHANICS("mechanics", "redstone mechanics 机械 红石 装置"),
        MISC("misc", "misc 其他 未分类");

        private final String id;
        private final String searchTerms;

        SoundCategory(String id, String searchTerms) {
            this.id = id;
            this.searchTerms = searchTerms.toLowerCase(Locale.ROOT);
        }

        public String id() {
            return id;
        }

        public String searchTerms() {
            return searchTerms;
        }

        public MutableComponent label() {
            return Component.translatable("cadeditor.gui.sound_category_" + id);
        }
    }

    private final Component displayName;
    private final String namespace;
    private final String searchName;
    private final SoundCategory primaryCategory;
    private final String categorySearch;
    private final SoundEvent soundEvent;

    public SoundEventListSelectionElementModel(ResourceLocation id, SoundEvent event) {
        super(id.toString(), id);
        this.soundEvent = event;
        this.namespace = id.getNamespace();
        Component translated = Component.translatable(Util.makeDescriptionId("sound_event", id));
        this.displayName = translated;
        String loweredDisplay = translated.getString().toLowerCase(Locale.ROOT);
        this.searchName = loweredDisplay;
        this.primaryCategory = classify(id, loweredDisplay);
        this.categorySearch = primaryCategory.searchTerms();
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    public String getNamespace() {
        return namespace;
    }

    public SoundCategory getPrimaryCategory() {
        return primaryCategory;
    }

    public SoundEvent getSoundEvent() {
        return soundEvent;
    }

    @Override
    public Type getType() {
        return Type.SOUND;
    }

    @Override
    public boolean matches(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        return super.matches(s) || searchName.contains(lower) || categorySearch.contains(lower);
    }

    private static SoundCategory classify(ResourceLocation id, String displayName) {
        String fullId = id.toString().toLowerCase(Locale.ROOT);
        List<String> tokens = tokenize(fullId, displayName);
        Map<SoundCategory, Integer> scores = new EnumMap<>(SoundCategory.class);
        for (SoundCategory category : SoundCategory.values()) {
            scores.put(category, 0);
        }

        boostByPrefix(scores, fullId);
        for (String token : tokens) {
            scoreToken(scores, token);
        }

        SoundCategory winner = SoundCategory.MISC;
        int bestScore = Integer.MIN_VALUE;
        for (SoundCategory category : SoundCategory.values()) {
            int score = scores.getOrDefault(category, 0);
            if (score > bestScore || (score == bestScore && tieBreak(category, winner))) {
                bestScore = score;
                winner = category;
            }
        }
        if (bestScore <= 0) {
            return fallbackByPrefix(fullId);
        }
        return winner;
    }

    private static List<String> tokenize(String fullId, String displayName) {
        List<String> tokens = new ArrayList<>();
        for (String part : TOKEN_SPLIT.split(fullId)) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        for (String part : TOKEN_SPLIT.split(displayName.toLowerCase(Locale.ROOT))) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private static void boostByPrefix(Map<SoundCategory, Integer> scores, String fullId) {
        if (fullId.startsWith("minecraft:music.") || fullId.contains(".music.")) add(scores, SoundCategory.MUSIC, 8);
        if (fullId.startsWith("minecraft:ui.") || fullId.contains(".ui.")) add(scores, SoundCategory.UI, 8);
        if (fullId.startsWith("minecraft:block.") || fullId.contains(".block.")) add(scores, SoundCategory.BLOCK, 7);
        if (fullId.startsWith("minecraft:item.") || fullId.contains(".item.")) add(scores, SoundCategory.ITEM, 7);
        if (fullId.startsWith("minecraft:entity.") || fullId.contains(".entity.")) add(scores, SoundCategory.ENTITY, 7);
        if (fullId.startsWith("minecraft:ambient.") || fullId.contains(".ambient.")) add(scores, SoundCategory.AMBIENT, 7);
        if (fullId.contains("redstone") || fullId.contains("piston") || fullId.contains("dispenser")) add(scores, SoundCategory.MECHANICS, 7);
    }

    private static void scoreToken(Map<SoundCategory, Integer> scores, String token) {
        if (in(token, "attack", "hit", "hurt", "damage", "death", "crit", "critical", "sweep", "knockback", "projectile", "shoot", "stab", "pierce")) {
            add(scores, SoundCategory.COMBAT, 4);
        }
        if (in(token, "entity", "mob", "villager", "zombie", "skeleton", "creeper", "spider", "wither", "dragon", "allay", "player", "piglin", "illager", "slime", "goat", "frog", "camel", "wolf", "cat", "horse")) {
            add(scores, SoundCategory.ENTITY, 4);
        }
        if (in(token, "block", "break", "place", "step", "dig", "door", "trapdoor", "button", "anvil", "chest", "stone", "wood", "metal", "gravel", "sand", "glass", "snow", "ladder", "sculk")) {
            add(scores, SoundCategory.BLOCK, 3);
        }
        if (in(token, "item", "equip", "use", "consume", "drink", "eat", "armor", "tool", "bucket", "shears", "bow", "crossbow", "trident", "shield", "totem", "flintandsteel", "bottle", "potion", "goat_horn")) {
            add(scores, SoundCategory.ITEM, 3);
        }
        if (in(token, "ambient", "weather", "rain", "thunder", "wind", "cave", "underwater", "ocean", "river", "forest", "portal", "nether", "end", "lava", "fire")) {
            add(scores, SoundCategory.AMBIENT, 3);
        }
        if (in(token, "ui", "click", "hover", "toast", "recipe_book", "button", "menu", "inventory", "screen", "book")) {
            add(scores, SoundCategory.UI, 3);
        }
        if (in(token, "music", "record", "disc", "jukebox")) {
            add(scores, SoundCategory.MUSIC, 5);
        }
        if (in(token, "redstone", "piston", "dispenser", "dropper", "comparator", "repeater", "lever", "tripwire", "beacon", "smithing", "brewing", "enchant")) {
            add(scores, SoundCategory.MECHANICS, 4);
        }
    }

    private static SoundCategory fallbackByPrefix(String fullId) {
        if (fullId.contains("music")) return SoundCategory.MUSIC;
        if (fullId.contains("ui")) return SoundCategory.UI;
        if (fullId.contains("entity")) return SoundCategory.ENTITY;
        if (fullId.contains("block")) return SoundCategory.BLOCK;
        if (fullId.contains("item")) return SoundCategory.ITEM;
        if (fullId.contains("ambient")) return SoundCategory.AMBIENT;
        return SoundCategory.MISC;
    }

    private static boolean tieBreak(SoundCategory candidate, SoundCategory current) {
        return priority(candidate) < priority(current);
    }

    private static int priority(SoundCategory category) {
        return switch (category) {
            case COMBAT -> 0;
            case ENTITY -> 1;
            case BLOCK -> 2;
            case ITEM -> 3;
            case MECHANICS -> 4;
            case UI -> 5;
            case MUSIC -> 6;
            case AMBIENT -> 7;
            case MISC -> 8;
        };
    }

    private static void add(Map<SoundCategory, Integer> scores, SoundCategory category, int points) {
        scores.put(category, scores.getOrDefault(category, 0) + points);
    }

    private static boolean in(String token, String... values) {
        for (String value : values) {
            if (value.equals(token)) {
                return true;
            }
        }
        return false;
    }
}
