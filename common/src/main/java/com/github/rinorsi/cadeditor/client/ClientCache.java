package com.github.rinorsi.cadeditor.client;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.*;
import com.github.rinorsi.cadeditor.common.ColoredItemHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.common.loot.LootTableIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ClientCache {
    private static List<String> itemSuggestions;
    private static List<ItemListSelectionElementModel> itemSelectionItems;
    private static List<String> blockSuggestions;
    private static List<ItemListSelectionElementModel> blockSelectionItems;
    private static List<TagListSelectionElementModel> blockTagSelectionItems;
    private static List<TagListSelectionElementModel> itemTagSelectionItems;
    private static List<TagListSelectionElementModel> damageTypeTagSelectionItems;
    private static List<String> enchantmentSuggestions;
    private static List<EnchantmentListSelectionElementModel> enchantmentSelectionItems;
    private static List<String> attributeSuggestions;
    private static List<ListSelectionElementModel> attributeSelectionItems;
    private static List<String> potionSuggestions;
    private static List<ItemListSelectionElementModel> potionSelectionItems;
    private static List<String> effectSuggestions;
    private static List<SelectableSpriteListSelectionElementModel> effectSelectionItems;
    private static List<String> entitySuggestions;
    private static List<EntityListSelectionElementModel> entitySelectionItems;
    private static List<String> villagerProfessionSuggestions;
    private static List<ListSelectionElementModel> villagerProfessionSelectionItems;
    private static List<String> villagerTypeSuggestions;
    private static List<ListSelectionElementModel> villagerTypeSelectionItems;
    private static List<String> trimPatternSuggestions;
    private static List<TrimPatternSelectionElementModel> trimPatternSelectionItems;
    private static List<String> trimMaterialSuggestions;
    private static List<TrimMaterialSelectionElementModel> trimMaterialSelectionItems;
    private static List<String> instrumentSuggestions;
    private static List<ListSelectionElementModel> instrumentSelectionItems;
    private static List<String> soundEventSuggestions;
    private static List<SoundEventListSelectionElementModel> soundEventSelectionItems;
    private static List<ListSelectionFilter> soundEventFilters;
    private static List<String> equipmentAssetSuggestions;
    private static List<ListSelectionElementModel> equipmentAssetSelectionItems;
    private static final List<Identifier> BUILTIN_EQUIPMENT_ASSETS = buildBuiltinEquipmentAssets();
    private static List<String> blockEntityTypeSuggestions;
    private static List<String> lootTableSuggestions;
    private static List<String> componentTypeIds;

    public static void invalidate() {
        itemSuggestions = null;
        itemSelectionItems = null;
        blockSuggestions = null;
        blockSelectionItems = null;
        blockTagSelectionItems = null;
        itemTagSelectionItems = null;
        damageTypeTagSelectionItems = null;
        enchantmentSuggestions = null;
        enchantmentSelectionItems = null;
        attributeSuggestions = null;
        attributeSelectionItems = null;
        potionSuggestions = null;
        potionSelectionItems = null;
        effectSuggestions = null;
        effectSelectionItems = null;
        entitySuggestions = null;
        entitySelectionItems = null;
        villagerProfessionSuggestions = null;
        villagerProfessionSelectionItems = null;
        villagerTypeSuggestions = null;
        villagerTypeSelectionItems = null;
        trimPatternSuggestions = null;
        trimPatternSelectionItems = null;
        trimMaterialSuggestions = null;
        trimMaterialSelectionItems = null;
        instrumentSuggestions = null;
        instrumentSelectionItems = null;
        soundEventSuggestions = null;
        soundEventSelectionItems = null;
        soundEventFilters = null;
        equipmentAssetSuggestions = null;
        equipmentAssetSelectionItems = null;
        blockEntityTypeSuggestions = null;
        lootTableSuggestions = null;
        componentTypeIds = null;
    }

    public static List<String> getItemSuggestions() {
        return itemSuggestions == null ? itemSuggestions = buildSuggestions(BuiltInRegistries.ITEM) : itemSuggestions;
    }

    public static List<ItemListSelectionElementModel> getItemSelectionItems() {
        return itemSelectionItems == null ? itemSelectionItems = buildItemSelectionItems() : itemSelectionItems;
    }

    public static List<String> getBlockSuggestions() {
        return blockSuggestions == null ? blockSuggestions = buildSuggestions(BuiltInRegistries.BLOCK) : blockSuggestions;
    }

    public static List<ItemListSelectionElementModel> getBlockSelectionItems() {
        return blockSelectionItems == null ? blockSelectionItems = buildBlockSelectionItems() : blockSelectionItems;
    }


    public static List<TagListSelectionElementModel> getBlockTagSelectionItems() {
        return blockTagSelectionItems == null ? blockTagSelectionItems = buildBlockTagSelectionItems() : blockTagSelectionItems;
    }
    public static List<TagListSelectionElementModel> getItemTagSelectionItems() {
        return itemTagSelectionItems == null ? itemTagSelectionItems = buildItemTagSelectionItems() : itemTagSelectionItems;
    }

    public static List<TagListSelectionElementModel> getDamageTypeTagSelectionItems() {
        return damageTypeTagSelectionItems == null ? damageTypeTagSelectionItems = buildDamageTypeTagSelectionItems() : damageTypeTagSelectionItems;
    }

    public static List<String> getBlockEntityTypeSuggestions() {
        return blockEntityTypeSuggestions == null
                ? blockEntityTypeSuggestions = buildSuggestions(BuiltInRegistries.BLOCK_ENTITY_TYPE)
                : blockEntityTypeSuggestions;
    }

    public static List<String> getEnchantmentSuggestions() {
        if (enchantmentSuggestions == null) {
            enchantmentSuggestions = registryAccess().lookup(Registries.ENCHANTMENT)
                    .map(ClientCache::buildSuggestions)
                    .orElseGet(List::of);
        }
        return enchantmentSuggestions;
    }

    public static List<SortedEnchantmentListSelectionElementModel> getSortedEnchantmentSelectionItems(ItemStack target) {
        if (enchantmentSelectionItems == null) {
            enchantmentSelectionItems = buildEnchantmentSelectionItems();
        }
        return enchantmentSelectionItems.stream()
            .map(item -> {
                boolean curse = item.getEnchantment().is(EnchantmentTags.CURSE);
                boolean canApply = enchantmentCanApply(item.getEnchantment(), target);
                return new SortedEnchantmentListSelectionElementModel(item, curse, canApply);
            })
            .sorted(
                Comparator.comparing((SortedEnchantmentListSelectionElementModel m) -> m.canApply() ? 0 : 1)
                .thenComparing(m -> m.isCurse() ? 1 : 0)
                .thenComparing(m -> m.getName().toLowerCase(java.util.Locale.ROOT))
            )
            .toList();
    }

    public static Optional<EnchantmentListSelectionElementModel> findEnchantmentSelectionItem(Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        if (enchantmentSelectionItems == null) {
            enchantmentSelectionItems = buildEnchantmentSelectionItems();
        }
        return enchantmentSelectionItems.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    private static boolean enchantmentCanApply(Holder<Enchantment> enchantment, ItemStack target) {
        if (target == null || target.isEmpty()) {
            return false;
        }
        if (target.is(Items.ENCHANTED_BOOK) || target.is(Items.BOOK)) {
            return true;
        }
        Enchantment value = enchantment.value();
        if (value.canEnchant(target)) {
            return true;
        }
        Item item = target.getItem();
        var definition = value.definition();
        Optional<? extends HolderLookup.RegistryLookup<Item>> registry = registryAccess().lookup(Registries.ITEM);
        if (definition.primaryItems().map(set -> holderSetContainsItem(set, item, registry)).orElse(false)) {
            return true;
        }
        return holderSetContainsItem(definition.supportedItems(), item, registry);
    }

    public static List<String> getComponentTypeIds() {
        if (componentTypeIds == null) {
            List<String> ids = new ArrayList<>(BuiltInRegistries.DATA_COMPONENT_TYPE.keySet().size());
            for (Identifier id : BuiltInRegistries.DATA_COMPONENT_TYPE.keySet()) {
                ids.add(id.toString());
            }
            ids.sort(String::compareTo);
            componentTypeIds = List.copyOf(ids);
        }
        return componentTypeIds;
    }

    public static boolean isComponentIdKnown(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        Identifier id = Identifier.tryParse(value);
        if (id == null) {
            return false;
        }
        return BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(id);
    }

    private static boolean holderSetContainsItem(HolderSet<Item> holders, Item item, Optional<? extends HolderLookup.RegistryLookup<Item>> registry) {
        if (holders == null) {
            return false;
        }
        try {
            if (holders.contains(BuiltInRegistries.ITEM.wrapAsHolder(item))) {
                return true;
            }
        } catch (IllegalStateException ignored) {
            // Fallback to manual inspection when the registry holder hasn't been bound yet.
        }
        ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
        if (key == null && registry.isPresent()) {
            key = registry.get().listElements()
                .filter(reference -> {
                    try {
                        return reference.value() == item;
                    } catch (IllegalStateException ignored) {
                        return false;
                    }
                })
                .map(Holder.Reference::key)
                .findFirst()
                .orElse(null);
        }
        for (Holder<Item> holder : holders) {
            if (key != null && holder.is(key)) {
                return true;
            }
            try {
                if (holder.value() == item) {
                    return true;
                }
            } catch (IllegalStateException ignored) {
                // Skip holders that still rely on unresolved tags.
            }
        }
        return false;
    }

    public static List<String> getAttributeSuggestions() {
        return attributeSuggestions == null ? attributeSuggestions = buildSuggestions(BuiltInRegistries.ATTRIBUTE) : attributeSuggestions;
    }

    public static List<ListSelectionElementModel> getAttributeSelectionItems() {
        return attributeSelectionItems == null ? attributeSelectionItems = buildAttributeSelectionItems() : attributeSelectionItems;
    }

    public static Optional<ListSelectionElementModel> findAttributeSelectionItem(Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        if (attributeSelectionItems == null) {
            attributeSelectionItems = buildAttributeSelectionItems();
        }
        return attributeSelectionItems.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public static List<String> getPotionSuggestions() {
        return potionSuggestions == null ? potionSuggestions = buildSuggestions(BuiltInRegistries.POTION) : potionSuggestions;
    }

    public static List<ItemListSelectionElementModel> getPotionSelectionItems() {
        return potionSelectionItems == null ? potionSelectionItems = buildPotionSelectionItems() : potionSelectionItems;
    }

    public static List<String> getEffectSuggestions() {
        return effectSuggestions == null ? effectSuggestions = buildSuggestions(BuiltInRegistries.MOB_EFFECT) : effectSuggestions;
    }

    public static List<SelectableSpriteListSelectionElementModel> getEffectSelectionItems() {
        return effectSelectionItems == null ? effectSelectionItems = buildEffectSelectionItems() : effectSelectionItems;
    }

    public static List<String> getEntitySuggestions() {
        return entitySuggestions == null ? entitySuggestions = buildSuggestions(BuiltInRegistries.ENTITY_TYPE) : entitySuggestions;
    }

    public static List<String> getEquipmentAssetSuggestions() {
        return equipmentAssetSuggestions == null ? equipmentAssetSuggestions = buildEquipmentAssetSuggestions() : equipmentAssetSuggestions;
    }

    public static List<ListSelectionElementModel> getEquipmentAssetSelectionItems() {
        return equipmentAssetSelectionItems == null ? equipmentAssetSelectionItems = buildEquipmentAssetSelectionItems() : equipmentAssetSelectionItems;
    }

    public static List<EntityListSelectionElementModel> getEntitySelectionItems() {
        return entitySelectionItems == null ? entitySelectionItems = buildEntitySelectionItems() : entitySelectionItems;
    }

    public static List<String> getVillagerProfessionSuggestions() {
        return villagerProfessionSuggestions == null ? villagerProfessionSuggestions = buildSuggestions(BuiltInRegistries.VILLAGER_PROFESSION) : villagerProfessionSuggestions;
    }

    public static List<ListSelectionElementModel> getVillagerProfessionSelectionItems() {
        return villagerProfessionSelectionItems == null ? villagerProfessionSelectionItems = buildVillagerProfessionSelectionItems() : villagerProfessionSelectionItems;
    }

    public static List<String> getVillagerTypeSuggestions() {
        return villagerTypeSuggestions == null ? villagerTypeSuggestions = buildSuggestions(BuiltInRegistries.VILLAGER_TYPE) : villagerTypeSuggestions;
    }

    public static List<ListSelectionElementModel> getVillagerTypeSelectionItems() {
        return villagerTypeSelectionItems == null ? villagerTypeSelectionItems = buildVillagerTypeSelectionItems() : villagerTypeSelectionItems;
    }

    public static List<String> getTrimPatternSuggestions() {
        if (trimPatternSuggestions == null) {
            trimPatternSuggestions = getTrimPatternSelectionItems().stream()
                    .map(element -> element.getId().toString())
                    .toList();
        }
        return trimPatternSuggestions;
    }

    public static List<TrimPatternSelectionElementModel> getTrimPatternSelectionItems() {
        if (trimPatternSelectionItems == null) {
            trimPatternSelectionItems = registryAccess().lookup(Registries.TRIM_PATTERN)
                    .map(ClientCache::buildTrimPatternSelectionItems)
                    .orElseGet(List::of);
        }
        return trimPatternSelectionItems;
    }

    public static List<String> getTrimMaterialSuggestions() {
        if (trimMaterialSuggestions == null) {
            trimMaterialSuggestions = getTrimMaterialSelectionItems().stream()
                    .map(element -> element.getId().toString())
                    .toList();
        }
        return trimMaterialSuggestions;
    }

    public static List<TrimMaterialSelectionElementModel> getTrimMaterialSelectionItems() {
        if (trimMaterialSelectionItems == null) {
            trimMaterialSelectionItems = registryAccess().lookup(Registries.TRIM_MATERIAL)
                    .map(ClientCache::buildTrimMaterialSelectionItems)
                    .orElseGet(List::of);
        }
        return trimMaterialSelectionItems;
    }

    public static List<String> getInstrumentSuggestions() {
        if (instrumentSuggestions == null) {
            instrumentSuggestions = getInstrumentSelectionItems().stream()
                    .map(element -> element.getId().toString())
                    .toList();
        }
        return instrumentSuggestions;
    }

    public static List<ListSelectionElementModel> getInstrumentSelectionItems() {
        if (instrumentSelectionItems == null) {
            instrumentSelectionItems = registryAccess().lookup(Registries.INSTRUMENT)
                    .map(ClientCache::buildInstrumentSelectionItems)
                    .orElseGet(List::of);
        }
        return instrumentSelectionItems;
    }

    public static List<String> getSoundEventSuggestions() {
        return soundEventSuggestions == null
                ? soundEventSuggestions = buildSuggestions(BuiltInRegistries.SOUND_EVENT)
                : soundEventSuggestions;
    }

    public static List<SoundEventListSelectionElementModel> getSoundEventSelectionItems() {
        if (soundEventSelectionItems == null) {
            soundEventSelectionItems = buildSoundEventSelectionItems();
        }
        return soundEventSelectionItems;
    }

    public static List<ListSelectionFilter> getSoundEventFilters() {
        if (soundEventFilters == null) {
            soundEventFilters = buildSoundEventFilters();
        }
        return soundEventFilters;
    }

    public static List<String> getLootTableSuggestions() {
        if (lootTableSuggestions == null) {
            lootTableSuggestions = buildLootTableSuggestions();
        }
        return lootTableSuggestions;
    }

    public static Optional<SelectableSpriteListSelectionElementModel> findEffectSelectionItem(Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        if (effectSelectionItems == null) {
            effectSelectionItems = buildEffectSelectionItems();
        }
        return effectSelectionItems.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    private static List<String> buildSuggestions(Registry<?> registry) {
        List<String> suggestions = new ArrayList<>();
        registry.entrySet().stream()
                .map(e -> e.getKey().identifier().toString())
                .forEach(id -> {
                    suggestions.add(id);
                    if (id.startsWith("minecraft:")) {
                        suggestions.add(id.substring(10));
                    }
                })
        ;
        return suggestions;
    }

    private static List<String> buildSuggestions(HolderLookup.RegistryLookup<?> lookup) {
        List<String> suggestions = new ArrayList<>();
        lookup.listElements().forEach(holder -> {
            String id = holder.key().identifier().toString();
            suggestions.add(id);
            if (id.startsWith("minecraft:")) {
                suggestions.add(id.substring(10));
            }
        });
        return suggestions;
    }

    private static List<String> buildLootTableSuggestions() {
        List<Identifier> ids = LootTableIndex.getAll();
        if (ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Identifier id : ids) {
            String full = id.toString();
            values.add(full);
            if (full.startsWith("minecraft:")) {
                values.add(full.substring(10));
            }
        }
        return List.copyOf(values);
    }

    private static List<ItemListSelectionElementModel> buildItemSelectionItems() {
        return BuiltInRegistries.ITEM.entrySet().stream()
                .map(e -> (ItemListSelectionElementModel) new SelectableItemListSelectionElementModel(
                        e.getValue().getDescriptionId(),
                        e.getKey().identifier(),
                        () -> new ItemStack(e.getValue())))
                .sorted().toList();
    }

    private static List<TagListSelectionElementModel> buildBlockTagSelectionItems() {
        return buildTagSelectionItems(Registries.BLOCK);
    }

    private static List<TagListSelectionElementModel> buildItemTagSelectionItems() {
        return buildTagSelectionItems(Registries.ITEM);
    }

    private static List<TagListSelectionElementModel> buildDamageTypeTagSelectionItems() {
        return buildTagSelectionItems(Registries.DAMAGE_TYPE);
    }

    private static <T> List<TagListSelectionElementModel> buildTagSelectionItems(ResourceKey<Registry<T>> registryKey) {
        return registryAccess().lookup(registryKey)
                .map(lookup -> lookup.listTags()
                        .map(named -> (TagListSelectionElementModel) new SelectableTagListSelectionElementModel(named.key().location()))
                        .sorted()
                        .toList())
                .orElseGet(List::of);
    }

    private static List<ItemListSelectionElementModel> buildBlockSelectionItems() {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .map(e -> (ItemListSelectionElementModel) new SelectableItemListSelectionElementModel(
                        e.getValue().getDescriptionId(),
                        e.getKey().identifier(),
                        () -> new ItemStack(e.getValue())))
                .sorted().toList();
    }

    private static List<EntityListSelectionElementModel> buildEntitySelectionItems() {
        return BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
                .map(e -> new EntityListSelectionElementModel(e.getValue(), e.getKey().identifier()))
                .sorted().toList();
    }

    private static List<ListSelectionElementModel> buildVillagerProfessionSelectionItems() {
        return BuiltInRegistries.VILLAGER_PROFESSION.entrySet().stream()
                .map(e -> new ListSelectionElementModel(villagerProfessionTranslation(e.getKey().identifier()), e.getKey().identifier()))
                .sorted().toList();
    }

    private static List<ListSelectionElementModel> buildVillagerTypeSelectionItems() {
        return BuiltInRegistries.VILLAGER_TYPE.entrySet().stream()
                .map(e -> new ListSelectionElementModel(villagerTypeTranslation(e.getKey().identifier()), e.getKey().identifier()))
                .sorted().toList();
    }

    private static String villagerProfessionTranslation(Identifier id) {
        return "villager.profession." + id.getPath();
    }

    private static String villagerTypeTranslation(Identifier id) {
        return "entity.minecraft.villager." + id.getPath();
    }

    private static List<EnchantmentListSelectionElementModel> buildEnchantmentSelectionItems() {
        return registryAccess().lookup(Registries.ENCHANTMENT)
                .map(lookup -> lookup.listElements()
                        .map(ref -> {
                            Item iconItem = getEnchantmentTypeItem(ref);
                            ItemStack icon = new ItemStack(iconItem);
                            Component categoryLabel = buildEnchantmentCategoryLabel(ref, icon);
                            return new EnchantmentListSelectionElementModel(
                                    ref.value().description().getString(),
                                    ref.key().identifier(),
                                    ref,
                                    () -> new ItemStack(iconItem),
                                    categoryLabel);
                        })
                        .sorted()
                        .toList())
                .orElseGet(List::of);
    }

    private static Component buildEnchantmentCategoryLabel(Holder<Enchantment> enchantment, ItemStack icon) {
        List<Component> labels = enchantment.value().definition().primaryItems()
                .map(ClientCache::describeItemSet)
                .orElseGet(List::of);
        if (labels.isEmpty()) {
            labels = describeItemSet(enchantment.value().definition().supportedItems());
        }
        if (!labels.isEmpty()) {
            return joinComponents(labels);
        }
        List<Component> slotLabels = describeSlotGroups(enchantment.value().definition().slots());
        if (!slotLabels.isEmpty()) {
            return joinComponents(slotLabels);
        }
        return icon.getHoverName().copy();
    }

    private static Item getEnchantmentTypeItem(Holder<Enchantment> enchantment) {
        return enchantment.value().definition().primaryItems()
                .flatMap(ClientCache::pickRepresentativeItem)
                .or(() -> pickRepresentativeItem(enchantment.value().definition().supportedItems()))
                .or(() -> pickItemFromSlots(enchantment.value().definition().slots()))
                .orElse(Items.ENCHANTED_BOOK);
    }

    private static List<Component> describeItemSet(HolderSet<Item> holders) {
        List<Component> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        holders.stream()
                .map(Holder::value)
                .filter(item -> item != Items.AIR)
                .forEach(item -> {
                    if (seen.add(item.getDescriptionId())) {
                        result.add(new ItemStack(item).getHoverName().copy());
                    }
                });
        return limitComponentList(result);
    }

    private static List<Component> describeSlotGroups(List<EquipmentSlotGroup> slots) {
        Set<String> seen = new LinkedHashSet<>();
        List<Component> result = new ArrayList<>();
        for (EquipmentSlotGroup slot : slots) {
            String raw = slot.name().toLowerCase(Locale.ROOT).replace('_', ' ');
            if (seen.add(raw)) {
                result.add(Component.literal(capitalizeWords(raw)));
            }
        }
        return limitComponentList(result);
    }

    private static List<Component> limitComponentList(List<Component> components) {
        int max = 3;
        if (components.size() <= max) {
            return components;
        }
        List<Component> limited = new ArrayList<>(components.subList(0, max));
        limited.add(Component.literal("…"));
        return limited;
    }

    private static Component joinComponents(List<Component> components) {
        MutableComponent result = Component.empty();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                result.append(Component.literal(", "));
            }
            result.append(components.get(i).copy());
        }
        return result;
    }

    private static String capitalizeWords(String input) {
        if (input.isEmpty()) {
            return input;
        }
        StringBuilder builder = new StringBuilder(input.length());
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == ' ') {
                capitalizeNext = true;
                builder.append(c);
            } else if (capitalizeNext) {
                builder.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static Optional<Item> pickRepresentativeItem(HolderSet<Item> holders) {
        return holders.stream()
                .map(Holder::value)
                .filter(item -> item != Items.AIR)
                .min(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
    }

    private static Optional<Item> pickItemFromSlots(List<EquipmentSlotGroup> slots) {
        if (slots.contains(EquipmentSlotGroup.ARMOR) || slots.contains(EquipmentSlotGroup.BODY)) {
            return Optional.of(Items.IRON_CHESTPLATE);
        }
        if (slots.contains(EquipmentSlotGroup.HEAD)) {
            return Optional.of(Items.DIAMOND_HELMET);
        }
        if (slots.contains(EquipmentSlotGroup.CHEST)) {
            return Optional.of(Items.DIAMOND_CHESTPLATE);
        }
        if (slots.contains(EquipmentSlotGroup.LEGS)) {
            return Optional.of(Items.DIAMOND_LEGGINGS);
        }
        if (slots.contains(EquipmentSlotGroup.FEET)) {
            return Optional.of(Items.DIAMOND_BOOTS);
        }
        if (slots.contains(EquipmentSlotGroup.MAINHAND) || slots.contains(EquipmentSlotGroup.HAND)) {
            return Optional.of(Items.DIAMOND_SWORD);
        }
        if (slots.contains(EquipmentSlotGroup.OFFHAND)) {
            return Optional.of(Items.SHIELD);
        }
        return Optional.empty();
    }

    private static List<ListSelectionElementModel> buildAttributeSelectionItems() {
        return BuiltInRegistries.ATTRIBUTE.entrySet().stream()
                .map(e -> new ListSelectionElementModel(e.getValue().getDescriptionId(), e.getKey().identifier()))
                .sorted().toList();
    }

    private static List<ItemListSelectionElementModel> buildPotionSelectionItems() {
        return registryAccess().lookup(Registries.POTION)
                .map(lookup -> lookup.listElements()
                        .map(holder -> {
                            PotionContents contents = new PotionContents(holder);
                            String name = contents.getName(Items.POTION.getDescriptionId() + ".effect.").getString();
                            return new ItemListSelectionElementModel(
                                    name,
                                    holder.key().identifier(),
                                    () -> ColoredItemHelper.createColoredPotionItem(holder.key().identifier(), Color.NONE)
                            );
                        })
                        .sorted()
                        .toList())
                .orElseGet(List::of);
    }

    private static List<SelectableSpriteListSelectionElementModel> buildEffectSelectionItems() {
        return registryAccess().lookup(Registries.MOB_EFFECT)
                .map(lookup -> lookup.listElements()
                        .map(holder -> new SelectableSpriteListSelectionElementModel(
                                holder.value().getDescriptionId(),
                                holder.key().identifier(),
                                mobEffectSpriteSupplier(holder)))
                        .sorted()
                        .toList())
                .orElseGet(List::of);
    }

    private static List<TrimPatternSelectionElementModel> buildTrimPatternSelectionItems(HolderLookup.RegistryLookup<TrimPattern> lookup) {
        return lookup.listElements()
                .map(holder -> {
                    Identifier patternId = holder.key().identifier();
                    return new TrimPatternSelectionElementModel(
                            holder.value().description(),
                            patternId,
                            () -> patternIconStack(patternId));
                })
                .sorted()
                .toList();
    }

    private static List<TrimMaterialSelectionElementModel> buildTrimMaterialSelectionItems(HolderLookup.RegistryLookup<TrimMaterial> lookup) {
        return lookup.listElements()
                .map(holder -> {
                    Identifier materialId = holder.key().identifier();
                    return new TrimMaterialSelectionElementModel(
                            holder.value().description(),
                            materialId,
                            () -> materialIconStack(materialId));
                })
                .sorted()
                .toList();
    }

    private static List<ListSelectionElementModel> buildInstrumentSelectionItems(HolderLookup.RegistryLookup<Instrument> lookup) {
        return lookup.listElements()
                .map(holder -> new ListSelectionElementModel(holder.key().identifier().toString(), holder.key().identifier()))
                .sorted()
                .toList();
    }

    private static List<SoundEventListSelectionElementModel> buildSoundEventSelectionItems() {
        return BuiltInRegistries.SOUND_EVENT.entrySet().stream()
                .map(entry -> new SoundEventListSelectionElementModel(entry.getKey().identifier(), entry.getValue()))
                .sorted()
                .toList();
    }

    private static List<String> buildEquipmentAssetSuggestions() {
        return registryAccess().lookup(EquipmentAssets.ROOT_ID)
                .map(lookup -> lookup.listElements()
                        .map(element -> element.key().identifier().toString())
                        .sorted()
                        .toList())
                .orElse(BUILTIN_EQUIPMENT_ASSETS.stream()
                        .map(Identifier::toString)
                        .collect(Collectors.toUnmodifiableList()));
    }

    private static List<ListSelectionElementModel> buildEquipmentAssetSelectionItems() {
        return registryAccess().lookup(EquipmentAssets.ROOT_ID)
                .map(lookup -> lookup.listElements()
                        .map(element -> (ListSelectionElementModel) new EquipmentAssetListSelectionElementModel(element.key().identifier()))
                        .sorted()
                        .toList())
                .orElse(BUILTIN_EQUIPMENT_ASSETS.stream()
                        .map(id -> (ListSelectionElementModel) new EquipmentAssetListSelectionElementModel(id))
                        .collect(Collectors.toUnmodifiableList()));
    }

    private static List<Identifier> buildBuiltinEquipmentAssets() {
        List<Identifier> ids = new ArrayList<>();
        ids.add(Identifier.withDefaultNamespace("leather"));
        ids.add(Identifier.withDefaultNamespace("chainmail"));
        ids.add(Identifier.withDefaultNamespace("iron"));
        ids.add(Identifier.withDefaultNamespace("gold"));
        ids.add(Identifier.withDefaultNamespace("diamond"));
        ids.add(Identifier.withDefaultNamespace("turtle_scute"));
        ids.add(Identifier.withDefaultNamespace("netherite"));
        ids.add(Identifier.withDefaultNamespace("armadillo_scute"));
        ids.add(Identifier.withDefaultNamespace("elytra"));
        ids.add(Identifier.withDefaultNamespace("saddle"));
        ids.add(Identifier.withDefaultNamespace("trader_llama"));
        for (String color : List.of(
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        )) {
            ids.add(Identifier.withDefaultNamespace("carpet_" + color));
            ids.add(Identifier.withDefaultNamespace("harness_" + color));
        }
        return List.copyOf(ids);
    }

    private static List<ListSelectionFilter> buildSoundEventFilters() {
        List<SoundEventListSelectionElementModel> items = getSoundEventSelectionItems();
        if (items.isEmpty()) {
            return List.of(new ListSelectionFilter("all", ModTexts.SOUND_FILTER_ALL, null));
        }
        List<ListSelectionFilter> filters = new ArrayList<>();
        filters.add(new ListSelectionFilter("all", ModTexts.SOUND_FILTER_ALL, null));
        items.stream()
                .map(SoundEventListSelectionElementModel::getNamespace)
                .distinct()
                .sorted()
                .forEach(namespace -> filters.add(new ListSelectionFilter("namespace:" + namespace,
                        ModTexts.soundFilterNamespace(namespace), element -> element instanceof SoundEventListSelectionElementModel sound
                                && sound.getNamespace().equals(namespace))));
        return List.copyOf(filters);
    }

    private static Supplier<TextureAtlasSprite> mobEffectSpriteSupplier(Holder<MobEffect> holder) {
        return () -> {
            var texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
            if (texture instanceof TextureAtlas atlas) {
                return atlas.getSprite(Gui.getMobEffectSprite(holder));
            }
            return ((TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS))
                    .getSprite(Gui.getMobEffectSprite(holder));
        };
    }

    private static ItemStack patternIconStack(Identifier patternId) {
        return new ItemStack(patternTemplateItem(patternId));
    }

    private static Item patternTemplateItem(Identifier patternId) {
        return switch (patternId.getPath()) {
            case "sentry" -> Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "dune" -> Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "coast" -> Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "wild" -> Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "ward" -> Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "eye" -> Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "vex" -> Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "tide" -> Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "snout" -> Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "rib" -> Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "spire" -> Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "wayfinder" -> Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "shaper" -> Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "silence" -> Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "raiser" -> Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "host" -> Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "flow" -> Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "bolt" -> Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE;
            default -> Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE;
        };
    }

    private static ItemStack materialIconStack(Identifier materialId) {
        return new ItemStack(materialDisplayItem(materialId));
    }

    private static Item materialDisplayItem(Identifier materialId) {
        return switch (materialId.getPath()) {
            case "quartz" -> Items.QUARTZ;
            case "iron" -> Items.IRON_INGOT;
            case "netherite" -> Items.NETHERITE_INGOT;
            case "redstone" -> Items.REDSTONE;
            case "copper" -> Items.COPPER_INGOT;
            case "gold" -> Items.GOLD_INGOT;
            case "emerald" -> Items.EMERALD;
            case "diamond" -> Items.DIAMOND;
            case "lapis" -> Items.LAPIS_LAZULI;
            case "amethyst" -> Items.AMETHYST_SHARD;
            case "resin" -> Items.RESIN_BRICK;
            default -> Items.IRON_INGOT;
        };
    }

    private static Supplier<ItemStack> iconFromHolder(Holder<Item> holder) {
        return () -> {
            try {
                return holder.value().getDefaultInstance();
            } catch (IllegalStateException ignored) {
                return ItemStack.EMPTY;
            }
        };
    }

    private static HolderLookup.Provider registryAccess() {
        return ClientUtil.registryAccess();
    }
}

