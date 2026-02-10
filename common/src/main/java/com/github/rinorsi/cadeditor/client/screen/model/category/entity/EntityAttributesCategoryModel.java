package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.DoubleEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Category exposing the base values of every attribute available on a living entity.
 */
public class EntityAttributesCategoryModel extends EntityCategoryModel {
    private static final String ATTRIBUTES_TAG = "attributes";
    private static final String ID_TAG = "id";
    private static final String LEGACY_ID_TAG = "Name";
    private static final String BASE_TAG = "base";
    private static final String LEGACY_BASE_TAG = "Base";

    private final Map<Identifier, AttributeState> attributeStates = new LinkedHashMap<>();

    public EntityAttributesCategoryModel(EntityEditorModel editor) {
        super(ModTexts.ENTITY_ATTRIBUTES, editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = ensureEntityTag();
        if (!(getEntity() instanceof LivingEntity living)) {
            return;
        }

        Map<Identifier, CompoundTag> existing = readExistingAttributes(data);
        HolderLookup.Provider registries = ClientUtil.registryAccess();
        if (registries == null) {
            existing.forEach((id, tag) -> {
                double baseValue = extractBaseValue(tag, 0d);
                AttributeState state = addOrUpdateState(null, id, tag, baseValue);
                MutableComponent label = Component.literal(id.toString());
                getEntries().add(new DoubleEntryModel(this, label, state.baseValue, value -> state.setBaseValue(value)));
            });
            return;
        }

        HolderLookup.RegistryLookup<Attribute> lookup = registries.lookupOrThrow(Registries.ATTRIBUTE);
        lookup.listElements().forEach(holder -> addAttributeEntry(holder, living, existing));
        existing.forEach((id, tag) -> {
            double baseValue = extractBaseValue(tag, 0d);
            AttributeState state = addOrUpdateState(null, id, tag, baseValue);
            MutableComponent label = Component.literal(id.toString());
            getEntries().add(new DoubleEntryModel(this, label, state.baseValue, value -> state.setBaseValue(value)));
        });
    }

    @Override
    public void apply() {
        super.apply();
        if (attributeStates.isEmpty()) {
            return;
        }

        ListTag attributes = new ListTag();
        for (AttributeState state : attributeStates.values()) {
            if (!state.shouldPersist()) {
                continue;
            }
            CompoundTag tag = state.tag.copy();
            String id = state.id.toString();
            tag.putString(ID_TAG, id);
            tag.putDouble(BASE_TAG, state.baseValue);
            if (tag.contains(LEGACY_BASE_TAG)) {
                tag.putDouble(LEGACY_BASE_TAG, state.baseValue);
            }
            if (tag.contains(LEGACY_ID_TAG) && !Objects.equals(tag.getString(LEGACY_ID_TAG), id)) {
                tag.putString(LEGACY_ID_TAG, id);
            }
            attributes.add(tag);
        }

        CompoundTag data = ensureEntityTag();
        if (attributes.isEmpty()) {
            data.remove(ATTRIBUTES_TAG);
        } else {
            data.put(ATTRIBUTES_TAG, attributes);
        }
    }

    private void addAttributeEntry(Holder.Reference<Attribute> holder, LivingEntity living, Map<Identifier, CompoundTag> existing) {
        AttributeInstance instance = living.getAttribute(holder);
        Identifier id = holder.key().identifier();
        CompoundTag backing = existing.remove(id);
        if (instance == null && backing == null) {
            return;
        }

        double defaultValue = instance != null ? instance.getBaseValue() : holder.value().getDefaultValue();
        double baseValue = extractBaseValue(backing, defaultValue);
        AttributeState state = addOrUpdateState(holder, id, backing, baseValue);
        MutableComponent label = Component.translatable(holder.value().getDescriptionId());
        getEntries().add(new DoubleEntryModel(this, label, state.baseValue, value -> state.setBaseValue(value)));
    }

    private AttributeState addOrUpdateState(Holder<Attribute> holder, Identifier id, CompoundTag backing, double baseValue) {
        AttributeState state = attributeStates.computeIfAbsent(id, key ->
                new AttributeState(key, backing == null ? new CompoundTag() : backing.copy(), baseValue, backing != null));
        state.setBaseValue(baseValue);
        return state;
    }

    private Map<Identifier, CompoundTag> readExistingAttributes(CompoundTag data) {
        Map<Identifier, CompoundTag> existing = new LinkedHashMap<>();
        if (!data.contains(ATTRIBUTES_TAG)) {
            return existing;
        }
        ListTag list = data.getList(ATTRIBUTES_TAG).orElse(null);
        if (list == null) {
            return existing;
        }
        for (Tag element : list) {
            if (!(element instanceof CompoundTag compound)) {
                continue;
            }
            String idString = compound.getString(ID_TAG).orElse("");
            if (idString.isEmpty()) {
                idString = compound.getString(LEGACY_ID_TAG).orElse("");
            }
            Identifier id = Identifier.tryParse(idString);
            if (id == null) {
                continue;
            }
            existing.putIfAbsent(id, compound);
        }
        return existing;
    }

    private double extractBaseValue(CompoundTag tag, double fallback) {
        if (tag == null) {
            return fallback;
        }
        if (tag.contains(BASE_TAG)) {
            return tag.getDouble(BASE_TAG).orElse(fallback);
        }
        if (tag.contains(LEGACY_BASE_TAG)) {
            return tag.getDouble(LEGACY_BASE_TAG).orElse(fallback);
        }
        return fallback;
    }

    private CompoundTag ensureEntityTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }

    private static boolean nearlyEquals(double a, double b) {
        return Math.abs(a - b) < 1.0E-6;
    }

    private final class AttributeState {
        private final Identifier id;
        private final CompoundTag tag;
        private final boolean hadExisting;
        private final double originalValue;
        private double baseValue;
        private boolean dirty;

        private AttributeState(Identifier id, CompoundTag tag, double baseValue, boolean hadExisting) {
            this.id = id;
            this.tag = tag;
            this.baseValue = baseValue;
            this.originalValue = baseValue;
            this.hadExisting = hadExisting;
            ensureIdentity();
        }

        private void ensureIdentity() {
            if (!tag.contains(ID_TAG)) {
                tag.putString(ID_TAG, id.toString());
            }
            if (!tag.contains(LEGACY_ID_TAG)) {
                tag.putString(LEGACY_ID_TAG, id.toString());
            }
        }

        private void setBaseValue(double value) {
            baseValue = value;
            dirty = !nearlyEquals(value, originalValue);
        }

        private boolean shouldPersist() {
            return hadExisting || dirty;
        }
    }
}
