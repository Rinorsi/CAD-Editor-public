package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringWithActionsEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.SoundEventSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;

/**
 * Handles equipment-related traits such as durability tweaks, fire resistance, enchantability, etc.
 */
public class ItemEquipmentTraitsCategoryModel extends ItemEditorCategoryModel {
    private static final DamageResistant FIRE_DAMAGE_RESISTANCE = new DamageResistant(DamageTypeTags.IS_FIRE);

    private BooleanEntryModel enchantableToggleEntry;
    private IntegerEntryModel enchantableValueEntry;
    private boolean enchantableEnabled;
    private int enchantableValue;

    private StringWithActionsEntryModel bypassedByEntry;
    private StringWithActionsEntryModel damageTypeEntry;

    private BooleanEntryModel repairableToggleEntry;
    private StringWithActionsEntryModel repairableItemsEntry;
    private boolean repairableEnabled;
    private String repairableItemsRaw;

    private BooleanEntryModel useCooldownToggleEntry;
    private StringEntryModel useCooldownGroupEntry;
    private FloatEntryModel useCooldownSecondsEntry;
    private boolean useCooldownEnabled;
    private String useCooldownGroupId;
    private float useCooldownSeconds;

    private BooleanEntryModel weaponToggleEntry;
    private IntegerEntryModel weaponDamageEntry;
    private FloatEntryModel weaponDisableEntry;
    private boolean weaponEnabled;
    private int weaponDamagePerAttack;
    private float weaponDisableSeconds;

    private BooleanEntryModel blocksAttacksToggleEntry;
    private FloatEntryModel blockDelayEntry;
    private FloatEntryModel disableCooldownScaleEntry;
    private FloatEntryModel damageBaseEntry;
    private FloatEntryModel damageFactorEntry;
    private FloatEntryModel damageAngleEntry;
    private FloatEntryModel itemDamageBaseEntry;
    private FloatEntryModel itemDamageFactorEntry;
    private FloatEntryModel itemDamageThresholdEntry;
    private SoundEventSelectionEntryModel blockSoundEntry;
    private SoundEventSelectionEntryModel disableSoundEntry;
    private boolean blocksAttacksEnabled;
    private float blockDelaySeconds;
    private float disableCooldownScale;
    private String bypassedByTag;
    private String damageReductionTypeTag;
    private float damageReductionBase;
    private float damageReductionFactor;
    private float damageReductionAngle;
    private float itemDamageBase;
    private float itemDamageFactor;
    private float itemDamageThreshold;
    private String blockSoundId;
    private String disableSoundId;

    public ItemEquipmentTraitsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("equipment_traits"), editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        int currentDamage = stack.isDamageableItem() ? stack.getDamageValue() : 0;
        boolean isUnbreakable = stack.has(DataComponents.UNBREAKABLE);

