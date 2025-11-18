package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.AttributeModifierEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
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
import java.util.UUID;

public class ItemAttributeModifiersCategoryModel extends ItemEditorCategoryModel {
    private ListTag newAttributeModifiers;

    public ItemAttributeModifiersCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ATTRIBUTE_MODIFIERS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        ItemAttributeModifiers comps = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (comps != null && !comps.modifiers().isEmpty()) {
            populateEntriesFromComponent(comps);
            return;
        }
        ListTag legacy = getTag().getList("AttributeModifiers", Tag.TAG_COMPOUND);
        if (!legacy.isEmpty()) {
            legacy.stream()
                    .map(CompoundTag.class::cast)
                    .map(this::createModifierEntry)
                    .forEach(getEntries()::add);
            return;
        }
        populateDefaultAttributeModifiers(stack);
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public int getEntryHeight() {
        return 50;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createModifierEntry(null);
    }

    private EntryModel createModifierEntry(CompoundTag tag) {
        if (tag != null) {
            String attributeName = tag.getString("AttributeName");
            String slot = tag.getString("Slot");
            int operation = tag.getInt("Operation");
            double amount = tag.getDouble("Amount");
            UUID uuid = tag.getUUID("UUID");
            return new AttributeModifierEntryModel(this, attributeName, slot, operation, amount, uuid, this::addAttributeModifier);
        }
        return new AttributeModifierEntryModel(this, this::addAttributeModifier);
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.MODIFIER;
    }

    @Override
    public void apply() {
        newAttributeModifiers = new ListTag();
        super.apply();
        // 1) Keep legacy NBT for compatibility with existing UI
        if (!newAttributeModifiers.isEmpty()) {
            getOrCreateTag().put("AttributeModifiers", newAttributeModifiers);
        } else if (getOrCreateTag().contains("AttributeModifiers")) {
            getOrCreateTag().remove("AttributeModifiers");
        }
        // 2) Apply 1.21 Data Components to the actual stack
        ItemStack stack = getParent().getContext().getItemStack();
        if (newAttributeModifiers.isEmpty()) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            return;
        }
        ItemAttributeModifiers mods = ItemAttributeModifiers.EMPTY;
        var attrLookupOpt = ClientUtil.registryAccess().lookup(Registries.ATTRIBUTE);
        if (attrLookupOpt.isPresent()) {
            var attrLookup = attrLookupOpt.get();
            for (Tag t : newAttributeModifiers) {
                if (t instanceof CompoundTag tag) {
                    String attrName = tag.getString("AttributeName");
                    String slot = tag.getString("Slot");
                    int op = tag.getInt("Operation");
                    double amount = tag.getDouble("Amount");
                    UUID uuid = tag.getUUID("UUID");
                    ResourceLocation attrRl = ResourceLocation.tryParse(attrName);
                    if (attrRl == null) continue;
                    ResourceKey<Attribute> attrKey = ResourceKey.create(Registries.ATTRIBUTE, attrRl);
                    var holderOpt = attrLookup.get(attrKey);
                    if (holderOpt.isEmpty()) continue;
                    Holder<Attribute> holder = holderOpt.get();
                    EquipmentSlotGroup group = fromSlotString(slot);
                    // Build AttributeModifier through NBT load for operation compatibility
                    CompoundTag m = new CompoundTag();
                    m.putString("id", uuid.toString());
                    m.putDouble("amount", amount);
                    m.putString("operation", fromOperationIndex(op));
                    AttributeModifier modifier = AttributeModifier.load(m);
                    if (modifier != null) {
                        mods = mods.withModifierAdded(holder, modifier, group);
                    }
                }
            }
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, mods);
        }
    }

    private void populateEntriesFromComponent(ItemAttributeModifiers modifiers) {
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            ResourceLocation attrId = entry.attribute().unwrapKey().map(key -> key.location()).orElse(null);
            if (attrId == null) continue;
            AttributeModifier modifier = entry.modifier();
            CompoundTag synthetic = modifier.save();
            UUID uuid = parseModifierUuid(synthetic);
            if (uuid == null && modifier.id() != null) {
                uuid = parseUuidString(modifier.id().toString());
            }
            if (uuid == null) {
                uuid = deterministicModifierUuid(synthetic);
            }
            getEntries().add(new AttributeModifierEntryModel(
                    this,
                    attrId.toString(),
                    toSlotString(entry.slot()),
                    modifier.operation().ordinal(),
                    modifier.amount(),
                    uuid,
                    this::addAttributeModifier
            ));
        }
    }

    @SuppressWarnings("deprecation")
    private void populateDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers defaults = stack.getItem().getDefaultAttributeModifiers();
        if (defaults == null || defaults.modifiers().isEmpty()) {
            return;
        }
        populateEntriesFromComponent(defaults);
    }

    private void addAttributeModifier(String attributeName, String slot, int operation, double amount, UUID uuid) {
        CompoundTag tag = new CompoundTag();
        tag.putString("AttributeName", attributeName);
        if (!"all".equals(slot)) {
            tag.putString("Slot", slot);
        }
        tag.putInt("Operation", operation);
        tag.putDouble("Amount", amount);
        tag.putUUID("UUID", uuid);
        newAttributeModifiers.add(tag);
    }

    private static UUID parseModifierUuid(CompoundTag tag) {
        UUID uuid = parseUuidString(tag.contains("id", Tag.TAG_STRING) ? tag.getString("id") : null);
        if (uuid != null) {
            return uuid;
        }
        if (tag.contains("id", Tag.TAG_INT_ARRAY)) {
            uuid = uuidFromIntArray(tag.getIntArray("id"));
            if (uuid != null) {
                return uuid;
            }
        }
        if (tag.contains("uuid", Tag.TAG_INT_ARRAY)) {
            uuid = uuidFromIntArray(tag.getIntArray("uuid"));
            if (uuid != null) {
                return uuid;
            }
        }
        if (tag.contains("UUID", Tag.TAG_INT_ARRAY)) {
            return uuidFromIntArray(tag.getIntArray("UUID"));
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

    private static int toOperationIndex(CompoundTag t) {
        if (t.contains("Operation", Tag.TAG_INT)) return t.getInt("Operation");
        if (t.contains("operation", Tag.TAG_STRING)) {
            return switch (t.getString("operation")) {
                case "add_value" -> 0;
                case "add_multiplied_base" -> 1;
                case "multiply_total" -> 2;
                default -> 0;
            };
        }
        return 0;
    }

    private static String fromOperationIndex(int i) {
        return switch (i) {
            case 0 -> "add_value";
            case 1 -> "add_multiplied_base";
            case 2 -> "multiply_total";
            default -> "add_value";
        };
    }
}
