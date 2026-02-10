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
import com.github.rinorsi.cadeditor.client.screen.model.entry.LabeledEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringWithActionsEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.SoundEventSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseEffects;
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

    private BooleanEntryModel minimumAttackChargeToggleEntry;
    private FloatEntryModel minimumAttackChargeValueEntry;
    private boolean minimumAttackChargeEnabled;
    private float minimumAttackCharge;

    private BooleanEntryModel directDamageTypeToggleEntry;
    private StringWithActionsEntryModel directDamageTypeEntry;
    private boolean directDamageTypeEnabled;
    private String directDamageTypeId;

    private BooleanEntryModel attackRangeToggleEntry;
    private FloatEntryModel attackRangeMinEntry;
    private FloatEntryModel attackRangeMaxEntry;
    private FloatEntryModel attackRangeMinCreativeEntry;
    private FloatEntryModel attackRangeMaxCreativeEntry;
    private FloatEntryModel attackRangeHitboxMarginEntry;
    private FloatEntryModel attackRangeMobFactorEntry;
    private boolean attackRangeEnabled;
    private float attackRangeMin;
    private float attackRangeMax;
    private float attackRangeMinCreative;
    private float attackRangeMaxCreative;
    private float attackRangeHitboxMargin;
    private float attackRangeMobFactor;

    private BooleanEntryModel swingAnimationToggleEntry;
    private StringWithActionsEntryModel swingAnimationTypeEntry;
    private IntegerEntryModel swingAnimationDurationEntry;
    private boolean swingAnimationEnabled;
    private String swingAnimationTypeId;
    private int swingAnimationDuration;

    private BooleanEntryModel useEffectsToggleEntry;
    private BooleanEntryModel useEffectsCanSprintEntry;
    private BooleanEntryModel useEffectsInteractVibrationsEntry;
    private FloatEntryModel useEffectsSpeedMultiplierEntry;
    private boolean useEffectsEnabled;
    private boolean useEffectsCanSprint;
    private boolean useEffectsInteractVibrations;
    private float useEffectsSpeedMultiplier;

    private BooleanEntryModel piercingWeaponToggleEntry;
    private BooleanEntryModel piercingWeaponDealsKnockbackEntry;
    private BooleanEntryModel piercingWeaponDismountsEntry;
    private SoundEventSelectionEntryModel piercingWeaponSoundEntry;
    private SoundEventSelectionEntryModel piercingWeaponHitSoundEntry;
    private boolean piercingWeaponEnabled;
    private boolean piercingWeaponDealsKnockback;
    private boolean piercingWeaponDismounts;
    private String piercingWeaponSoundId;
    private String piercingWeaponHitSoundId;

    private BooleanEntryModel kineticWeaponToggleEntry;
    private IntegerEntryModel kineticWeaponContactCooldownEntry;
    private IntegerEntryModel kineticWeaponDelayEntry;
    private FloatEntryModel kineticWeaponForwardMovementEntry;
    private FloatEntryModel kineticWeaponDamageMultiplierEntry;
    private SoundEventSelectionEntryModel kineticWeaponSoundEntry;
    private SoundEventSelectionEntryModel kineticWeaponHitSoundEntry;
    private BooleanEntryModel kineticWeaponDismountConditionToggleEntry;
    private IntegerEntryModel kineticWeaponDismountConditionDurationEntry;
    private FloatEntryModel kineticWeaponDismountConditionMinSpeedEntry;
    private FloatEntryModel kineticWeaponDismountConditionMinRelativeSpeedEntry;
    private BooleanEntryModel kineticWeaponKnockbackConditionToggleEntry;
    private IntegerEntryModel kineticWeaponKnockbackConditionDurationEntry;
    private FloatEntryModel kineticWeaponKnockbackConditionMinSpeedEntry;
    private FloatEntryModel kineticWeaponKnockbackConditionMinRelativeSpeedEntry;
    private BooleanEntryModel kineticWeaponDamageConditionToggleEntry;
    private IntegerEntryModel kineticWeaponDamageConditionDurationEntry;
    private FloatEntryModel kineticWeaponDamageConditionMinSpeedEntry;
    private FloatEntryModel kineticWeaponDamageConditionMinRelativeSpeedEntry;
    private boolean kineticWeaponEnabled;
    private int kineticWeaponContactCooldownTicks;
    private int kineticWeaponDelayTicks;
    private float kineticWeaponForwardMovement;
    private float kineticWeaponDamageMultiplier;
    private String kineticWeaponSoundId;
    private String kineticWeaponHitSoundId;
    private boolean kineticWeaponDismountConditionEnabled;
    private int kineticWeaponDismountConditionDuration;
    private float kineticWeaponDismountConditionMinSpeed;
    private float kineticWeaponDismountConditionMinRelativeSpeed;
    private boolean kineticWeaponKnockbackConditionEnabled;
    private int kineticWeaponKnockbackConditionDuration;
    private float kineticWeaponKnockbackConditionMinSpeed;
    private float kineticWeaponKnockbackConditionMinRelativeSpeed;
    private boolean kineticWeaponDamageConditionEnabled;
    private int kineticWeaponDamageConditionDuration;
    private float kineticWeaponDamageConditionMinSpeed;
    private float kineticWeaponDamageConditionMinRelativeSpeed;

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
        weaponToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("weapon_enabled"), weaponEnabled, this::setWeaponEnabled),
                "weapon_enabled", 2);
        getEntries().add(weaponToggleEntry);
        weaponDamageEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("weapon_item_damage"), weaponDamagePerAttack, this::setWeaponDamagePerAttack, value -> value != null && value >= 0),
                "weapon_item_damage", 1);
        weaponDisableEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("weapon_disable_blocking"), weaponDisableSeconds, this::setWeaponDisableSeconds, value -> value != null && value >= 0f),
                "weapon_disable_blocking", 1);
        if (weaponEnabled) {
            insertAfter(weaponToggleEntry, weaponDamageEntry, weaponDisableEntry);
        }

        // Minimum attack charge
        Float minimumAttackChargeComponent = stack.get(DataComponents.MINIMUM_ATTACK_CHARGE);
        minimumAttackChargeEnabled = minimumAttackChargeComponent != null;
        minimumAttackCharge = minimumAttackChargeComponent != null ? minimumAttackChargeComponent : 0f;
        minimumAttackChargeToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("minimum_attack_charge_enabled"), minimumAttackChargeEnabled, this::setMinimumAttackChargeEnabled),
                "minimum_attack_charge_enabled", 2);
        getEntries().add(minimumAttackChargeToggleEntry);
        minimumAttackChargeValueEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("minimum_attack_charge_value"), minimumAttackCharge, this::setMinimumAttackCharge, value -> value != null && value >= 0f && value <= 1f),
                "minimum_attack_charge_value", 2);
        if (minimumAttackChargeEnabled) {
            insertAfter(minimumAttackChargeToggleEntry, minimumAttackChargeValueEntry);
        }

        // Direct damage type
        EitherHolder<DamageType> directDamageType = stack.get(DataComponents.DAMAGE_TYPE);
        directDamageTypeEnabled = directDamageType != null;
        directDamageTypeId = directDamageType != null ? extractDamageTypeId(directDamageType) : "";
        directDamageTypeToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("damage_type_enabled"), directDamageTypeEnabled, this::setDirectDamageTypeEnabled),
                "damage_type_enabled", 1);
        getEntries().add(directDamageTypeToggleEntry);
        directDamageTypeEntry = withWikiTooltip(new StringWithActionsEntryModel(this, ModTexts.gui("damage_type_value"), directDamageTypeId, this::setDirectDamageTypeId),
                "damage_type_value", 1);
        directDamageTypeEntry.setPlaceholder("minecraft:player_attack");
        directDamageTypeEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_damage_type"), this::openDirectDamageTypeSelection));
        if (directDamageTypeEnabled) {
            insertAfter(directDamageTypeToggleEntry, directDamageTypeEntry);
        }

        // Attack range
        AttackRange attackRange = stack.get(DataComponents.ATTACK_RANGE);
        attackRangeEnabled = attackRange != null;
        attackRangeMin = attackRange != null ? attackRange.minRange() : 0f;
        attackRangeMax = attackRange != null ? attackRange.maxRange() : 3f;
        attackRangeMinCreative = attackRange != null ? attackRange.minCreativeRange() : 0f;
        attackRangeMaxCreative = attackRange != null ? attackRange.maxCreativeRange() : 5f;
        attackRangeHitboxMargin = attackRange != null ? attackRange.hitboxMargin() : 0.3f;
        attackRangeMobFactor = attackRange != null ? attackRange.mobFactor() : 1f;
        attackRangeToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("attack_range_enabled"), attackRangeEnabled, this::setAttackRangeEnabled),
                "attack_range_enabled", 2);
        getEntries().add(attackRangeToggleEntry);
        attackRangeMinEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_min"), attackRangeMin, this::setAttackRangeMin, value -> value != null && value >= 0f && value <= 64f),
                "attack_range_min", 2);
        attackRangeMaxEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_max"), attackRangeMax, this::setAttackRangeMax, value -> value != null && value >= 0f && value <= 64f),
                "attack_range_max", 2);
        attackRangeMinCreativeEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_min_creative"), attackRangeMinCreative, this::setAttackRangeMinCreative, value -> value != null && value >= 0f && value <= 64f),
                "attack_range_min_creative", 2);
        attackRangeMaxCreativeEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_max_creative"), attackRangeMaxCreative, this::setAttackRangeMaxCreative, value -> value != null && value >= 0f && value <= 64f),
                "attack_range_max_creative", 2);
        attackRangeHitboxMarginEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_hitbox_margin"), attackRangeHitboxMargin, this::setAttackRangeHitboxMargin, value -> value != null && value >= 0f && value <= 1f),
                "attack_range_hitbox_margin", 2);
        attackRangeMobFactorEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("attack_range_mob_factor"), attackRangeMobFactor, this::setAttackRangeMobFactor, value -> value != null && value >= 0f && value <= 2f),
                "attack_range_mob_factor", 2);
        if (attackRangeEnabled) {
            insertAfter(
                    attackRangeToggleEntry,
                    attackRangeMinEntry,
                    attackRangeMaxEntry,
                    attackRangeMinCreativeEntry,
                    attackRangeMaxCreativeEntry,
                    attackRangeHitboxMarginEntry,
                    attackRangeMobFactorEntry
            );
        }

        // Swing animation
        SwingAnimation swingAnimation = stack.get(DataComponents.SWING_ANIMATION);
        swingAnimationEnabled = swingAnimation != null;
        swingAnimationTypeId = swingAnimation != null
                ? swingAnimation.type().getSerializedName()
                : SwingAnimation.DEFAULT.type().getSerializedName();
        swingAnimationDuration = swingAnimation != null ? swingAnimation.duration() : SwingAnimation.DEFAULT.duration();
        swingAnimationToggleEntry = new BooleanEntryModel(this, ModTexts.gui("swing_animation_enabled"), swingAnimationEnabled, this::setSwingAnimationEnabled);
        getEntries().add(swingAnimationToggleEntry);
        swingAnimationTypeEntry = new StringWithActionsEntryModel(this, ModTexts.gui("swing_animation_type"), swingAnimationTypeId, this::setSwingAnimationTypeId);
        swingAnimationTypeEntry.setPlaceholder("whack | stab | none");
        swingAnimationTypeEntry.addButton(new StringWithActionsEntryModel.ActionButton(ModTextures.SEARCH, ModTexts.gui("select_swing_animation_type"), this::openSwingAnimationTypeSelection));
        swingAnimationDurationEntry = new IntegerEntryModel(this, ModTexts.gui("swing_animation_duration"), swingAnimationDuration, this::setSwingAnimationDuration, value -> value != null && value >= 0);
        if (swingAnimationEnabled) {
            insertAfter(swingAnimationToggleEntry, swingAnimationTypeEntry, swingAnimationDurationEntry);
        }

        // Use effects
        UseEffects useEffects = stack.get(DataComponents.USE_EFFECTS);
        useEffectsEnabled = useEffects != null;
        useEffectsCanSprint = useEffects != null ? useEffects.canSprint() : UseEffects.DEFAULT.canSprint();
        useEffectsInteractVibrations = useEffects != null ? useEffects.interactVibrations() : UseEffects.DEFAULT.interactVibrations();
        useEffectsSpeedMultiplier = useEffects != null ? useEffects.speedMultiplier() : UseEffects.DEFAULT.speedMultiplier();
        useEffectsToggleEntry = new BooleanEntryModel(this, ModTexts.gui("use_effects_enabled"), useEffectsEnabled, this::setUseEffectsEnabled);
        getEntries().add(useEffectsToggleEntry);
        useEffectsCanSprintEntry = new BooleanEntryModel(this, ModTexts.gui("use_effects_can_sprint"), useEffectsCanSprint, this::setUseEffectsCanSprint);
        useEffectsInteractVibrationsEntry = new BooleanEntryModel(this, ModTexts.gui("use_effects_interact_vibrations"), useEffectsInteractVibrations, this::setUseEffectsInteractVibrations);
        useEffectsSpeedMultiplierEntry = new FloatEntryModel(this, ModTexts.gui("use_effects_speed_multiplier"), useEffectsSpeedMultiplier, this::setUseEffectsSpeedMultiplier, value -> value != null && value > 0f);
        if (useEffectsEnabled) {
            insertAfter(useEffectsToggleEntry, useEffectsCanSprintEntry, useEffectsInteractVibrationsEntry, useEffectsSpeedMultiplierEntry);
        }

        // Piercing weapon
        PiercingWeapon piercingWeapon = stack.get(DataComponents.PIERCING_WEAPON);
        piercingWeaponEnabled = piercingWeapon != null;
        piercingWeaponDealsKnockback = piercingWeapon != null ? piercingWeapon.dealsKnockback() : true;
        piercingWeaponDismounts = piercingWeapon != null && piercingWeapon.dismounts();
        piercingWeaponSoundId = piercingWeapon != null && piercingWeapon.sound().isPresent()
                ? piercingWeapon.sound().get().unwrapKey().map(key -> key.identifier().toString()).orElse("")
                : "";
        piercingWeaponHitSoundId = piercingWeapon != null && piercingWeapon.hitSound().isPresent()
                ? piercingWeapon.hitSound().get().unwrapKey().map(key -> key.identifier().toString()).orElse("")
                : "";
        piercingWeaponToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("piercing_weapon_enabled"), piercingWeaponEnabled, this::setPiercingWeaponEnabled),
                "piercing_weapon_enabled", 2);
        getEntries().add(piercingWeaponToggleEntry);
        piercingWeaponDealsKnockbackEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("piercing_weapon_knockback"), piercingWeaponDealsKnockback, this::setPiercingWeaponDealsKnockback),
                "piercing_weapon_knockback", 1);
        piercingWeaponDismountsEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("piercing_weapon_dismounts"), piercingWeaponDismounts, this::setPiercingWeaponDismounts),
                "piercing_weapon_dismounts", 1);
        piercingWeaponSoundEntry = withWikiTooltip(new SoundEventSelectionEntryModel(this, ModTexts.gui("piercing_weapon_sound"), piercingWeaponSoundId, this::setPiercingWeaponSoundId, namespaceFilter(piercingWeaponSoundId)),
                "piercing_weapon_sound", 1);
        piercingWeaponHitSoundEntry = withWikiTooltip(new SoundEventSelectionEntryModel(this, ModTexts.gui("piercing_weapon_hit_sound"), piercingWeaponHitSoundId, this::setPiercingWeaponHitSoundId, namespaceFilter(piercingWeaponHitSoundId)),
                "piercing_weapon_hit_sound", 1);
        if (piercingWeaponEnabled) {
            insertAfter(
                    piercingWeaponToggleEntry,
                    piercingWeaponDealsKnockbackEntry,
                    piercingWeaponDismountsEntry,
                    piercingWeaponSoundEntry,
                    piercingWeaponHitSoundEntry
            );
        }

        // Kinetic weapon
        KineticWeapon kineticWeapon = stack.get(DataComponents.KINETIC_WEAPON);
        kineticWeaponEnabled = kineticWeapon != null;
        kineticWeaponContactCooldownTicks = kineticWeapon != null ? kineticWeapon.contactCooldownTicks() : 10;
        kineticWeaponDelayTicks = kineticWeapon != null ? kineticWeapon.delayTicks() : 0;
        kineticWeaponForwardMovement = kineticWeapon != null ? kineticWeapon.forwardMovement() : 0f;
        kineticWeaponDamageMultiplier = kineticWeapon != null ? kineticWeapon.damageMultiplier() : 1f;
        kineticWeaponSoundId = kineticWeapon != null && kineticWeapon.sound().isPresent()
                ? kineticWeapon.sound().get().unwrapKey().map(key -> key.identifier().toString()).orElse("")
                : "";
        kineticWeaponHitSoundId = kineticWeapon != null && kineticWeapon.hitSound().isPresent()
                ? kineticWeapon.hitSound().get().unwrapKey().map(key -> key.identifier().toString()).orElse("")
                : "";
        kineticWeaponDismountConditionEnabled = kineticWeapon != null && kineticWeapon.dismountConditions().isPresent();
        kineticWeaponKnockbackConditionEnabled = kineticWeapon != null && kineticWeapon.knockbackConditions().isPresent();
        kineticWeaponDamageConditionEnabled = kineticWeapon != null && kineticWeapon.damageConditions().isPresent();
        KineticWeapon.Condition dismountCondition = kineticWeapon != null ? kineticWeapon.dismountConditions().orElse(null) : null;
        KineticWeapon.Condition knockbackCondition = kineticWeapon != null ? kineticWeapon.knockbackConditions().orElse(null) : null;
        KineticWeapon.Condition damageCondition = kineticWeapon != null ? kineticWeapon.damageConditions().orElse(null) : null;
        kineticWeaponDismountConditionDuration = dismountCondition != null ? dismountCondition.maxDurationTicks() : 0;
        kineticWeaponDismountConditionMinSpeed = dismountCondition != null ? dismountCondition.minSpeed() : 0f;
        kineticWeaponDismountConditionMinRelativeSpeed = dismountCondition != null ? dismountCondition.minRelativeSpeed() : 0f;
        kineticWeaponKnockbackConditionDuration = knockbackCondition != null ? knockbackCondition.maxDurationTicks() : 0;
        kineticWeaponKnockbackConditionMinSpeed = knockbackCondition != null ? knockbackCondition.minSpeed() : 0f;
        kineticWeaponKnockbackConditionMinRelativeSpeed = knockbackCondition != null ? knockbackCondition.minRelativeSpeed() : 0f;
        kineticWeaponDamageConditionDuration = damageCondition != null ? damageCondition.maxDurationTicks() : 0;
        kineticWeaponDamageConditionMinSpeed = damageCondition != null ? damageCondition.minSpeed() : 0f;
        kineticWeaponDamageConditionMinRelativeSpeed = damageCondition != null ? damageCondition.minRelativeSpeed() : 0f;
        kineticWeaponToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("kinetic_weapon_enabled"), kineticWeaponEnabled, this::setKineticWeaponEnabled),
                "kinetic_weapon_enabled", 2);
        getEntries().add(kineticWeaponToggleEntry);
        kineticWeaponContactCooldownEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("kinetic_weapon_contact_cooldown"), kineticWeaponContactCooldownTicks, this::setKineticWeaponContactCooldownTicks, value -> value != null && value >= 0),
                "kinetic_weapon_contact_cooldown", 2);
        kineticWeaponDelayEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("kinetic_weapon_delay_ticks"), kineticWeaponDelayTicks, this::setKineticWeaponDelayTicks, value -> value != null && value >= 0),
                "kinetic_weapon_delay_ticks", 1);
        kineticWeaponForwardMovementEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_forward_movement"), kineticWeaponForwardMovement, this::setKineticWeaponForwardMovement),
                "kinetic_weapon_forward_movement", 1);
        kineticWeaponDamageMultiplierEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_damage_multiplier"), kineticWeaponDamageMultiplier, this::setKineticWeaponDamageMultiplier, value -> value != null && value >= 0f),
                "kinetic_weapon_damage_multiplier", 2);
        kineticWeaponSoundEntry = withWikiTooltip(new SoundEventSelectionEntryModel(this, ModTexts.gui("kinetic_weapon_sound"), kineticWeaponSoundId, this::setKineticWeaponSoundId, namespaceFilter(kineticWeaponSoundId)),
                "kinetic_weapon_sound", 1);
        kineticWeaponHitSoundEntry = withWikiTooltip(new SoundEventSelectionEntryModel(this, ModTexts.gui("kinetic_weapon_hit_sound"), kineticWeaponHitSoundId, this::setKineticWeaponHitSoundId, namespaceFilter(kineticWeaponHitSoundId)),
                "kinetic_weapon_hit_sound", 1);
        kineticWeaponDismountConditionToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("kinetic_weapon_dismount_conditions"), kineticWeaponDismountConditionEnabled, this::setKineticWeaponDismountConditionEnabled),
                "kinetic_weapon_dismount_conditions", 1);
        kineticWeaponDismountConditionDurationEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("kinetic_weapon_condition_max_duration"), kineticWeaponDismountConditionDuration, this::setKineticWeaponDismountConditionDuration, value -> value != null && value >= 0),
                "kinetic_weapon_condition_max_duration", 1);
        kineticWeaponDismountConditionMinSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_speed"), kineticWeaponDismountConditionMinSpeed, this::setKineticWeaponDismountConditionMinSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_speed", 1);
        kineticWeaponDismountConditionMinRelativeSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_relative_speed"), kineticWeaponDismountConditionMinRelativeSpeed, this::setKineticWeaponDismountConditionMinRelativeSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_relative_speed", 1);
        kineticWeaponKnockbackConditionToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("kinetic_weapon_knockback_conditions"), kineticWeaponKnockbackConditionEnabled, this::setKineticWeaponKnockbackConditionEnabled),
                "kinetic_weapon_knockback_conditions", 1);
        kineticWeaponKnockbackConditionDurationEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("kinetic_weapon_condition_max_duration"), kineticWeaponKnockbackConditionDuration, this::setKineticWeaponKnockbackConditionDuration, value -> value != null && value >= 0),
                "kinetic_weapon_condition_max_duration", 1);
        kineticWeaponKnockbackConditionMinSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_speed"), kineticWeaponKnockbackConditionMinSpeed, this::setKineticWeaponKnockbackConditionMinSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_speed", 1);
        kineticWeaponKnockbackConditionMinRelativeSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_relative_speed"), kineticWeaponKnockbackConditionMinRelativeSpeed, this::setKineticWeaponKnockbackConditionMinRelativeSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_relative_speed", 1);
        kineticWeaponDamageConditionToggleEntry = withWikiTooltip(new BooleanEntryModel(this, ModTexts.gui("kinetic_weapon_damage_conditions"), kineticWeaponDamageConditionEnabled, this::setKineticWeaponDamageConditionEnabled),
                "kinetic_weapon_damage_conditions", 1);
        kineticWeaponDamageConditionDurationEntry = withWikiTooltip(new IntegerEntryModel(this, ModTexts.gui("kinetic_weapon_condition_max_duration"), kineticWeaponDamageConditionDuration, this::setKineticWeaponDamageConditionDuration, value -> value != null && value >= 0),
                "kinetic_weapon_condition_max_duration", 1);
        kineticWeaponDamageConditionMinSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_speed"), kineticWeaponDamageConditionMinSpeed, this::setKineticWeaponDamageConditionMinSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_speed", 1);
        kineticWeaponDamageConditionMinRelativeSpeedEntry = withWikiTooltip(new FloatEntryModel(this, ModTexts.gui("kinetic_weapon_condition_min_relative_speed"), kineticWeaponDamageConditionMinRelativeSpeed, this::setKineticWeaponDamageConditionMinRelativeSpeed, value -> value != null && value >= 0f),
                "kinetic_weapon_condition_min_relative_speed", 1);
        if (kineticWeaponEnabled) {
            insertAfter(
                    kineticWeaponToggleEntry,
                    kineticWeaponContactCooldownEntry,
                    kineticWeaponDelayEntry,
                    kineticWeaponForwardMovementEntry,
                    kineticWeaponDamageMultiplierEntry,
                    kineticWeaponSoundEntry,
                    kineticWeaponHitSoundEntry,
                    kineticWeaponDismountConditionToggleEntry,
                    kineticWeaponDismountConditionDurationEntry,
                    kineticWeaponDismountConditionMinSpeedEntry,
                    kineticWeaponDismountConditionMinRelativeSpeedEntry,
                    kineticWeaponKnockbackConditionToggleEntry,
                    kineticWeaponKnockbackConditionDurationEntry,
                    kineticWeaponKnockbackConditionMinSpeedEntry,
                    kineticWeaponKnockbackConditionMinRelativeSpeedEntry,
                    kineticWeaponDamageConditionToggleEntry,
                    kineticWeaponDamageConditionDurationEntry,
                    kineticWeaponDamageConditionMinSpeedEntry,
                    kineticWeaponDamageConditionMinRelativeSpeedEntry
            );
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
                ? blocksAttacks.blockSound().get().unwrapKey().map(holder -> holder.identifier().toString()).orElse("")
                : "";
        disableSoundId = blocksAttacks != null && blocksAttacks.disableSound().isPresent()
                ? blocksAttacks.disableSound().get().unwrapKey().map(holder -> holder.identifier().toString()).orElse("")
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

    private void setMinimumAttackChargeEnabled(boolean value) {
        minimumAttackChargeEnabled = value;
        if (value) {
            insertAfter(minimumAttackChargeToggleEntry, minimumAttackChargeValueEntry);
        } else {
            removeEntries(minimumAttackChargeValueEntry);
        }
        applyMinimumAttackChargeComponent();
    }

    private void setMinimumAttackCharge(Float value) {
        minimumAttackCharge = value == null ? 0f : value;
        if (minimumAttackChargeEnabled) {
            applyMinimumAttackChargeComponent();
        }
    }

    private void setDirectDamageTypeEnabled(boolean value) {
        directDamageTypeEnabled = value;
        if (value) {
            insertAfter(directDamageTypeToggleEntry, directDamageTypeEntry);
        } else {
            removeEntries(directDamageTypeEntry);
        }
        applyDirectDamageTypeComponent();
    }

    private void setDirectDamageTypeId(String value) {
        directDamageTypeId = value == null ? "" : value.trim();
        if (directDamageTypeEnabled) {
            applyDirectDamageTypeComponent();
        }
    }

    private void setAttackRangeEnabled(boolean value) {
        attackRangeEnabled = value;
        if (value) {
            insertAfter(
                    attackRangeToggleEntry,
                    attackRangeMinEntry,
                    attackRangeMaxEntry,
                    attackRangeMinCreativeEntry,
                    attackRangeMaxCreativeEntry,
                    attackRangeHitboxMarginEntry,
                    attackRangeMobFactorEntry
            );
        } else {
            removeEntries(
                    attackRangeMinEntry,
                    attackRangeMaxEntry,
                    attackRangeMinCreativeEntry,
                    attackRangeMaxCreativeEntry,
                    attackRangeHitboxMarginEntry,
                    attackRangeMobFactorEntry
            );
        }
        applyAttackRangeComponent();
    }

    private void setAttackRangeMin(Float value) {
        attackRangeMin = value == null ? 0f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setAttackRangeMax(Float value) {
        attackRangeMax = value == null ? 3f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setAttackRangeMinCreative(Float value) {
        attackRangeMinCreative = value == null ? 0f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setAttackRangeMaxCreative(Float value) {
        attackRangeMaxCreative = value == null ? 5f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setAttackRangeHitboxMargin(Float value) {
        attackRangeHitboxMargin = value == null ? 0.3f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setAttackRangeMobFactor(Float value) {
        attackRangeMobFactor = value == null ? 1f : value;
        if (attackRangeEnabled) {
            applyAttackRangeComponent();
        }
    }

    private void setSwingAnimationEnabled(boolean value) {
        swingAnimationEnabled = value;
        if (value) {
            insertAfter(swingAnimationToggleEntry, swingAnimationTypeEntry, swingAnimationDurationEntry);
        } else {
            removeEntries(swingAnimationTypeEntry, swingAnimationDurationEntry);
        }
        applySwingAnimationComponent();
    }

    private void setSwingAnimationTypeId(String value) {
        swingAnimationTypeId = value == null ? "" : value.trim();
        if (swingAnimationEnabled) {
            applySwingAnimationComponent();
        }
    }

    private void setSwingAnimationDuration(Integer value) {
        swingAnimationDuration = value == null ? SwingAnimation.DEFAULT.duration() : Math.max(0, value);
        if (swingAnimationEnabled) {
            applySwingAnimationComponent();
        }
    }

    private void setUseEffectsEnabled(boolean value) {
        useEffectsEnabled = value;
        if (value) {
            insertAfter(useEffectsToggleEntry, useEffectsCanSprintEntry, useEffectsInteractVibrationsEntry, useEffectsSpeedMultiplierEntry);
        } else {
            removeEntries(useEffectsCanSprintEntry, useEffectsInteractVibrationsEntry, useEffectsSpeedMultiplierEntry);
        }
        applyUseEffectsComponent();
    }

    private void setUseEffectsCanSprint(boolean value) {
        useEffectsCanSprint = value;
        if (useEffectsEnabled) {
            applyUseEffectsComponent();
        }
    }

    private void setUseEffectsInteractVibrations(boolean value) {
        useEffectsInteractVibrations = value;
        if (useEffectsEnabled) {
            applyUseEffectsComponent();
        }
    }

    private void setUseEffectsSpeedMultiplier(Float value) {
        useEffectsSpeedMultiplier = value == null ? UseEffects.DEFAULT.speedMultiplier() : value;
        if (useEffectsEnabled) {
            applyUseEffectsComponent();
        }
    }

    private void setPiercingWeaponEnabled(boolean value) {
        piercingWeaponEnabled = value;
        if (value) {
            insertAfter(
                    piercingWeaponToggleEntry,
                    piercingWeaponDealsKnockbackEntry,
                    piercingWeaponDismountsEntry,
                    piercingWeaponSoundEntry,
                    piercingWeaponHitSoundEntry
            );
        } else {
            removeEntries(
                    piercingWeaponDealsKnockbackEntry,
                    piercingWeaponDismountsEntry,
                    piercingWeaponSoundEntry,
                    piercingWeaponHitSoundEntry
            );
        }
        applyPiercingWeaponComponent();
    }

    private void setPiercingWeaponDealsKnockback(boolean value) {
        piercingWeaponDealsKnockback = value;
        if (piercingWeaponEnabled) {
            applyPiercingWeaponComponent();
        }
    }

    private void setPiercingWeaponDismounts(boolean value) {
        piercingWeaponDismounts = value;
        if (piercingWeaponEnabled) {
            applyPiercingWeaponComponent();
        }
    }

    private void setPiercingWeaponSoundId(String id) {
        piercingWeaponSoundId = sanitizeId(id);
        if (piercingWeaponSoundEntry != null) {
            piercingWeaponSoundEntry.setValid(piercingWeaponSoundId.isBlank() || resolveSoundHolder(piercingWeaponSoundId).isPresent());
        }
        if (piercingWeaponEnabled) {
            applyPiercingWeaponComponent();
        }
    }

    private void setPiercingWeaponHitSoundId(String id) {
        piercingWeaponHitSoundId = sanitizeId(id);
        if (piercingWeaponHitSoundEntry != null) {
            piercingWeaponHitSoundEntry.setValid(piercingWeaponHitSoundId.isBlank() || resolveSoundHolder(piercingWeaponHitSoundId).isPresent());
        }
        if (piercingWeaponEnabled) {
            applyPiercingWeaponComponent();
        }
    }

    private void setKineticWeaponEnabled(boolean value) {
        kineticWeaponEnabled = value;
        if (value) {
            insertAfter(
                    kineticWeaponToggleEntry,
                    kineticWeaponContactCooldownEntry,
                    kineticWeaponDelayEntry,
                    kineticWeaponForwardMovementEntry,
                    kineticWeaponDamageMultiplierEntry,
                    kineticWeaponSoundEntry,
                    kineticWeaponHitSoundEntry,
                    kineticWeaponDismountConditionToggleEntry,
                    kineticWeaponDismountConditionDurationEntry,
                    kineticWeaponDismountConditionMinSpeedEntry,
                    kineticWeaponDismountConditionMinRelativeSpeedEntry,
                    kineticWeaponKnockbackConditionToggleEntry,
                    kineticWeaponKnockbackConditionDurationEntry,
                    kineticWeaponKnockbackConditionMinSpeedEntry,
                    kineticWeaponKnockbackConditionMinRelativeSpeedEntry,
                    kineticWeaponDamageConditionToggleEntry,
                    kineticWeaponDamageConditionDurationEntry,
                    kineticWeaponDamageConditionMinSpeedEntry,
                    kineticWeaponDamageConditionMinRelativeSpeedEntry
            );
        } else {
            removeEntries(
                    kineticWeaponContactCooldownEntry,
                    kineticWeaponDelayEntry,
                    kineticWeaponForwardMovementEntry,
                    kineticWeaponDamageMultiplierEntry,
                    kineticWeaponSoundEntry,
                    kineticWeaponHitSoundEntry,
                    kineticWeaponDismountConditionToggleEntry,
                    kineticWeaponDismountConditionDurationEntry,
                    kineticWeaponDismountConditionMinSpeedEntry,
                    kineticWeaponDismountConditionMinRelativeSpeedEntry,
                    kineticWeaponKnockbackConditionToggleEntry,
                    kineticWeaponKnockbackConditionDurationEntry,
                    kineticWeaponKnockbackConditionMinSpeedEntry,
                    kineticWeaponKnockbackConditionMinRelativeSpeedEntry,
                    kineticWeaponDamageConditionToggleEntry,
                    kineticWeaponDamageConditionDurationEntry,
                    kineticWeaponDamageConditionMinSpeedEntry,
                    kineticWeaponDamageConditionMinRelativeSpeedEntry
            );
        }
        applyKineticWeaponComponent();
    }

    private void setKineticWeaponContactCooldownTicks(Integer value) {
        kineticWeaponContactCooldownTicks = value == null ? 10 : Math.max(0, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDelayTicks(Integer value) {
        kineticWeaponDelayTicks = value == null ? 0 : Math.max(0, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponForwardMovement(Float value) {
        kineticWeaponForwardMovement = value == null ? 0f : value;
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDamageMultiplier(Float value) {
        kineticWeaponDamageMultiplier = value == null ? 1f : value;
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponSoundId(String id) {
        kineticWeaponSoundId = sanitizeId(id);
        if (kineticWeaponSoundEntry != null) {
            kineticWeaponSoundEntry.setValid(kineticWeaponSoundId.isBlank() || resolveSoundHolder(kineticWeaponSoundId).isPresent());
        }
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponHitSoundId(String id) {
        kineticWeaponHitSoundId = sanitizeId(id);
        if (kineticWeaponHitSoundEntry != null) {
            kineticWeaponHitSoundEntry.setValid(kineticWeaponHitSoundId.isBlank() || resolveSoundHolder(kineticWeaponHitSoundId).isPresent());
        }
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDismountConditionEnabled(boolean value) {
        kineticWeaponDismountConditionEnabled = value;
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDismountConditionDuration(Integer value) {
        kineticWeaponDismountConditionDuration = value == null ? 0 : Math.max(0, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDismountConditionMinSpeed(Float value) {
        kineticWeaponDismountConditionMinSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDismountConditionMinRelativeSpeed(Float value) {
        kineticWeaponDismountConditionMinRelativeSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponKnockbackConditionEnabled(boolean value) {
        kineticWeaponKnockbackConditionEnabled = value;
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponKnockbackConditionDuration(Integer value) {
        kineticWeaponKnockbackConditionDuration = value == null ? 0 : Math.max(0, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponKnockbackConditionMinSpeed(Float value) {
        kineticWeaponKnockbackConditionMinSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponKnockbackConditionMinRelativeSpeed(Float value) {
        kineticWeaponKnockbackConditionMinRelativeSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDamageConditionEnabled(boolean value) {
        kineticWeaponDamageConditionEnabled = value;
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDamageConditionDuration(Integer value) {
        kineticWeaponDamageConditionDuration = value == null ? 0 : Math.max(0, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDamageConditionMinSpeed(Float value) {
        kineticWeaponDamageConditionMinSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
        }
    }

    private void setKineticWeaponDamageConditionMinRelativeSpeed(Float value) {
        kineticWeaponDamageConditionMinRelativeSpeed = value == null ? 0f : Math.max(0f, value);
        if (kineticWeaponEnabled) {
            applyKineticWeaponComponent();
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
        Set<Identifier> initiallySelected = extractItemIds(repairableItemsRaw);
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
                    for (Identifier rl : selected) {
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
        Set<Identifier> initiallySelected = extractTagIds(repairableItemsRaw);
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
                    for (Identifier rl : selected) {
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

    private void openDirectDamageTypeSelection() {
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_damage_type"),
                "damage_type",
                ClientCache.getDamageTypeSelectionItems(),
                value -> {
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    directDamageTypeId = value;
                    directDamageTypeEntry.setValue(value);
                    if (directDamageTypeEnabled) {
                        applyDirectDamageTypeComponent();
                    }
                }
        );
    }

    private void openSwingAnimationTypeSelection() {
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("select_swing_animation_type"),
                "swing_animation_type",
                swingAnimationTypeSelectionItems(),
                value -> {
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    Identifier id = tryParse(value);
                    String normalized = id != null ? id.getPath() : value;
                    swingAnimationTypeId = normalized;
                    swingAnimationTypeEntry.setValue(normalized);
                    if (swingAnimationEnabled) {
                        applySwingAnimationComponent();
                    }
                }
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
                .map(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse("?"))
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
        Optional<Identifier> group = Optional.empty();
        String sanitized = sanitizeId(useCooldownGroupId);
        if (!sanitized.isBlank()) {
            Identifier parsed = tryParse(sanitized);
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

    private void applyMinimumAttackChargeComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!minimumAttackChargeEnabled) {
            stack.remove(DataComponents.MINIMUM_ATTACK_CHARGE);
            getParent().removeComponentFromDataTag("minecraft:minimum_attack_charge");
            if (minimumAttackChargeValueEntry != null) {
                minimumAttackChargeValueEntry.setValid(true);
            }
            return;
        }
        float clamped = Math.max(0f, Math.min(1f, minimumAttackCharge));
        if (minimumAttackChargeValueEntry != null) {
            minimumAttackChargeValueEntry.setValid(minimumAttackCharge >= 0f && minimumAttackCharge <= 1f);
        }
        stack.set(DataComponents.MINIMUM_ATTACK_CHARGE, clamped);
    }

    private void applyDirectDamageTypeComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!directDamageTypeEnabled || sanitizeId(directDamageTypeId).isBlank()) {
            stack.remove(DataComponents.DAMAGE_TYPE);
            getParent().removeComponentFromDataTag("minecraft:damage_type");
            if (directDamageTypeEntry != null) {
                directDamageTypeEntry.setValid(true);
            }
            return;
        }
        Identifier parsed = tryParse(directDamageTypeId);
        if (parsed == null) {
            if (directDamageTypeEntry != null) {
                directDamageTypeEntry.setValid(false);
            }
            return;
        }
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, parsed);
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.DAMAGE_TYPE);
        if (lookupOpt.isEmpty() || lookupOpt.get().get(key).isEmpty()) {
            if (directDamageTypeEntry != null) {
                directDamageTypeEntry.setValid(false);
            }
            return;
        }
        if (directDamageTypeEntry != null) {
            directDamageTypeEntry.setValid(true);
        }
        stack.set(DataComponents.DAMAGE_TYPE, new EitherHolder<>(key));
    }

    private void applyAttackRangeComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!attackRangeEnabled) {
            stack.remove(DataComponents.ATTACK_RANGE);
            getParent().removeComponentFromDataTag("minecraft:attack_range");
            markAttackRangeEntriesValid(true);
            return;
        }
        boolean boundsValid = attackRangeMin <= attackRangeMax && attackRangeMinCreative <= attackRangeMaxCreative;
        markAttackRangeEntriesValid(boundsValid);
        if (!boundsValid) {
            return;
        }
        stack.set(DataComponents.ATTACK_RANGE, new AttackRange(
                clamp(attackRangeMin, 0f, 64f),
                clamp(attackRangeMax, 0f, 64f),
                clamp(attackRangeMinCreative, 0f, 64f),
                clamp(attackRangeMaxCreative, 0f, 64f),
                clamp(attackRangeHitboxMargin, 0f, 1f),
                clamp(attackRangeMobFactor, 0f, 2f)
        ));
    }

    private void applySwingAnimationComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!swingAnimationEnabled) {
            stack.remove(DataComponents.SWING_ANIMATION);
            getParent().removeComponentFromDataTag("minecraft:swing_animation");
            if (swingAnimationTypeEntry != null) {
                swingAnimationTypeEntry.setValid(true);
            }
            return;
        }
        Optional<SwingAnimationType> parsedType = parseSwingAnimationType(swingAnimationTypeId);
        if (parsedType.isEmpty()) {
            if (swingAnimationTypeEntry != null) {
                swingAnimationTypeEntry.setValid(false);
            }
            return;
        }
        if (swingAnimationTypeEntry != null) {
            swingAnimationTypeEntry.setValid(true);
        }
        stack.set(DataComponents.SWING_ANIMATION, new SwingAnimation(parsedType.get(), Math.max(0, swingAnimationDuration)));
    }

    private void applyUseEffectsComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!useEffectsEnabled) {
            stack.remove(DataComponents.USE_EFFECTS);
            getParent().removeComponentFromDataTag("minecraft:use_effects");
            if (useEffectsSpeedMultiplierEntry != null) {
                useEffectsSpeedMultiplierEntry.setValid(true);
            }
            return;
        }
        if (useEffectsSpeedMultiplierEntry != null) {
            useEffectsSpeedMultiplierEntry.setValid(useEffectsSpeedMultiplier > 0f);
        }
        if (useEffectsSpeedMultiplier <= 0f) {
            return;
        }
        stack.set(DataComponents.USE_EFFECTS, new UseEffects(
                useEffectsCanSprint,
                useEffectsInteractVibrations,
                useEffectsSpeedMultiplier
        ));
    }

    private void applyPiercingWeaponComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!piercingWeaponEnabled) {
            stack.remove(DataComponents.PIERCING_WEAPON);
            getParent().removeComponentFromDataTag("minecraft:piercing_weapon");
            return;
        }
        stack.set(DataComponents.PIERCING_WEAPON, new PiercingWeapon(
                piercingWeaponDealsKnockback,
                piercingWeaponDismounts,
                resolveSoundHolder(piercingWeaponSoundId),
                resolveSoundHolder(piercingWeaponHitSoundId)
        ));
    }

    private void applyKineticWeaponComponent() {
        ItemStack stack = getParent().getContext().getItemStack();
        if (!kineticWeaponEnabled) {
            stack.remove(DataComponents.KINETIC_WEAPON);
            getParent().removeComponentFromDataTag("minecraft:kinetic_weapon");
            markKineticConditionEntriesValid(true);
            return;
        }
        Optional<KineticWeapon.Condition> dismountCondition = buildKineticCondition(
                kineticWeaponDismountConditionEnabled,
                kineticWeaponDismountConditionDuration,
                kineticWeaponDismountConditionMinSpeed,
                kineticWeaponDismountConditionMinRelativeSpeed
        );
        Optional<KineticWeapon.Condition> knockbackCondition = buildKineticCondition(
                kineticWeaponKnockbackConditionEnabled,
                kineticWeaponKnockbackConditionDuration,
                kineticWeaponKnockbackConditionMinSpeed,
                kineticWeaponKnockbackConditionMinRelativeSpeed
        );
        Optional<KineticWeapon.Condition> damageCondition = buildKineticCondition(
                kineticWeaponDamageConditionEnabled,
                kineticWeaponDamageConditionDuration,
                kineticWeaponDamageConditionMinSpeed,
                kineticWeaponDamageConditionMinRelativeSpeed
        );
        boolean valid = (!kineticWeaponDismountConditionEnabled || dismountCondition.isPresent())
                && (!kineticWeaponKnockbackConditionEnabled || knockbackCondition.isPresent())
                && (!kineticWeaponDamageConditionEnabled || damageCondition.isPresent());
        markKineticConditionEntriesValid(valid);
        if (!valid) {
            return;
        }
        stack.set(DataComponents.KINETIC_WEAPON, new KineticWeapon(
                Math.max(0, kineticWeaponContactCooldownTicks),
                Math.max(0, kineticWeaponDelayTicks),
                dismountCondition,
                knockbackCondition,
                damageCondition,
                kineticWeaponForwardMovement,
                Math.max(0f, kineticWeaponDamageMultiplier),
                resolveSoundHolder(kineticWeaponSoundId),
                resolveSoundHolder(kineticWeaponHitSoundId)
        ));
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

    private <T extends LabeledEntryModel> T withWikiTooltip(T entry, String key, int lines) {
        if (entry == null || lines <= 0) {
            return entry;
        }
        var tooltipLines = ModTexts.wikiTooltip(key, lines);
        for (int i = 0; i < tooltipLines.length; i++) {
            tooltipLines[i] = tooltipLines[i].copy().withStyle(ChatFormatting.GRAY);
        }
        entry.setLabelTooltip(tooltipLines);
        return entry;
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

    private void markAttackRangeEntriesValid(boolean valid) {
        if (attackRangeMinEntry != null) attackRangeMinEntry.setValid(valid);
        if (attackRangeMaxEntry != null) attackRangeMaxEntry.setValid(valid);
        if (attackRangeMinCreativeEntry != null) attackRangeMinCreativeEntry.setValid(valid);
        if (attackRangeMaxCreativeEntry != null) attackRangeMaxCreativeEntry.setValid(valid);
        if (attackRangeHitboxMarginEntry != null) attackRangeHitboxMarginEntry.setValid(valid);
        if (attackRangeMobFactorEntry != null) attackRangeMobFactorEntry.setValid(valid);
    }

    private void markKineticConditionEntriesValid(boolean valid) {
        setConditionEntriesValid(
                kineticWeaponDismountConditionDurationEntry,
                kineticWeaponDismountConditionMinSpeedEntry,
                kineticWeaponDismountConditionMinRelativeSpeedEntry,
                !kineticWeaponDismountConditionEnabled || valid
        );
        setConditionEntriesValid(
                kineticWeaponKnockbackConditionDurationEntry,
                kineticWeaponKnockbackConditionMinSpeedEntry,
                kineticWeaponKnockbackConditionMinRelativeSpeedEntry,
                !kineticWeaponKnockbackConditionEnabled || valid
        );
        setConditionEntriesValid(
                kineticWeaponDamageConditionDurationEntry,
                kineticWeaponDamageConditionMinSpeedEntry,
                kineticWeaponDamageConditionMinRelativeSpeedEntry,
                !kineticWeaponDamageConditionEnabled || valid
        );
    }

    private void setConditionEntriesValid(IntegerEntryModel durationEntry, FloatEntryModel minSpeedEntry, FloatEntryModel minRelativeEntry, boolean valid) {
        if (durationEntry != null) durationEntry.setValid(valid);
        if (minSpeedEntry != null) minSpeedEntry.setValid(valid);
        if (minRelativeEntry != null) minRelativeEntry.setValid(valid);
    }

    private Optional<KineticWeapon.Condition> buildKineticCondition(boolean enabled, int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        if (!enabled) {
            return Optional.empty();
        }
        if (maxDurationTicks < 0 || minSpeed < 0f || minRelativeSpeed < 0f) {
            return Optional.empty();
        }
        return Optional.of(new KineticWeapon.Condition(maxDurationTicks, minSpeed, minRelativeSpeed));
    }

    private List<ListSelectionElementModel> swingAnimationTypeSelectionItems() {
        List<ListSelectionElementModel> items = new ArrayList<>();
        for (SwingAnimationType type : SwingAnimationType.values()) {
            String key = "cadeditor.gui.swing_animation_type." + type.getSerializedName();
            Identifier id = Identifier.withDefaultNamespace(type.getSerializedName());
            items.add(new ListSelectionElementModel(key, id));
        }
        return items;
    }

    private Optional<SwingAnimationType> parseSwingAnimationType(String value) {
        String sanitized = sanitizeId(value).toLowerCase(Locale.ROOT);
        if (sanitized.isBlank()) {
            return Optional.empty();
        }
        if (sanitized.startsWith("minecraft:")) {
            sanitized = sanitized.substring("minecraft:".length());
        }
        for (SwingAnimationType type : SwingAnimationType.values()) {
            if (type.getSerializedName().equals(sanitized) || type.name().equalsIgnoreCase(sanitized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    private String extractDamageTypeId(EitherHolder<DamageType> eitherHolder) {
        return eitherHolder.key()
                .map(key -> key.identifier().toString())
                .or(() -> eitherHolder.unwrap(ClientUtil.registryAccess())
                        .flatMap(holder -> holder.unwrapKey().map(key -> key.identifier().toString())))
                .orElse("");
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
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
                Identifier rl = tryParse(entry.substring(1));
                if (rl == null) continue;
                TagKey<Item> tag = TagKey.create(Registries.ITEM, rl);
                lookup.get(tag).ifPresent(named -> named.stream().forEach(holders::add));
            } else {
                Identifier rl = tryParse(entry);
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
        Identifier rl = tryParse(tagId);
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
        Identifier rl = tryParse(sanitized.substring(1));
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
        Identifier rl = tryParse(sanitized.substring(1));
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
        Identifier rl = tryParse(sanitized);
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

    private Set<Identifier> extractTagIds(String raw) {
        Set<Identifier> set = new LinkedHashSet<>();
        for (String entry : parseIdentifierList(raw)) {
            String trimmed = entry.startsWith("#") ? entry.substring(1) : null;
            if (trimmed != null && !trimmed.isBlank()) {
                Identifier rl = tryParse(trimmed);
                if (rl != null) {
                    set.add(rl);
                }
            }
        }
        return set;
    }

    private Set<Identifier> extractItemIds(String raw) {
        Set<Identifier> set = new LinkedHashSet<>();
        for (String entry : parseIdentifierList(raw)) {
            if (entry.startsWith("#")) {
                continue;
            }
            Identifier rl = tryParse(entry);
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
                .map(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse(""))
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

    private Identifier tryParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String sanitized = value.trim();
        try {
            return Identifier.parse(sanitized);
        } catch (Exception ignored) {
            if (!sanitized.contains(":")) {
                try {
                    return Identifier.parse("minecraft:" + sanitized);
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