        getEntries().add(new IntegerEntryModel(this, ModTexts.DAMAGE, currentDamage, this::setDamage));
        getEntries().add(new IntegerEntryModel(this, ModTexts.MAX_DAMAGE, getMaxDamageValue(stack), this::setMaxDamage, value -> value >= 0));
        getEntries().add(new BooleanEntryModel(this, ModTexts.UNBREAKABLE, isUnbreakable, this::setUnbreakable));
        getEntries().add(new IntegerEntryModel(this, ModTexts.gui("repair_cost"), getRepairCost(stack), this::setRepairCost));
        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("glint_override"), getGlintOverride(stack), this::setGlintOverride));
        getEntries().add(new BooleanEntryModel(this, ModTexts.FIRE_RESISTANT, isFireResistant(stack), this::setFireResistant));
        getEntries().add(new BooleanEntryModel(this, ModTexts.INTANGIBLE_PROJECTILE, stack.has(DataComponents.INTANGIBLE_PROJECTILE), this::setIntangibleProjectile));

        // Repairable
        Repairable repairable = stack.get(DataComponents.REPAIRABLE);
        Repairable baseRepairable = stack.getItem().components().get(DataComponents.REPAIRABLE);
        repairableEnabled = repairable != null;
        repairableItemsRaw = repairable != null
                ? serializeRepairableItems(repairable)
                : baseRepairable != null ? serializeRepairableItems(baseRepairable) : "";
        repairableToggleEntry = new BooleanEntryModel(this, ModTexts.gui("repairable_enabled"), repairableEnabled, this::setRepairableEnabled);
        getEntries().add(repairableToggleEntry);
        repairableItemsEntry = new StringWithActionsEntryModel(this, ModTexts.gui("repairable_items"), repairableItemsRaw, this::setRepairableItems);
        repairableItemsEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_item"), this::openRepairableItemSelection));
        repairableItemsEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_tag"), this::openRepairableTagSelection));
        if (repairableEnabled) {
            insertAfter(repairableToggleEntry, repairableItemsEntry);
        }

        // Use cooldown
        net.minecraft.world.item.component.UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
        useCooldownEnabled = cooldown != null;
        useCooldownGroupId = cooldown != null && cooldown.cooldownGroup().isPresent()
                ? cooldown.cooldownGroup().get().toString()
                : "";
        useCooldownSeconds = cooldown != null ? cooldown.seconds() : 0f;
        useCooldownToggleEntry = new BooleanEntryModel(this, ModTexts.gui("use_cooldown_enabled"), useCooldownEnabled, this::setUseCooldownEnabled);
        getEntries().add(useCooldownToggleEntry);
        useCooldownGroupEntry = new StringEntryModel(this, ModTexts.gui("use_cooldown_group"), useCooldownGroupId, this::setUseCooldownGroupId);
        useCooldownGroupEntry.setPlaceholder("namespace:id (可留空)");
        useCooldownSecondsEntry = new FloatEntryModel(this, ModTexts.gui("use_cooldown_seconds"), useCooldownSeconds, this::setUseCooldownSeconds, v -> v != null && v > 0f);
        if (useCooldownEnabled) {
            insertAfter(useCooldownToggleEntry, useCooldownGroupEntry, useCooldownSecondsEntry);
        }

        // Weapon
        net.minecraft.world.item.component.Weapon weapon = stack.get(DataComponents.WEAPON);
        weaponEnabled = weapon != null;
        weaponDamagePerAttack = weapon != null ? weapon.itemDamagePerAttack() : 1;
        weaponDisableSeconds = weapon != null ? weapon.disableBlockingForSeconds() : 0f;
        weaponToggleEntry = new BooleanEntryModel(this, ModTexts.gui("weapon_enabled"), weaponEnabled, this::setWeaponEnabled);
        getEntries().add(weaponToggleEntry);
        weaponDamageEntry = new IntegerEntryModel(this, ModTexts.gui("weapon_item_damage"), weaponDamagePerAttack, this::setWeaponDamagePerAttack, value -> value != null && value >= 0);
        weaponDisableEntry = new FloatEntryModel(this, ModTexts.gui("weapon_disable_blocking"), weaponDisableSeconds, this::setWeaponDisableSeconds, value -> value != null && value >= 0f);
        if (weaponEnabled) {
            insertAfter(weaponToggleEntry, weaponDamageEntry, weaponDisableEntry);
        }

        // Blocks attacks
        BlocksAttacks blocksAttacks = stack.get(DataComponents.BLOCKS_ATTACKS);
        blocksAttacksEnabled = blocksAttacks != null;
        blockDelaySeconds = blocksAttacks != null ? blocksAttacks.blockDelaySeconds() : 0f;
        disableCooldownScale = blocksAttacks != null ? blocksAttacks.disableCooldownScale() : 1f;
        bypassedByTag = blocksAttacks != null && blocksAttacks.bypassedBy().isPresent()
                ? "#" + blocksAttacks.bypassedBy().get().location()
                : "";
        List<DamageReduction> reductions = blocksAttacks != null ? blocksAttacks.damageReductions() : List.of();
        DamageReduction reduction = reductions.isEmpty() ? null : reductions.get(0);
        damageReductionAngle = reduction != null ? reduction.horizontalBlockingAngle() : 90f;
        damageReductionBase = reduction != null ? reduction.base() : 0f;
        damageReductionFactor = reduction != null ? reduction.factor() : 0f;
        damageReductionTypeTag = "";
        if (reduction != null) {
            var typeSet = reduction.type().orElse(null);
            if (typeSet instanceof HolderSet.Named<DamageType> named) {
                damageReductionTypeTag = "#" + named.key().location();
            }
        }
        ItemDamageFunction itemDamageFunction = blocksAttacks != null ? blocksAttacks.itemDamage() : null;
        itemDamageThreshold = itemDamageFunction != null ? itemDamageFunction.threshold() : 0f;
        itemDamageBase = itemDamageFunction != null ? itemDamageFunction.base() : 1f;
        itemDamageFactor = itemDamageFunction != null ? itemDamageFunction.factor() : 0f;
        blockSoundId = blocksAttacks != null && blocksAttacks.blockSound().isPresent()
                ? blocksAttacks.blockSound().get().unwrapKey().map(holder -> holder.location().toString()).orElse("")
                : "";
        disableSoundId = blocksAttacks != null && blocksAttacks.disableSound().isPresent()
                ? blocksAttacks.disableSound().get().unwrapKey().map(holder -> holder.location().toString()).orElse("")
                : "";
        blocksAttacksToggleEntry = new BooleanEntryModel(this, ModTexts.gui("blocks_attacks_enabled"), blocksAttacksEnabled, this::setBlocksAttacksEnabled);
        getEntries().add(blocksAttacksToggleEntry);
        blockDelayEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_block_delay"), blockDelaySeconds, this::setBlockDelaySeconds, value -> value != null && value >= 0f);
        disableCooldownScaleEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_disable_scale"), disableCooldownScale, this::setDisableCooldownScale, value -> value != null && value >= 0f);
        bypassedByEntry = new StringWithActionsEntryModel(this, ModTexts.gui("blocks_attacks_bypassed_by"), bypassedByTag, this::setBypassedByTag);
        bypassedByEntry.setPlaceholder("#minecraft:bypasses_shield");
        bypassedByEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_tag"), this::openBypassedTagSelection));
        damageTypeEntry = new StringWithActionsEntryModel(this, ModTexts.gui("blocks_attacks_damage_type"), damageReductionTypeTag, this::setDamageReductionTypeTag);
        damageTypeEntry.setPlaceholder("#minecraft:is_projectile");
        damageTypeEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_tag"), this::openDamageTypeTagSelection));
        damageBaseEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_damage_base"), damageReductionBase, this::setDamageReductionBase);
        damageFactorEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_damage_factor"), damageReductionFactor, this::setDamageReductionFactor);
        damageAngleEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_damage_angle"), damageReductionAngle, this::setDamageReductionAngle, value -> value != null && value > 0f);
        itemDamageBaseEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_item_damage_base"), itemDamageBase, this::setItemDamageBase);
        itemDamageFactorEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_item_damage_factor"), itemDamageFactor, this::setItemDamageFactor);
        itemDamageThresholdEntry = new FloatEntryModel(this, ModTexts.gui("blocks_attacks_item_damage_threshold"), itemDamageThreshold, this::setItemDamageThreshold, value -> value != null && value >= 0f);
        blockSoundEntry = new SoundEventSelectionEntryModel(this, ModTexts.gui("blocks_attacks_block_sound"), blockSoundId, this::setBlockSoundId, namespaceFilter(blockSoundId));
        disableSoundEntry = new SoundEventSelectionEntryModel(this, ModTexts.gui("blocks_attacks_disabled_sound"), disableSoundId, this::setDisableSoundId, namespaceFilter(disableSoundId));
        if (blocksAttacksEnabled) {
            insertAfter(
                    blocksAttacksToggleEntry,
                    blockDelayEntry,
                    disableCooldownScaleEntry,
                    bypassedByEntry,
                    damageTypeEntry,
                    damageBaseEntry,
                    damageFactorEntry,
                    damageAngleEntry,
                    blockSoundEntry,
                    disableSoundEntry,
                    itemDamageBaseEntry,
                    itemDamageFactorEntry,
                    itemDamageThresholdEntry
            );
        }

        // Enchantable
        Enchantable appliedEnchantable = stack.get(DataComponents.ENCHANTABLE);
        Enchantable baseEnchantable = stack.getItem().components().get(DataComponents.ENCHANTABLE);
        enchantableEnabled = appliedEnchantable != null;
        enchantableValue = appliedEnchantable != null
                ? appliedEnchantable.value()
                : baseEnchantable != null ? baseEnchantable.value() : 1;
        if (enchantableValue < 1) enchantableValue = 1;

        enchantableToggleEntry = new BooleanEntryModel(this, ModTexts.gui("enchantable_enabled"), enchantableEnabled, this::setEnchantableEnabled);
        getEntries().add(enchantableToggleEntry);
        enchantableValueEntry = new IntegerEntryModel(
                this,
                ModTexts.gui("enchantable_value"),
                enchantableValue,
                this::setEnchantableValue,
                value -> value != null && value >= 1
        );
        if (enchantableEnabled) {
            insertEnchantableValueEntry();
        }

        getEntries().add(new BooleanEntryModel(this, ModTexts.gui("glider_enabled"), stack.has(DataComponents.GLIDER), this::setGliderEnabled));
    }

    private void setDamage(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!stack.isDamageableItem()) {
            if (value > 0) {
                stack.set(DataComponents.DAMAGE, value);
            } else {
                stack.remove(DataComponents.DAMAGE);
            }
        } else {
            int max = stack.getMaxDamage();
            int clamped = Math.max(0, Math.min(value, Math.max(0, max - 1)));
            stack.setDamageValue(clamped);
            if (clamped == 0) {
                stack.remove(DataComponents.DAMAGE);
            }
        }
    }

    private int getMaxDamageValue(ItemStack stack) {
        Integer override = stack.get(DataComponents.MAX_DAMAGE);
        if (override != null) {
            return override;
        }
        return stack.isDamageableItem() ? stack.getMaxDamage() : 0;
    }

    private void setMaxDamage(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value > 0) {
            stack.set(DataComponents.MAX_DAMAGE, value);
        } else {
            stack.remove(DataComponents.MAX_DAMAGE);
        }
    }

    private void setUnbreakable(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.UNBREAKABLE);
        }
    }

    private int getRepairCost(ItemStack stack) {
        Integer v = stack.get(DataComponents.REPAIR_COST);
        return v != null ? v : 0;
    }

    private void setRepairCost(int value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value > 0) {
            stack.set(DataComponents.REPAIR_COST, value);
        } else {
            stack.remove(DataComponents.REPAIR_COST);
        }
    }

    private boolean getGlintOverride(ItemStack stack) {
        Boolean b = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return b != null && b;
    }

    private void setGlintOverride(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        else stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private boolean isFireResistant(ItemStack stack) {
        DamageResistant resistant = stack.get(DataComponents.DAMAGE_RESISTANT);
        return resistant != null && resistant.types().equals(DamageTypeTags.IS_FIRE);
    }

    private void setFireResistant(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.DAMAGE_RESISTANT, FIRE_DAMAGE_RESISTANCE);
        } else {
            DamageResistant existing = stack.get(DataComponents.DAMAGE_RESISTANT);
            if (existing != null && existing.types().equals(DamageTypeTags.IS_FIRE)) {
                stack.remove(DataComponents.DAMAGE_RESISTANT);
            }
        }
    }

    private void setIntangibleProjectile(boolean value) {
        ItemEditorModel parent = getParent();
        ItemStack stack = parent.getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.INTANGIBLE_PROJECTILE);
        }
        syncIntangibleProjectileComponent(parent, value);
    }

    private void syncIntangibleProjectileComponent(ItemEditorModel parent, boolean enabled) {
        var context = parent.getContext();
        CompoundTag root = context.getTag();
        if (root == null) {
            return;
        }
        CompoundTag components = root.getCompound("components").orElse(null);
        if (enabled) {
            if (components == null) {
                components = new CompoundTag();
                root.put("components", components);
            }
            components.put("minecraft:intangible_projectile", new CompoundTag());
            components.remove("!minecraft:intangible_projectile");
        } else if (components != null) {
            components.remove("minecraft:intangible_projectile");
            components.remove("!minecraft:intangible_projectile");
            if (components.isEmpty()) {
                root.remove("components");
            }
        }
        context.setTag(root);
    }

    private void setEnchantableEnabled(boolean value) {
        enchantableEnabled = value;
        updateEnchantableValueEntry();
        applyEnchantableComponent();
    }

    private void setEnchantableValue(Integer value) {
        enchantableValue = value == null ? 1 : Math.max(1, value);
        if (enchantableEnabled) {
            applyEnchantableComponent();
        }
    }

    private void updateEnchantableValueEntry() {
        if (enchantableEnabled) {
            insertEnchantableValueEntry();
        } else {
            getEntries().remove(enchantableValueEntry);
        }
    }

    private void insertEnchantableValueEntry() {
        if (getEntries().contains(enchantableValueEntry)) {
            return;
        }
        int toggleIndex = getEntries().indexOf(enchantableToggleEntry);
        if (toggleIndex < 0) {
            getEntries().add(enchantableValueEntry);
        } else {
            getEntries().add(toggleIndex + 1, enchantableValueEntry);
        }
    }

    private void applyEnchantableComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (enchantableEnabled) {
            try {
                stack.set(DataComponents.ENCHANTABLE, new Enchantable(enchantableValue));
            } catch (IllegalArgumentException e) {
                enchantableValue = Math.max(1, enchantableValue);
                stack.set(DataComponents.ENCHANTABLE, new Enchantable(enchantableValue));
            }
        } else {
            stack.remove(DataComponents.ENCHANTABLE);
            getParent().removeComponentFromDataTag("minecraft:enchantable");
        }
    }

    private void setGliderEnabled(boolean value) {
        ItemStack stack = getParent().getContext().getItemStack();
        if (value) {
            stack.set(DataComponents.GLIDER, Unit.INSTANCE);
        } else {
            stack.remove(DataComponents.GLIDER);
            getParent().removeComponentFromDataTag("minecraft:glider");
        }
    }

    private void setRepairableEnabled(boolean value) {
        repairableEnabled = value;
        if (value) {
            insertAfter(repairableToggleEntry, repairableItemsEntry);
        } else {
            removeEntries(repairableItemsEntry);
        }
        applyRepairableComponent();
    }

    private void setRepairableItems(String value) {
        repairableItemsRaw = value == null ? "" : value.trim();
        if (repairableEnabled) {
            applyRepairableComponent();
        }
    }

    private void setUseCooldownEnabled(boolean value) {
        useCooldownEnabled = value;
        if (value) {
            insertAfter(useCooldownToggleEntry, useCooldownGroupEntry, useCooldownSecondsEntry);
        } else {
            removeEntries(useCooldownGroupEntry, useCooldownSecondsEntry);
        }
        applyUseCooldownComponent();
    }

    private void setUseCooldownGroupId(String value) {
        useCooldownGroupId = value == null ? "" : value.trim();
        if (useCooldownEnabled) {
            applyUseCooldownComponent();
        }
    }

    private void setUseCooldownSeconds(Float value) {
        useCooldownSeconds = value == null ? 0f : Math.max(0f, value);
        if (useCooldownEnabled) {
            applyUseCooldownComponent();
        }
    }

    private void setWeaponEnabled(boolean value) {
        weaponEnabled = value;
        if (value) {
            insertAfter(weaponToggleEntry, weaponDamageEntry, weaponDisableEntry);
        } else {
            removeEntries(weaponDamageEntry, weaponDisableEntry);
        }
        applyWeaponComponent();
    }

    private void setWeaponDamagePerAttack(Integer value) {
        weaponDamagePerAttack = value == null ? 1 : Math.max(0, value);
        if (weaponEnabled) {
            applyWeaponComponent();
        }
    }

    private void setWeaponDisableSeconds(Float value) {
        weaponDisableSeconds = value == null ? 0f : Math.max(0f, value);
        if (weaponEnabled) {
            applyWeaponComponent();
        }
    }

    private void setBlocksAttacksEnabled(boolean value) {
        blocksAttacksEnabled = value;
        if (value) {
            insertAfter(
                    blocksAttacksToggleEntry,
                    blockDelayEntry,
                    disableCooldownScaleEntry,
                    bypassedByEntry,
                    damageTypeEntry,
                    damageBaseEntry,
                    damageFactorEntry,
                    damageAngleEntry,
                    blockSoundEntry,
                    disableSoundEntry,
                    itemDamageBaseEntry,
                    itemDamageFactorEntry,
                    itemDamageThresholdEntry
            );
        } else {
            removeEntries(
                    blockDelayEntry,
                    disableCooldownScaleEntry,
                    bypassedByEntry,
                    damageTypeEntry,
                    damageBaseEntry,
                    damageFactorEntry,
                    damageAngleEntry,
                    blockSoundEntry,
                    disableSoundEntry,
                    itemDamageBaseEntry,
                    itemDamageFactorEntry,
                    itemDamageThresholdEntry
            );
        }
        applyBlocksAttacksComponent();
    }

    private void setBlockDelaySeconds(Float value) {
        blockDelaySeconds = value == null ? 0f : Math.max(0f, value);
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDisableCooldownScale(Float value) {
        disableCooldownScale = value == null ? 1f : Math.max(0f, value);
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setBypassedByTag(String value) {
        bypassedByTag = value == null ? "" : value.trim();
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDamageReductionTypeTag(String value) {
        damageReductionTypeTag = value == null ? "" : value.trim();
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDamageReductionBase(Float value) {
        damageReductionBase = value == null ? 0f : value;
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDamageReductionFactor(Float value) {
        damageReductionFactor = value == null ? 0f : value;
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDamageReductionAngle(Float value) {
        damageReductionAngle = value == null ? 90f : Math.max(0.1f, value);
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setItemDamageBase(Float value) {
        itemDamageBase = value == null ? 1f : value;
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setItemDamageFactor(Float value) {
        itemDamageFactor = value == null ? 0f : value;
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setItemDamageThreshold(Float value) {
        itemDamageThreshold = value == null ? 0f : Math.max(0f, value);
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setBlockSoundId(String id) {
        blockSoundId = sanitizeId(id);
        if (blockSoundEntry != null) {
            blockSoundEntry.setValid(blockSoundId.isBlank() || resolveSoundHolder(blockSoundId).isPresent());
        }
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void setDisableSoundId(String id) {
        disableSoundId = sanitizeId(id);
        if (disableSoundEntry != null) {
            disableSoundEntry.setValid(disableSoundId.isBlank() || resolveSoundHolder(disableSoundId).isPresent());
        }
        if (blocksAttacksEnabled) {
            applyBlocksAttacksComponent();
        }
    }

    private void openRepairableItemSelection() {
        Set<ResourceLocation> initiallySelected = extractItemIds(repairableItemsRaw);
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_item"),
                "repairable_items",
                ClientCache.getItemSelectionItems(),
                null,
                true,
                selected -> {
                    List<String> entries = new ArrayList<>();
                    for (String entry : parseIdentifierList(repairableItemsRaw)) {
                        if (entry.startsWith("#")) {
                            entries.add(entry);
                        }
                    }
                    for (ResourceLocation rl : selected) {
                        entries.add(rl.toString());
                    }
                    String joined = String.join(", ", entries).trim();
                    repairableItemsRaw = joined;
                    repairableItemsEntry.setValue(joined);
                    applyRepairableComponent();
                },
                initiallySelected
        );
    }

    private void openRepairableTagSelection() {
        Set<ResourceLocation> initiallySelected = extractTagIds(repairableItemsRaw);
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_tag"),
                "repairable_tags",
                ClientCache.getItemTagSelectionItems(),
                null,
                true,
                selected -> {
                    List<String> entries = new ArrayList<>();
                    for (String entry : parseIdentifierList(repairableItemsRaw)) {
                        if (!entry.startsWith("#")) {
                            entries.add(entry);
                        }
                    }
                    for (ResourceLocation rl : selected) {
                        entries.add("#" + rl);
                    }
                    String joined = String.join(", ", entries).trim();
                    repairableItemsRaw = joined;
                    repairableItemsEntry.setValue(joined);
                    applyRepairableComponent();
                },
                initiallySelected
        );
    }

    private void openBypassedTagSelection() {
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_tag"),
                "blocks_attacks_bypass",
                ClientCache.getDamageTypeTagSelectionItems(),
                value -> {
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    bypassedByTag = "#" + value;
                    bypassedByEntry.setValue(bypassedByTag);
                    if (blocksAttacksEnabled) {
                        applyBlocksAttacksComponent();
                    }
                }
        );
    }

    private void openDamageTypeTagSelection() {
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_tag"),
                "blocks_attacks_damage_type",
                ClientCache.getDamageTypeTagSelectionItems(),
                value -> {
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    damageReductionTypeTag = "#" + value;
                    damageTypeEntry.setValue(damageReductionTypeTag);
                    if (blocksAttacksEnabled) {
                        applyBlocksAttacksComponent();
                    }
                }
        );
    }

    private void applyRepairableComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!repairableEnabled || repairableItemsRaw.isBlank()) {
            repairableItemsEntry.setValid(true);
            stack.remove(DataComponents.REPAIRABLE);
            getParent().removeComponentFromDataTag("minecraft:repairable");
            DebugLog.info(() -> "[Repairable] Disabled or empty input, removing component");
            return;
        }

        List<String> entries = parseIdentifierList(repairableItemsRaw);
        if (entries.isEmpty()) {
            repairableItemsEntry.setValid(false);
            DebugLog.info(() -> "[Repairable] Parsed entries empty from raw: " + repairableItemsRaw);
            return;
        }

        List<String> tagIds = new ArrayList<>();
        List<String> itemIds = new ArrayList<>();
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                String tagId = entry.substring(1);
                if (!tagId.isBlank()) {
                    tagIds.add(tagId);
                }
            } else if (!entry.isBlank()) {
                itemIds.add(entry);
            }
        }

        if (itemIds.isEmpty() && tagIds.size() == 1) {
            Optional<HolderSet<Item>> namedSet = resolveNamedItemTag(tagIds.get(0));
            if (namedSet.isPresent()) {
                repairableItemsEntry.setValid(true);
                final String tagName = tagIds.get(0);
                DebugLog.info(() -> "[Repairable] Applying named tag #" + tagName);
                stack.set(DataComponents.REPAIRABLE, new Repairable(namedSet.get()));
                return;
            }
            DebugLog.info(() -> "[Repairable] Unable to resolve tag #" + tagIds.get(0));
        }

        List<Holder<Item>> holders = resolveItemHolders(entries);
        if (holders.isEmpty()) {
            repairableItemsEntry.setValid(false);
            DebugLog.info(() -> "[Repairable] No holders matched entries: " + entries);
            return;
        }
        repairableItemsEntry.setValid(true);
        DebugLog.info(() -> "[Repairable] Applying explicit items: " + holders.stream()
                .map(holder -> holder.unwrapKey().map(key -> key.location().toString()).orElse("?"))
                .toList());
        stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(holders)));
    }

    private void applyUseCooldownComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!useCooldownEnabled || useCooldownSeconds <= 0f) {
            stack.remove(DataComponents.USE_COOLDOWN);
            getParent().removeComponentFromDataTag("minecraft:use_cooldown");
            return;
        }
        Optional<ResourceLocation> group = Optional.empty();
        String sanitized = sanitizeId(useCooldownGroupId);
        if (!sanitized.isBlank()) {
            ResourceLocation parsed = tryParse(sanitized);
            if (parsed != null) {
                group = Optional.of(parsed);
            }
        }
        UseCooldown component = group.isPresent()
                ? new UseCooldown(useCooldownSeconds, group)
                : new UseCooldown(useCooldownSeconds);
        stack.set(DataComponents.USE_COOLDOWN, component);
    }

    private void applyWeaponComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!weaponEnabled) {
            stack.remove(DataComponents.WEAPON);
            getParent().removeComponentFromDataTag("minecraft:weapon");
            return;
        }
        int damage = Math.max(0, weaponDamagePerAttack);
        float disable = Math.max(0f, weaponDisableSeconds);
        stack.set(DataComponents.WEAPON, new Weapon(damage, disable));
    }

    private void applyBlocksAttacksComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!blocksAttacksEnabled) {
            stack.remove(DataComponents.BLOCKS_ATTACKS);
            getParent().removeComponentFromDataTag("minecraft:blocks_attacks");
            return;
        }
        List<DamageReduction> reductions = new ArrayList<>();
        Optional<HolderSet<DamageType>> typeSet = parseDamageTypeHolderSet(damageReductionTypeTag);
        if (typeSet.isPresent() || damageReductionBase != 0f || damageReductionFactor != 0f || damageReductionAngle > 0f) {
            reductions.add(new DamageReduction(
                    Math.max(0.1f, damageReductionAngle),
                    typeSet,
                    damageReductionBase,
                    damageReductionFactor
            ));
        }
        ItemDamageFunction itemDamage = new ItemDamageFunction(
                Math.max(0f, itemDamageThreshold),
                itemDamageBase,
                itemDamageFactor
        );
        BlocksAttacks component = new BlocksAttacks(
                Math.max(0f, blockDelaySeconds),
                Math.max(0f, disableCooldownScale),
                reductions.isEmpty() ? List.of() : List.copyOf(reductions),
                itemDamage,
                parseDamageTypeTag(bypassedByTag),
                resolveSoundHolder(blockSoundId),
                resolveSoundHolder(disableSoundId)
        );
        stack.set(DataComponents.BLOCKS_ATTACKS, component);
    }

    private void insertAfter(EntryModel anchor, EntryModel... entries) {
        if (anchor == null || entries == null || entries.length == 0) return;
        int index = getEntries().indexOf(anchor);
        int insertIndex = index < 0 ? getEntries().size() : index + 1;
        for (EntryModel entry : entries) {
            if (entry == null) continue;
            if (!getEntries().contains(entry)) {
                getEntries().add(insertIndex, entry);
                insertIndex++;
            }
        }
    }

    private void removeEntries(EntryModel... entries) {
        if (entries == null) return;
        for (EntryModel entry : entries) {
            if (entry != null) {
                getEntries().remove(entry);
            }
        }
    }

    private List<Holder<Item>> resolveItemHolders(List<String> ids) {
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.ITEM);
        if (lookupOpt.isEmpty()) {
            return List.of();
        }
        HolderLookup.RegistryLookup<Item> lookup = lookupOpt.get();
        List<Holder<Item>> holders = new ArrayList<>();
        for (String entry : ids) {
            if (entry.isBlank()) continue;
            if (entry.startsWith("#")) {
                ResourceLocation rl = tryParse(entry.substring(1));
                if (rl == null) continue;
                TagKey<Item> tag = TagKey.create(Registries.ITEM, rl);
                lookup.get(tag).ifPresent(named -> named.stream().forEach(holders::add));
            } else {
                ResourceLocation rl = tryParse(entry);
                if (rl == null) continue;
                ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, rl);
                lookup.get(key).ifPresent(holders::add);
            }
        }
        return holders;
    }

    private Optional<HolderSet<Item>> resolveNamedItemTag(String tagId) {
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.ITEM);
        if (lookupOpt.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation rl = tryParse(tagId);
        if (rl == null) {
            return Optional.empty();
        }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, rl);
        return lookupOpt.get().get(tag).map(named -> (HolderSet<Item>) named);
    }

    private Optional<HolderSet<DamageType>> parseDamageTypeHolderSet(String value) {
        String sanitized = sanitizeId(value);
        if (sanitized.isBlank()) {
            return Optional.empty();
        }
        if (!sanitized.startsWith("#")) {
            return Optional.empty();
        }
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.DAMAGE_TYPE);
        if (lookupOpt.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation rl = tryParse(sanitized.substring(1));
        if (rl == null) {
            return Optional.empty();
        }
        TagKey<DamageType> tag = TagKey.create(Registries.DAMAGE_TYPE, rl);
        return lookupOpt.get().get(tag).map(named -> (HolderSet<DamageType>) named);
    }

    private Optional<TagKey<DamageType>> parseDamageTypeTag(String value) {
        String sanitized = sanitizeId(value);
        if (sanitized.isBlank() || !sanitized.startsWith("#")) {
            return Optional.empty();
        }
        ResourceLocation rl = tryParse(sanitized.substring(1));
        if (rl == null) {
            return Optional.empty();
        }
        return Optional.of(TagKey.create(Registries.DAMAGE_TYPE, rl));
    }

    private Optional<Holder<SoundEvent>> resolveSoundHolder(String id) {
        return resolveSoundHolder(ClientUtil.registryAccess().lookup(Registries.SOUND_EVENT).orElse(null), id);
    }

    private Optional<Holder<SoundEvent>> resolveSoundHolder(HolderLookup.RegistryLookup<SoundEvent> lookup, String id) {
        if (lookup == null) {
            return Optional.empty();
        }
        String sanitized = sanitizeId(id);
        if (sanitized.isBlank()) {
            return Optional.empty();
        }
        ResourceLocation rl = tryParse(sanitized);
        if (rl == null) {
            return Optional.empty();
        }
        ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, rl);
        return lookup.get(key).map(holder -> (Holder<SoundEvent>) holder);
    }

    private List<String> parseIdentifierList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] parts = raw.split("[,\\n]");
        List<String> out = new ArrayList<>();
        for (String part : parts) {
            String trimmed = stripQuotes(part.trim());
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    private Set<ResourceLocation> extractTagIds(String raw) {
        Set<ResourceLocation> set = new LinkedHashSet<>();
        for (String entry : parseIdentifierList(raw)) {
            String trimmed = entry.startsWith("#") ? entry.substring(1) : null;
            if (trimmed != null && !trimmed.isBlank()) {
                ResourceLocation rl = tryParse(trimmed);
                if (rl != null) {
                    set.add(rl);
                }
            }
        }
        return set;
    }

    private Set<ResourceLocation> extractItemIds(String raw) {
        Set<ResourceLocation> set = new LinkedHashSet<>();
        for (String entry : parseIdentifierList(raw)) {
            if (entry.startsWith("#")) {
                continue;
            }
            ResourceLocation rl = tryParse(entry);
            if (rl != null) {
                set.add(rl);
            }
        }
        return set;
    }

    private String serializeRepairableItems(Repairable repairable) {
        HolderSet<Item> set = repairable.items();
        if (set instanceof HolderSet.Named<Item> named) {
            return "#" + named.key().location();
        }
        return set.stream()
                .map(holder -> holder.unwrapKey().map(key -> key.location().toString()).orElse(""))
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String sanitizeId(String id) {
        return id == null ? "" : stripQuotes(id.trim());
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim();
        if ((result.startsWith("\"") && result.endsWith("\"")) || (result.startsWith("'") && result.endsWith("'"))) {
            if (result.length() >= 2) {
                result = result.substring(1, result.length() - 1);
            }
        }
        return result.trim();
    }

    private ResourceLocation tryParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String sanitized = value.trim();
        try {
            return ResourceLocation.parse(sanitized);
        } catch (Exception ignored) {
            if (!sanitized.contains(":")) {
                try {
                    return ResourceLocation.parse("minecraft:" + sanitized);
                } catch (Exception ignored2) {
                    return null;
                }
            }
            return null;
        }
    }

    private String namespaceFilter(String id) {
        String sanitized = sanitizeId(id);
        if (sanitized.isBlank()) {
            return null;
        }
        String namespace = sanitized.contains(":") ? sanitized.substring(0, sanitized.indexOf(':')) : "minecraft";
        return "namespace:" + namespace.toLowerCase(Locale.ROOT);
    }
}
