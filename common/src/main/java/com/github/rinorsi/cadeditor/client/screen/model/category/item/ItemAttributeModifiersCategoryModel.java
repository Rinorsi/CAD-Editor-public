package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.InfoEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.AttributeModifierEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.client.util.NbtUuidHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ItemAttributeModifiersCategoryModel extends ItemEditorCategoryModel {
    private ListTag newAttributeModifiers;
    private final Map<UUID, ResourceLocation> modifierIds = new HashMap<>();

    public ItemAttributeModifiersCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ATTRIBUTE_MODIFIERS, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().add(new InfoEntryModel(this, ModTexts.ATTRIBUTE_TOOLTIP_INFO));
        ItemStack stack = getParent().getContext().getItemStack();
        modifierIds.clear();
        boolean hasEntries = addComponentEntries(stack.get(DataComponents.ATTRIBUTE_MODIFIERS));
        if (!hasEntries) {
            hasEntries = addLegacyEntries();
        }
        if (!hasEntries) {
            addDefaultEntries(stack);
        }
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public int getEntryHeight() {
        return 40;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createModifierEntry(null);
    }

    private EntryModel createModifierEntry(CompoundTag tag) {
        if (tag == null) {
            return new AttributeModifierEntryModel(this, this::addAttributeModifier);
        }
        String attributeName = NbtHelper.getString(tag, "AttributeName", "");
        String slot = NbtHelper.getString(tag, "Slot", "");
        int operation = NbtHelper.getInt(tag, "Operation", 0);
        double amount = NbtHelper.getDouble(tag, "Amount", 0d);
        UUID uuid = parseModifierUuid(tag);
        if (uuid == null) {
            uuid = deterministicModifierUuid(tag);
        }
        modifierIds.putIfAbsent(uuid, null);
        return new AttributeModifierEntryModel(this, attributeName, slot, operation, amount, uuid, this::addAttributeModifier);
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.MODIFIER;
    }

    @Override
    public void apply() {
        newAttributeModifiers = new ListTag();
        super.apply();
        if (!newAttributeModifiers.isEmpty()) {
            getOrCreateTag().put("AttributeModifiers", newAttributeModifiers);
        } else if (getOrCreateTag().contains("AttributeModifiers")) {
            getOrCreateTag().remove("AttributeModifiers");
        }
        ItemStack stack = getParent().getContext().getItemStack();
        var attrLookupOpt = ClientUtil.registryAccess().lookup(Registries.ATTRIBUTE);
        if (attrLookupOpt.isEmpty()) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            return;
        }
        HolderLookup.RegistryLookup<Attribute> attrLookup = attrLookupOpt.get();
        List<ItemAttributeModifiers.Entry> componentEntries = new ArrayList<>();
        for (Tag t : newAttributeModifiers) {
            if (!(t instanceof CompoundTag tag)) {
                continue;
            }
            String attrName = NbtHelper.getString(tag, "AttributeName", "");
            ResourceLocation attrRl = ResourceLocation.tryParse(attrName);
            if (attrRl == null) continue;
            ResourceKey<Attribute> attrKey = ResourceKey.create(Registries.ATTRIBUTE, attrRl);
            var holderOpt = attrLookup.get(attrKey);
            if (holderOpt.isEmpty()) continue;
            Holder<Attribute> holder = holderOpt.get();
            String slotName = NbtHelper.getString(tag, "Slot", "");
            EquipmentSlotGroup group = fromSlotString(slotName.isEmpty() ? "all" : slotName);
            int op = NbtHelper.getInt(tag, "Operation", 0);
            double amount = NbtHelper.getDouble(tag, "Amount", 0d);
            UUID uuid = parseModifierUuid(tag);
            if (uuid == null) {
                uuid = deterministicModifierUuid(tag);
            }
            AttributeModifier.Operation operation = operationFromIndex(op);
            ResourceLocation modifierId = resolveModifierId(uuid);
            AttributeModifier modifier = new AttributeModifier(modifierId, amount, operation);
            modifierIds.put(uuid, modifierId);
            componentEntries.add(new ItemAttributeModifiers.Entry(holder, modifier, group));
        }
        if (componentEntries.isEmpty()) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        } else {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(componentEntries));
        }
    }

    private void addAttributeModifier(String attributeName, String slot, int operation, double amount, UUID uuid) {
        CompoundTag tag = new CompoundTag();
        tag.putString("AttributeName", attributeName);
        if (!"all".equals(slot)) {
            tag.putString("Slot", slot);
        }
        tag.putInt("Operation", operation);
        tag.putDouble("Amount", amount);
        NbtUuidHelper.putUuid(tag, "UUID", uuid);
        modifierIds.putIfAbsent(uuid, null);
        newAttributeModifiers.add(tag);
    }

    private static UUID parseModifierUuid(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        UUID uuid = parseUuidString(tag.getString("UUID").orElse(null));
        if (uuid != null) {
            return uuid;
        }
        uuid = parseUuidString(tag.getString("id").orElse(null));
        if (uuid != null) return uuid;
        uuid = uuidFromIntArray(NbtHelper.getIntArray(tag, "id"));
        if (uuid != null) return uuid;
        uuid = uuidFromIntArray(NbtHelper.getIntArray(tag, "uuid"));
        if (uuid != null) return uuid;
        uuid = uuidFromIntArray(NbtHelper.getIntArray(tag, "UUID"));
        return uuid;
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

    private static UUID deterministicModifierUuid(CompoundTag tag) {
        CompoundTag copy = tag.copy();
        copy.remove("id");
        copy.remove("UUID");
        copy.remove("uuid");
        return UUID.nameUUIDFromBytes(copy.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String toSlotString(EquipmentSlotGroup g) {
        if (g == EquipmentSlotGroup.MAINHAND) return "mainhand";
        if (g == EquipmentSlotGroup.OFFHAND) return "offhand";
        if (g == EquipmentSlotGroup.FEET) return "feet";
        if (g == EquipmentSlotGroup.LEGS) return "legs";
        if (g == EquipmentSlotGroup.CHEST) return "chest";
        if (g == EquipmentSlotGroup.HEAD) return "head";
        if (g == EquipmentSlotGroup.HAND) return "hand";
        if (g == EquipmentSlotGroup.ARMOR) return "armor";
        if (g == EquipmentSlotGroup.BODY) return "body";
        return "all";
    }

    private static EquipmentSlotGroup fromSlotString(String s) {
        return switch (s) {
            case "mainhand" -> EquipmentSlotGroup.MAINHAND;
            case "offhand" -> EquipmentSlotGroup.OFFHAND;
            case "feet" -> EquipmentSlotGroup.FEET;
            case "legs" -> EquipmentSlotGroup.LEGS;
            case "chest" -> EquipmentSlotGroup.CHEST;
            case "head" -> EquipmentSlotGroup.HEAD;
            case "hand" -> EquipmentSlotGroup.HAND;
            case "armor" -> EquipmentSlotGroup.ARMOR;
            case "body" -> EquipmentSlotGroup.BODY;
            default -> EquipmentSlotGroup.ANY;
        };
    }

    private static UUID uuidFromResourceLocation(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(("rl:" + id).getBytes(StandardCharsets.UTF_8));
    }

    private ResourceLocation resolveModifierId(UUID uuid) {
        ResourceLocation existing = modifierIds.get(uuid);
        if (existing != null) {
            return existing;
        }
        ResourceLocation generated = ResourceLocation.fromNamespaceAndPath("cadeditor", uuid.toString());
        modifierIds.put(uuid, generated);
        return generated;
    }

    private static AttributeModifier.Operation operationFromIndex(int index) {
        AttributeModifier.Operation[] values = AttributeModifier.Operation.values();
        if (index < 0 || index >= values.length) {
            return values[0];
        }
        return values[index];
    }

    private static int operationToIndex(AttributeModifier.Operation operation) {
        return operation.ordinal();
    }

    private boolean addComponentEntries(ItemAttributeModifiers comps) {
        if (comps == null) {
            return false;
        }
        if (comps.modifiers().isEmpty()) {
            // Explicit override with no modifiers; make sure defaults are not reintroduced
            return true;
        }
        boolean added = false;
        Set<String> seen = new HashSet<>();
        for (ItemAttributeModifiers.Entry entry : comps.modifiers()) {
            added |= addModifierEntry(entry, seen);
        }
        return added;
    }

    private boolean addModifierEntry(ItemAttributeModifiers.Entry entry, Set<String> seen) {
        String attrName = entry.attribute().unwrapKey().map(k -> k.location().toString()).orElse("");
        AttributeModifier modifier = entry.modifier();
        UUID uuid = uuidFromResourceLocation(modifier.id());
        modifierIds.put(uuid, modifier.id());
        String slot = toSlotString(entry.slot());
        int opIndex = operationToIndex(modifier.operation());
        double amount = modifier.amount();
        String key = attrName + "|" + uuid + "|" + amount + "|" + opIndex + "|" + slot;
        if (!seen.add(key)) {
            return false;
        }
        getEntries().add(new AttributeModifierEntryModel(this, attrName, slot, opIndex, amount, uuid, this::addAttributeModifier));
        return true;
    }

    private boolean addLegacyEntries() {
        CompoundTag root = getTag();
        ListTag legacyList = root == null ? new ListTag() : NbtHelper.getListOrEmpty(root, "AttributeModifiers");
        boolean added = false;
        for (Tag tag : legacyList) {
            if (tag instanceof CompoundTag compound) {
                getEntries().add(createModifierEntry(compound));
                added = true;
            }
        }
        return added;
    }

    private void addDefaultEntries(ItemStack stack) {
        ItemAttributeModifiers defaults = stack.getItem().components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (defaults != null && !defaults.modifiers().isEmpty()) {
            addComponentEntries(defaults);
        }
    }
}
