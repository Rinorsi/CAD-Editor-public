package com.github.rinorsi.cadeditor.client.context;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ItemEditorContext extends EditorContext<ItemEditorContext> {
    private static final Pattern SIMPLE_KEY = Pattern.compile("[a-z0-9_\\-+.]+");
    private static final Set<String> BOOLEAN_HINTS = Set.of(
            "enchantment_glint_override",
            "keep_hanging",
            "keep_owner",
            "keep_on_death",
            "keep_on_death_loss",
            "creative_slot_lock",
            "show_in_tooltip",
            "show_in_additional_tooltip",
            "show_in_enchantment_tooltip",
            "show_in_tooltips",
            "hide_tooltip",
            "hide_additional_tooltip",
            "hide_enchantment_tooltip",
            "hide_tooltips",
            "glint_override"
    );
    private ItemStack itemStack;

    public ItemEditorContext(ItemStack itemStack, Component errorTooltip, boolean canSaveToVault, Consumer<ItemEditorContext> action) {
        super(saveStack(itemStack), errorTooltip, canSaveToVault, action);
        this.itemStack = itemStack.copy();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack stack) {
        this.itemStack = stack;
    }

    @Override
    public List<String> getStringSuggestions(List<String> path) {
        List<String> suggestions = super.getStringSuggestions(path);
        if (!suggestions.isEmpty()) {
            return suggestions;
        }
        if (path == null || path.isEmpty()) {
            return List.of();
        }
        String key = lastKey(path);
        if (key == null) {
            return List.of();
        }
        String parent = previousNamedKey(path, 1);
        String grandParent = previousNamedKey(path, 2);

        if ("id".equals(key)) {
            if (path.size() == 1) {
                return ClientCache.getItemSuggestions();
            }
            if (isEnchantmentContainer(parent, grandParent)) {
                return ClientCache.getEnchantmentSuggestions();
            }
            if (isEffectContainer(parent, grandParent)) {
                return ClientCache.getEffectSuggestions();
            }
            if (isItemContainer(parent, grandParent)) {
                return ClientCache.getItemSuggestions();
            }
            if (equalsAnyIgnoreCase(parent, "entity")
                    && equalsAnyIgnoreCase(grandParent, "minecraft:bucket_entity_data", "BucketEntityData")) {
                return ClientCache.getEntitySuggestions();
            }
        }

        if (equalsAnyIgnoreCase(key, "item", "Item")) {
            return ClientCache.getItemSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "potion")) {
            return ClientCache.getPotionSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "effect")) {
            return ClientCache.getEffectSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "instrument")) {
            return ClientCache.getInstrumentSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "pattern", "pattern_id") && containsTrimContext(path)) {
            return ClientCache.getTrimPatternSuggestions();
        }
        if (equalsAnyIgnoreCase(key, "material", "material_id") && containsTrimContext(path)) {
            return ClientCache.getTrimMaterialSuggestions();
        }

        return List.of();
    }

    private static boolean isEnchantmentContainer(String parent, String grandParent) {
        return equalsAnyIgnoreCase(parent, "Enchantments", "StoredEnchantments", "minecraft:enchantments")
                || (equalsAnyIgnoreCase(parent, "levels") && equalsAnyIgnoreCase(grandParent, "minecraft:enchantments"));
    }

    private static boolean isEffectContainer(String parent, String grandParent) {
        return equalsAnyIgnoreCase(parent, "effects", "custom_potion_effects", "status_effects")
                || (equalsAnyIgnoreCase(parent, "entries") && equalsAnyIgnoreCase(grandParent, "minecraft:food"));
    }

    private static boolean isItemContainer(String parent, String grandParent) {
        return equalsAnyIgnoreCase(parent, "Items", "items", "HandItems", "ArmorItems", "ChargedProjectiles", "contents")
                || (equalsAnyIgnoreCase(parent, "stacks") && equalsAnyIgnoreCase(grandParent, "minecraft:container"));
    }

    private static boolean containsTrimContext(List<String> path) {
        return pathContains(path, "minecraft:trim") || pathContains(path, "Trim") || pathContains(path, "trim");
    }

    private static ItemStack decodeStack(HolderLookup.Provider lookup, CompoundTag edited) {
        return ClientUtil.parseItemStack(edited);
    }

    @Override
    public void update() {
        ItemStack result = itemStack.copy();
        CompoundTag edited = getTag();
        HolderLookup.Provider lookup = ClientUtil.registryAccess();
        if (edited != null) {
            try {
                ItemStack parsed = decodeStack(lookup, edited);
                if (!parsed.isEmpty()) {
                    result = parsed;
                }
            } catch (Exception ignored) {
            }
        }
        itemStack = result;
        setTag(saveStack(result));
        super.update();
    }

    @Override
    public void saveToVault() {
        Vault.getInstance().saveItem(getTag());
        ClientUtil.showMessage(ModTexts.Messages.successSavedVault(ModTexts.ITEM));
    }

    @Override
    public MutableComponent getTargetName() {
        return ModTexts.ITEM;
    }

    @Override
    public String getCommandName() {
        return "/give";
    }

    @Override
    protected String getCommand() {
        return buildGiveCommand(getItemStack());
    }

    private static CompoundTag saveStack(ItemStack stack) {
        return ClientUtil.saveItemStack(stack);
    }

    private static String buildGiveCommand(ItemStack stack) {
        CompoundTag data = saveStack(stack);
        String id = data.getStringOr("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        CompoundTag components = data.getCompound("components")
                .map(CompoundTag::copy)
                .orElseGet(CompoundTag::new);
        if (data.contains("tag") && !components.contains("minecraft:custom_data")) {
            data.getCompound("tag").filter(tag -> !tag.isEmpty())
                    .ifPresent(legacy -> components.put("minecraft:custom_data", legacy.copy()));
        }
        StringBuilder builder = new StringBuilder("/give @p ").append(id);
        String componentSpec = formatComponentList(components);
        if (!componentSpec.isEmpty()) {
            builder.append(componentSpec);
        }
        int count = stack.getCount();
        if (count > 1) {
            builder.append(' ').append(count);
        }
        return builder.toString();
    }

    private static String formatComponentList(CompoundTag components) {
        if (components == null || components.isEmpty()) {
            return "";
        }
        CompoundTag normalized = normalizeComponents(components);
        List<String> keys = new ArrayList<>(normalized.keySet());
        Collections.sort(keys);
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        boolean hasEntry = false;
        for (String key : keys) {
            if (key.startsWith("!")) {
                continue;
            }
            Tag value = normalized.get(key);
            if (value == null || value.getId() == Tag.TAG_END) {
                continue;
            }
            String rendered = formatTagValue(key, value);
            if (rendered.isEmpty()) {
                continue;
            }
            joiner.add(key + "=" + rendered);
            hasEntry = true;
        }
        return hasEntry ? joiner.toString() : "";
    }

    private static CompoundTag normalizeComponents(CompoundTag components) {
        CompoundTag normalized = components.copy();
        Tag attributeTag = normalized.get("minecraft:attribute_modifiers");
        if (attributeTag instanceof CompoundTag attributeCompound) {
            ListTag modifiers = attributeCompound.getListOrEmpty("modifiers");
            if (!modifiers.isEmpty()) {
                Set<UUID> usedIds = new HashSet<>();
                for (Tag entryTag : modifiers) {
                    if (entryTag instanceof CompoundTag modifier) {
                        UUID id = readModifierUUID(modifier);
                        if (id != null && !usedIds.add(id)) {
                            id = null;
                        }
                        if (id == null) {
                            id = generateModifierUUID(modifier, usedIds);
                        }
                        modifier.putString("id", id.toString());
                    }
                }
            }
        }
        return normalized;
    }

    private static UUID generateModifierUUID(CompoundTag modifier, Set<UUID> usedIds) {
        String seed = buildModifierSeed(modifier);
        int salt = 0;
        while (true) {
            UUID uuid = UUID.nameUUIDFromBytes((seed + "#" + salt).getBytes(StandardCharsets.UTF_8));
            if (usedIds.add(uuid)) {
                return uuid;
            }
            salt++;
        }
    }

    private static String buildModifierSeed(CompoundTag modifier) {
        CompoundTag copy = modifier.copy();
        copy.remove("id");
        copy.remove("UUID");
        copy.remove("uuid");
        return copy.toString();
    }

    private static UUID readModifierUUID(CompoundTag modifier) {
        UUID id = parseUuidFromTag(modifier, "id");
        if (id != null) {
            return id;
        }
        id = parseUuidFromTag(modifier, "uuid");
        if (id != null) {
            return id;
        }
        return parseUuidFromTag(modifier, "UUID");
    }

    private static UUID parseUuidFromTag(CompoundTag modifier, String key) {
        if (!modifier.contains(key)) {
            return null;
        }
        Tag tag = modifier.get(key);
        if (tag == null) {
            return null;
        }
        if (tag.getId() == Tag.TAG_STRING) {
            return parseUuidString(((StringTag) tag).asString().orElse(""));
        }
        if (tag.getId() == Tag.TAG_INT_ARRAY) {
            return modifier.getIntArray(key).map(ItemEditorContext::uuidFromIntArray).orElse(null);
        }
        return null;
    }

    private static UUID parseUuidString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return UUID.fromString(trimmed);
        } catch (IllegalArgumentException ignored) {
        }
        String candidate = trimmed;
        int underscore = candidate.lastIndexOf('_');
        if (underscore >= 0 && underscore + 1 < candidate.length()) {
            candidate = candidate.substring(underscore + 1);
        } else if (candidate.contains(":")) {
            candidate = candidate.substring(candidate.lastIndexOf(':') + 1);
        }
        candidate = candidate.replace("-", "");
        return parseUuidFromHex(candidate);
    }

    private static UUID parseUuidFromHex(String value) {
        if (value == null) {
            return null;
        }
        String hex = value.trim();
        if (hex.length() != 32) {
            return null;
        }
        try {
            long most = Long.parseUnsignedLong(hex.substring(0, 16), 16);
            long least = Long.parseUnsignedLong(hex.substring(16), 16);
            return new UUID(most, least);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static UUID uuidFromIntArray(int[] data) {
        if (data.length != 4) {
            return null;
        }
        long most = ((long) data[0] << 32) | (data[1] & 0xffffffffL);
        long least = ((long) data[2] << 32) | (data[3] & 0xffffffffL);
        return new UUID(most, least);
    }

    private static String formatTagValue(String key, Tag tag) {
        return switch (tag.getId()) {
            case Tag.TAG_COMPOUND -> formatCompound((CompoundTag) tag);
            case Tag.TAG_LIST -> formatList((ListTag) tag);
            case Tag.TAG_STRING -> formatString(((StringTag) tag).asString().orElse(""));
            case Tag.TAG_BYTE -> formatByte(key, ((NumericTag) tag).byteValue());
            case Tag.TAG_SHORT -> Integer.toString(((NumericTag) tag).shortValue());
            case Tag.TAG_INT -> Integer.toString(((NumericTag) tag).intValue());
            case Tag.TAG_LONG -> Long.toString(((NumericTag) tag).longValue());
            case Tag.TAG_FLOAT -> formatFloating(((NumericTag) tag).floatValue());
            case Tag.TAG_DOUBLE -> formatFloating(((NumericTag) tag).doubleValue());
            case Tag.TAG_BYTE_ARRAY, Tag.TAG_INT_ARRAY, Tag.TAG_LONG_ARRAY -> tag.toString();
            default -> tag.toString();
        };
    }

    private static String formatCompound(CompoundTag tag) {
        if (tag.isEmpty()) {
            return "{}";
        }
        List<String> keys = new ArrayList<>(tag.keySet());
        Collections.sort(keys);
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (String key : keys) {
            Tag value = tag.get(key);
            if (value == null || value.getId() == Tag.TAG_END) {
                continue;
            }
            String formattedKey = SIMPLE_KEY.matcher(key).matches() ? key : StringTag.quoteAndEscape(key);
            String formattedValue = formatTagValue(key, value);
            joiner.add(formattedKey + ":" + formattedValue);
        }
        return joiner.toString();
    }

    private static String formatList(ListTag list) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Tag tag : list) {
            joiner.add(formatTagValue(null, tag));
        }
        return joiner.toString();
    }

    private static String formatString(String value) {
        return SIMPLE_KEY.matcher(value).matches() ? value : StringTag.quoteAndEscape(value);
    }

    private static String formatByte(String key, byte value) {
        if (isBooleanKey(key) && (value == 0 || value == 1)) {
            return value == 1 ? "true" : "false";
        }
        return Byte.toString(value);
    }

    private static boolean isBooleanKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        String bare = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        if (BOOLEAN_HINTS.contains(bare)) {
            return true;
        }
        return bare.startsWith("is_") || bare.startsWith("has_") || bare.startsWith("can_") ||
                bare.startsWith("show_") || bare.startsWith("keep_") || bare.startsWith("allow_") ||
                bare.startsWith("use_") || bare.startsWith("enable_") || bare.startsWith("should_") ||
                bare.startsWith("hide_");
    }

    private static String formatFloating(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Double.toString(value);
        }
        double roundedTenth = Math.round(value * 10.0) / 10.0;
        if (Math.abs(value - roundedTenth) < 1e-6) {
            return formatFixed(roundedTenth, 1);
        }
        double roundedThousandth = Math.round(value * 1000.0) / 1000.0;
        if (Math.abs(value - roundedThousandth) < 1e-7) {
            return trimTrailingZeros(Double.toString(roundedThousandth));
        }
        return trimTrailingZeros(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString());
    }

    private static String formatFixed(double value, int decimals) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP);
        return trimTrailingZeros(bd.toPlainString());
    }

    private static String trimTrailingZeros(String value) {
        if (!value.contains(".")) {
            return value;
        }
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && value.charAt(end - 1) == '.') {
            end++;
        }
        if (end > value.length()) {
            end = value.length();
        }
        return value.substring(0, end);
    }

    @Override
    protected MutableComponent getCopySuccessMessage() {
        if ("/give".equals(getCommandName())) {
            return ModTexts.Messages.successCopyGiveCommand();
        }
        return super.getCopySuccessMessage();
    }
}
