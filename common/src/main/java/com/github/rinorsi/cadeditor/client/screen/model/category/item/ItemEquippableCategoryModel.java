package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.AllowedEntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.EquipmentAssetSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.SoundEventSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ItemEquippableCategoryModel extends ItemEditorCategoryModel {
    private EquipmentSlot slot;
    private String equipSoundId;
    private String assetId;
    private String cameraOverlayId;
    private boolean dispensable;
    private boolean swappable;
    private boolean damageOnHurt;
    private boolean equipOnInteract;
    private boolean canBeSheared;
    private String shearingSoundId;
    private final List<String> initialAllowedEntities = new ArrayList<>();
    private int allowedEntitiesStartIndex = -1;

    private SoundEventSelectionEntryModel equipSoundEntry;
    private SoundEventSelectionEntryModel shearingSoundEntry;
    private EquipmentAssetSelectionEntryModel assetEntry;
    private StringEntryModel overlayEntry;

    public ItemEquippableCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("equippable_settings"), editor);
    }

    @Override
    protected void setupEntries() {
        loadStateFromStack();
        EnumEntryModel<EquipmentSlot> slotEntry = new EnumEntryModel<>(
                this,
                ModTexts.gui("equippable_slot"),
                Arrays.asList(EquipmentSlot.values()),
                slot,
                value -> slot = value == null ? EquipmentSlot.HEAD : value
        ).withTextFactory(ModTexts::equipmentSlot);
        getEntries().add(slotEntry);

        equipSoundEntry = new SoundEventSelectionEntryModel(
                this,
                ModTexts.gui("equippable_equip_sound"),
                equipSoundId,
                this::setEquipSoundId,
                namespaceFilter(equipSoundId)
        );
        getEntries().add(equipSoundEntry);

        assetEntry = new EquipmentAssetSelectionEntryModel(this, ModTexts.EQUIPMENT_ASSET, assetId, this::setAssetId);
        getEntries().add(assetEntry);

        overlayEntry = new StringEntryModel(this, ModTexts.gui("equippable_camera_overlay"), cameraOverlayId, this::setCameraOverlayId);
        overlayEntry.setPlaceholder("minecraft:spyglass_overlay");
        getEntries().add(overlayEntry);

        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("equippable_dispensable"), dispensable, value -> dispensable = value));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("equippable_swappable"), swappable, value -> swappable = value));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("equippable_damage_on_hurt"), damageOnHurt, value -> damageOnHurt = value));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("equippable_on_interact"), equipOnInteract, value -> equipOnInteract = value));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("equippable_can_be_sheared"), canBeSheared, value -> canBeSheared = value));

        shearingSoundEntry = new SoundEventSelectionEntryModel(
                this,
                ModTexts.gui("equippable_shearing_sound"),
                shearingSoundId,
                this::setShearingSoundId,
                namespaceFilter(shearingSoundId)
        );
        getEntries().add(shearingSoundEntry);

        allowedEntitiesStartIndex = getEntries().size();
        initialAllowedEntities.forEach(id -> getEntries().add(createAllowedEntityEntry(id)));
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        HolderLookup.Provider provider = ClientUtil.registryAccess();
        HolderLookup.RegistryLookup<SoundEvent> soundLookup = provider.lookup(Registries.SOUND_EVENT).orElse(null);
        HolderLookup.RegistryLookup<EntityType<?>> entityLookup = provider.lookup(Registries.ENTITY_TYPE).orElse(null);

        Optional<Holder<SoundEvent>> equipSoundHolder = resolveSound(soundLookup, equipSoundId);
        Optional<Holder<SoundEvent>> shearingSoundHolder = resolveSound(soundLookup, shearingSoundId);

        if (soundLookup == null) {
            equipSoundEntry.setValid(false);
            shearingSoundEntry.setValid(false);
            return;
        } else {
            equipSoundEntry.setValid(equipSoundHolder.isPresent() || equipSoundId.isBlank());
            shearingSoundEntry.setValid(shearingSoundHolder.isPresent() || shearingSoundId.isBlank());
        }

        List<AllowedEntityEntryModel> entityEntries = getAllowedEntityEntries();
        List<Identifier> entityLocations = new ArrayList<>();
        boolean entityValid = true;
        for (AllowedEntityEntryModel entry : entityEntries) {
            String trimmed = sanitizeId(entry.getValue());
            if (trimmed.isEmpty()) {
                entry.setValid(true);
                continue;
            }
            Identifier location = ClientUtil.parseResourceLocation(trimmed);
            if (location == null) {
                entry.setValid(false);
                entityValid = false;
            } else {
                entry.setValid(true);
                entityLocations.add(location);
            }
        }

        List<Holder<EntityType<?>>> entityHolders = new ArrayList<>();
        if (!entityLocations.isEmpty()) {
            if (entityLookup == null) {
                entityValid = false;
                entityEntries.forEach(entry -> entry.setValid(false));
            } else {
                for (int i = 0; i < entityLocations.size(); i++) {
                    Identifier id = entityLocations.get(i);
                    AllowedEntityEntryModel entry = getAllowedEntityEntry(i);
                    if (entry == null) {
                        continue;
                    }
                    ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
                    Optional<Holder.Reference<EntityType<?>>> holder = entityLookup.get(key);
                    if (holder.isPresent()) {
                        entry.setValid(true);
                        entityHolders.add(holder.get());
                    } else {
                        entry.setValid(false);
                        entityValid = false;
                    }
                }
            }
        }
        if (!entityValid) {
            return;
        }

        Equippable.Builder builder = Equippable.builder(slot);
        equipSoundHolder.ifPresent(builder::setEquipSound);

        parseEquipmentAsset().ifPresent(builder::setAsset);
        parseCameraOverlay().ifPresent(builder::setCameraOverlay);

        if (!entityHolders.isEmpty()) {
            builder.setAllowedEntities(HolderSet.direct(entityHolders));
        }

        builder.setDispensable(dispensable);
        builder.setSwappable(swappable);
        builder.setDamageOnHurt(damageOnHurt);
        builder.setEquipOnInteract(equipOnInteract);
        builder.setCanBeSheared(canBeSheared);
        shearingSoundHolder.ifPresent(builder::setShearingSound);

        stack.set(DataComponents.EQUIPPABLE, builder.build());
    }

    @Override
    public int getEntryListStart() {
        return allowedEntitiesStartIndex;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createAllowedEntityEntry("");
    }

    @Override
    protected boolean canAddEntryInList() {
        return allowedEntitiesStartIndex >= 0;
    }

    @Override
    protected net.minecraft.network.chat.MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.gui("equippable_add_allowed_entity");
    }

    private void loadStateFromStack() {
        ItemStack stack = getParent().getContext().getItemStack();
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            slot = EquipmentSlot.HEAD;
            equipSoundId = "";
            shearingSoundId = "";
            assetId = "";
            cameraOverlayId = "";
            dispensable = true;
            swappable = true;
            damageOnHurt = false;
            equipOnInteract = true;
            canBeSheared = false;
            initialAllowedEntities.clear();
            return;
        }
        slot = equippable.slot();
        equipSoundId = holderId(equippable.equipSound());
        shearingSoundId = holderId(equippable.shearingSound());
        assetId = equippable.assetId().map(ResourceKey::identifier).map(Identifier::toString).orElse("");
        cameraOverlayId = equippable.cameraOverlay().map(Identifier::toString).orElse("");
        dispensable = equippable.dispensable();
        swappable = equippable.swappable();
        damageOnHurt = equippable.damageOnHurt();
        equipOnInteract = equippable.equipOnInteract();
        canBeSheared = equippable.canBeSheared();
        initialAllowedEntities.clear();
        equippable.allowedEntities().ifPresent(set -> {
            for (Holder<EntityType<?>> holder : set) {
                holder.unwrapKey().ifPresent(key -> initialAllowedEntities.add(key.identifier().toString()));
            }
        });
    }

    private void setEquipSoundId(String id) {
        equipSoundId = sanitizeId(id);
    }

    private void setShearingSoundId(String id) {
        shearingSoundId = sanitizeId(id);
    }

    private void setAssetId(String id) {
        assetId = sanitizeId(id);
    }

    private void setCameraOverlayId(String id) {
        cameraOverlayId = sanitizeId(id);
    }

    private AllowedEntityEntryModel getAllowedEntityEntry(int index) {
        int relative = allowedEntitiesStartIndex + index;
        if (relative < 0 || relative >= getEntries().size()) {
            return null;
        }
        EntryModel entry = getEntries().get(relative);
        return entry instanceof AllowedEntityEntryModel allowed ? allowed : null;
    }

    private List<AllowedEntityEntryModel> getAllowedEntityEntries() {
        List<AllowedEntityEntryModel> entries = new ArrayList<>();
        for (EntryModel entry : getEntries()) {
            if (entry instanceof AllowedEntityEntryModel allowed) {
                entries.add(allowed);
            }
        }
        return entries;
    }

    private Optional<ResourceKey<EquipmentAsset>> parseEquipmentAsset() {
        String value = sanitizeId(assetId);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        Identifier location = ClientUtil.parseResourceLocation(value);
        return location == null
                ? Optional.empty()
                : Optional.of(ResourceKey.create(EquipmentAssets.ROOT_ID, location));
    }

    private Optional<Identifier> parseCameraOverlay() {
        String value = sanitizeId(cameraOverlayId);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ClientUtil.parseResourceLocation(value));
    }

    private static Optional<Holder<SoundEvent>> resolveSound(HolderLookup.RegistryLookup<SoundEvent> lookup, String id) {
        String value = sanitizeId(id);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (lookup == null) {
            return Optional.empty();
        }
        Identifier location = ClientUtil.parseResourceLocation(value);
        if (location == null) {
            return Optional.empty();
        }
        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, location);
        return lookup.get(key).map(holder -> (Holder<SoundEvent>) holder);
    }

    private static String holderId(Holder<SoundEvent> holder) {
        return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }

    private static String sanitizeId(String id) {
        return id == null ? "" : id.trim();
    }

    private String namespaceFilter(String id) {
        String trimmed = sanitizeId(id);
        if (trimmed.isEmpty()) {
            return null;
        }
        String namespace = trimmed.contains(":") ? trimmed.substring(0, trimmed.indexOf(':')) : "minecraft";
        return "namespace:" + namespace.toLowerCase(Locale.ROOT);
    }

    private AllowedEntityEntryModel createAllowedEntityEntry(String value) {
        return new AllowedEntityEntryModel(this, value, v -> {
        });
    }
}
