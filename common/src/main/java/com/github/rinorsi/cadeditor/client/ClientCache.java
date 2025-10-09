package com.github.rinorsi.cadeditor.client;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.*;
import com.github.rinorsi.cadeditor.common.ColoredItemHelper;
import com.github.rinorsi.cadeditor.common.loot.LootTableIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ClientCache {
    private static List<String> itemSuggestions;
    private static List<ItemListSelectionElementModel> itemSelectionItems;
    private static List<String> blockSuggestions;
    private static List<ItemListSelectionElementModel> blockSelectionItems;
    private static List<TagListSelectionElementModel> blockTagSelectionItems;
    private static List<String> enchantmentSuggestions;
    private static List<EnchantmentListSelectionElementModel> enchantmentSelectionItems;
    private static List<String> attributeSuggestions;
    private static List<ListSelectionElementModel> attributeSelectionItems;
    private static List<String> potionSuggestions;
    private static List<ItemListSelectionElementModel> potionSelectionItems;
    private static List<String> effectSuggestions;
    private static List<SpriteListSelectionElementModel> effectSelectionItems;
    private static List<String> entitySuggestions;
    private static List<EntityListSelectionElementModel> entitySelectionItems;
    private static List<String> trimPatternSuggestions;
    private static List<TrimPatternSelectionElementModel> trimPatternSelectionItems;
    private static List<String> trimMaterialSuggestions;
    private static List<TrimMaterialSelectionElementModel> trimMaterialSelectionItems;
    private static List<String> instrumentSuggestions;
    private static List<ListSelectionElementModel> instrumentSelectionItems;
    private static List<String> blockEntityTypeSuggestions;
    private static List<String> lootTableSuggestions;

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

    public static Optional<EnchantmentListSelectionElementModel> findEnchantmentSelectionItem(ResourceLocation id) {
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
        Optional<HolderLookup.RegistryLookup<Item>> registry = registryAccess().lookup(Registries.ITEM);
        if (definition.primaryItems().map(set -> holderSetContainsItem(set, item, registry)).orElse(false)) {
            return true;
        }
        return holderSetContainsItem(definition.supportedItems(), item, registry);
    }

    private static boolean holderSetContainsItem(HolderSet<Item> holders, Item item, Optional<HolderLookup.RegistryLookup<Item>> registry) {
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

    public static Optional<ListSelectionElementModel> findAttributeSelectionItem(ResourceLocation id) {
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

    public static List<SpriteListSelectionElementModel> getEffectSelectionItems() {
        return effectSelectionItems == null ? effectSelectionItems = buildEffectSelectionItems() : effectSelectionItems;
    }

    public static List<String> getEntitySuggestions() {
        return entitySuggestions == null ? entitySuggestions = buildSuggestions(BuiltInRegistries.ENTITY_TYPE) : entitySuggestions;
    }

    public static List<EntityListSelectionElementModel> getEntitySelectionItems() {
        return entitySelectionItems == null ? entitySelectionItems = buildEntitySelectionItems() : entitySelectionItems;
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

    public static List<String> getLootTableSuggestions() {
        if (lootTableSuggestions == null) {
            lootTableSuggestions = buildLootTableSuggestions();
        }
        return lootTableSuggestions;
    }

    public static Optional<SpriteListSelectionElementModel> findEffectSelectionItem(ResourceLocation id) {
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
                .map(e -> e.getKey().location().toString())
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
            String id = holder.key().location().toString();
            suggestions.add(id);
            if (id.startsWith("minecraft:")) {
                suggestions.add(id.substring(10));
            }
        });
        return suggestions;
    }

    private static List<String> buildLootTableSuggestions() {
        List<ResourceLocation> ids = LootTableIndex.getAll();
        if (ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (ResourceLocation id : ids) {
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
                .map(e -> new ItemListSelectionElementModel(e.getValue().getDescriptionId(), e.getKey().location(), new ItemStack(e.getValue())))
                .sorted().toList();
    }

    private static List<TagListSelectionElementModel> buildBlockTagSelectionItems() {
        return registryAccess().lookup(Registries.BLOCK)
                .map(lookup -> lookup.listTags()
                        .map(named -> (TagListSelectionElementModel) new SelectableTagListSelectionElementModel(named.key().location()))
                        .sorted()
                        .toList())
                .orElseGet(List::of);
    }

    private static List<ItemListSelectionElementModel> buildBlockSelectionItems() {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .map(e -> (ItemListSelectionElementModel) new SelectableItemListSelectionElementModel(e.getValue().getDescriptionId(), e.getKey().location(), new ItemStack(e.getValue())))
                .sorted().toList();
    }

    private static List<EntityListSelectionElementModel> buildEntitySelectionItems() {
        return BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
                .map(e -> new EntityListSelectionElementModel(e.getValue(), e.getKey().location()))
                .sorted().toList();
    }

    private static List<EnchantmentListSelectionElementModel> buildEnchantmentSelectionItems() {
        return registryAccess().lookup(Registries.ENCHANTMENT)
                .map(lookup -> lookup.listElements()
                        .map(ref -> {
                            ItemStack icon = new ItemStack(getEnchantmentTypeItem(ref));
                            Component categoryLabel = buildEnchantmentCategoryLabel(ref, icon);
                            return new EnchantmentListSelectionElementModel(ref.value().description().getString(), ref.key().location(), ref, icon, categoryLabel);
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
                .map(e -> new ListSelectionElementModel(e.getValue().getDescriptionId(), e.getKey().location()))
                .sorted().toList();
    }

    private static List<ItemListSelectionElementModel> buildPotionSelectionItems() {
        return BuiltInRegistries.POTION.entrySet().stream()
                .map(e -> BuiltInRegistries.POTION.getHolder(e.getKey())
                        .map(holder -> new ItemListSelectionElementModel(
                                Potion.getName(Optional.of(holder), Items.POTION.getDescriptionId() + ".effect."),
                                e.getKey().location(),
                                ColoredItemHelper.createColoredPotionItem(e.getKey().location(), Color.NONE)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted().toList();
    }

    private static List<SpriteListSelectionElementModel> buildEffectSelectionItems() {
        return BuiltInRegistries.MOB_EFFECT.entrySet().stream()
                .map(e -> BuiltInRegistries.MOB_EFFECT.getHolder(e.getKey())
                        .map(holder -> new SpriteListSelectionElementModel(holder.value().getDescriptionId(), e.getKey().location(), () -> Minecraft.getInstance().getMobEffectTextures().get(holder)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted().toList();
    }

    private static List<TrimPatternSelectionElementModel> buildTrimPatternSelectionItems(HolderLookup.RegistryLookup<TrimPattern> lookup) {
        return lookup.listElements()
                .map(holder -> {
                    var itemHolder = holder.value().templateItem();
                    return new TrimPatternSelectionElementModel(holder.value().description(), holder.key().location(), iconFromHolder(itemHolder));
                })
                .sorted()
                .toList();
    }

    private static List<TrimMaterialSelectionElementModel> buildTrimMaterialSelectionItems(HolderLookup.RegistryLookup<TrimMaterial> lookup) {
        return lookup.listElements()
                .map(holder -> new TrimMaterialSelectionElementModel(holder.value().description(), holder.key().location(), iconFromHolder(holder.value().ingredient())))
                .sorted()
                .toList();
    }

    private static List<ListSelectionElementModel> buildInstrumentSelectionItems(HolderLookup.RegistryLookup<Instrument> lookup) {
        return lookup.listElements()
                .map(holder -> new ListSelectionElementModel(holder.key().location().toString(), holder.key().location()))
                .sorted()
                .toList();
    }

    private static ItemStack iconFromHolder(Holder<Item> holder) {
        try {
            return holder.value().getDefaultInstance();
        } catch (IllegalStateException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static HolderLookup.Provider registryAccess() {
        return ClientUtil.registryAccess();
    }
}

