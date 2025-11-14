package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FoodComponentState {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final float DEFAULT_CONSUME_SECONDS = 1.6f;
    private static final ItemUseAnimation DEFAULT_ANIMATION = ItemUseAnimation.EAT;

    private boolean enabled;
    private boolean hasEverBeenEnabled;
    private int nutrition = 4;
    private float saturation = 0.3f;
    private boolean alwaysEat;
    private float consumeSeconds = DEFAULT_CONSUME_SECONDS;
    private boolean hasConsumeParticles;
    private ItemUseAnimation animation = DEFAULT_ANIMATION;
    private Optional<Holder<SoundEvent>> consumeSound = Optional.empty();
    private String usingConvertsToId = "";
    private Optional<ItemStack> originalUsingConvertsTo = Optional.empty();
    private Optional<ItemStack> customUsingConvertsTo = Optional.empty();
    private List<FoodEffectData> effects = new ArrayList<>();
    private List<ConsumeEffect> preservedOtherEffects = new ArrayList<>();

    public void loadFrom(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        UseRemainder remainder = stack.get(DataComponents.USE_REMAINDER);

        if (food != null || consumable != null) {
            hasEverBeenEnabled = true;
            setEnabled(true);
        } else {
            resetToDefaults();
            setEnabled(false);
            DebugLog.infoKey("cadeditor.debug.food.missing", describeStack(stack));
            return;
        }

        if (food != null) {
            nutrition = food.nutrition();
            saturation = food.saturation();
            alwaysEat = food.canAlwaysEat();
        } else {
            nutrition = 4;
            saturation = 0.3f;
            alwaysEat = false;
        }

        if (consumable != null) {
            consumeSeconds = consumable.consumeSeconds();
            hasConsumeParticles = consumable.hasConsumeParticles();
            animation = consumable.animation();
            consumeSound = Optional.ofNullable(consumable.sound());
            preservedOtherEffects = consumable.onConsumeEffects().stream()
                    .filter(effect -> !(effect instanceof ApplyStatusEffectsConsumeEffect))
                    .collect(Collectors.toCollection(ArrayList::new));
            effects = consumable.onConsumeEffects().stream()
                    .filter(ApplyStatusEffectsConsumeEffect.class::isInstance)
                    .map(ApplyStatusEffectsConsumeEffect.class::cast)
                    .flatMap(apply -> apply.effects().stream()
                            .map(eff -> new FoodEffectData(new MobEffectInstance(eff), apply.probability())))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            consumeSeconds = DEFAULT_CONSUME_SECONDS;
            hasConsumeParticles = false;
            animation = DEFAULT_ANIMATION;
            consumeSound = Optional.empty();
            preservedOtherEffects = new ArrayList<>();
            effects = new ArrayList<>();
        }

        if (remainder != null) {
            ItemStack convert = prepareConvertStack(remainder.convertInto());
            originalUsingConvertsTo = Optional.of(convert);
            customUsingConvertsTo = originalUsingConvertsTo.map(ItemStack::copy);
            usingConvertsToId = BuiltInRegistries.ITEM.getKey(convert.getItem()).toString();
        } else {
            originalUsingConvertsTo = Optional.empty();
            customUsingConvertsTo = Optional.empty();
            usingConvertsToId = "";
        }

        DebugLog.infoKey("cadeditor.debug.food.loaded",
                describeStack(stack),
                usingConvertsToId,
                originalUsingConvertsTo.map(this::describeStack).orElse("<empty>"));
    }

    private void resetToDefaults() {
        hasEverBeenEnabled = false;
        nutrition = 4;
        saturation = 0.3f;
        alwaysEat = false;
        consumeSeconds = DEFAULT_CONSUME_SECONDS;
        hasConsumeParticles = false;
        animation = DEFAULT_ANIMATION;
        consumeSound = Optional.empty();
        usingConvertsToId = "";
        originalUsingConvertsTo = Optional.empty();
        customUsingConvertsTo = Optional.empty();
        effects = new ArrayList<>();
        preservedOtherEffects = new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            hasEverBeenEnabled = true;
        }
    }

    public int getNutrition() {
        return nutrition;
    }

    public void setNutrition(int nutrition) {
        this.nutrition = Math.max(0, nutrition);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = Math.max(0f, saturation);
    }

    public boolean isAlwaysEat() {
        return alwaysEat;
    }

    public void setAlwaysEat(boolean alwaysEat) {
        this.alwaysEat = alwaysEat;
    }

    public float getConsumeSeconds() {
        return consumeSeconds;
    }

    public void setConsumeSeconds(float seconds) {
        consumeSeconds = Math.max(0.05f, seconds);
    }

    public boolean hasConsumeParticles() {
        return hasConsumeParticles;
    }

    public void setHasConsumeParticles(boolean value) {
        hasConsumeParticles = value;
    }

    public ItemUseAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(ItemUseAnimation animation) {
        this.animation = animation == null ? DEFAULT_ANIMATION : animation;
    }

    public Optional<Holder<SoundEvent>> getConsumeSound() {
        return consumeSound;
    }

    public void setConsumeSound(Optional<Holder<SoundEvent>> sound) {
        consumeSound = sound == null ? Optional.empty() : sound;


    }

    public String getUsingConvertsToId() {
        return usingConvertsToId;
    }

    public void setUsingConvertsToId(String id) {
        usingConvertsToId = id == null ? "" : id.trim();
        if (usingConvertsToId.isBlank()) {
            customUsingConvertsTo = Optional.empty();
            originalUsingConvertsTo = Optional.empty();
        } else {
            customUsingConvertsTo = filterStackById(customUsingConvertsTo, usingConvertsToId);
            originalUsingConvertsTo = filterStackById(originalUsingConvertsTo, usingConvertsToId);
        }
    }

    public void prepareForInitialEnable(ItemStack stack) {
        if (hasEverBeenEnabled) {
            return;
        }
        setNutrition(0);
        setSaturation(0f);
        setAlwaysEat(true);
        setConsumeSeconds(DEFAULT_CONSUME_SECONDS);
        setUsingConvertsToId("");
        originalUsingConvertsTo = Optional.empty();
        customUsingConvertsTo = Optional.empty();
        effects = new ArrayList<>();
        preservedOtherEffects = new ArrayList<>();

        DebugLog.infoKey("cadeditor.debug.food.prepared", describeStack(stack));
    }

    public List<FoodEffectData> getEffects() {
        return effects.stream().map(FoodEffectData::copy).collect(Collectors.toUnmodifiableList());
    }

    public void setEffects(List<FoodEffectData> newEffects) {
        if (newEffects == null || newEffects.isEmpty()) {
            effects = new ArrayList<>();
        } else {
            effects = newEffects.stream()
                    .filter(Objects::nonNull)
                    .map(FoodEffectData::copy)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public Optional<ItemStack> resolveUsingConvertsTo() {
        if (usingConvertsToId.isBlank()) {
            DebugLog.infoKey("cadeditor.debug.food.convert_blank");
            return Optional.empty();
        }
        ResourceLocation rl = ResourceLocation.tryParse(usingConvertsToId);
        if (rl == null) {
            LOGGER.warn("Failed to parse using_converts_to id '{}'", usingConvertsToId);
            return Optional.empty();
        }
        Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
        if (item == null) {
            LOGGER.warn("Unknown using_converts_to item id '{}'", usingConvertsToId);
            return Optional.empty();
        }
        Optional<ItemStack> customized = customUsingConvertsTo
                .filter(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rl))
                .map(ItemStack::copy);
        if (customized.isPresent()) {
            DebugLog.infoKey("cadeditor.debug.food.convert_reuse", describeStack(customized.get()));
            return customized;
        }
        Optional<ItemStack> preserved = originalUsingConvertsTo
                .filter(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rl))
                .map(ItemStack::copy);
        if (preserved.isPresent()) {
            DebugLog.infoKey("cadeditor.debug.food.convert_reuse", describeStack(preserved.get()));
            return preserved;
        }
        ItemStack result = item.getDefaultInstance();
        if (result.isEmpty()) {
            result = new ItemStack(item);
        }
        ItemStack prepared = prepareConvertStack(result);
        DebugLog.infoKey("cadeditor.debug.food.convert_new", usingConvertsToId, describeStack(prepared));
        return Optional.of(prepared);
    }

    public List<FoodEffectData> copyEffectsForComponent() {
        return effects.stream().map(FoodEffectData::copy).collect(Collectors.toUnmodifiableList());
    }

    public List<ConsumeEffect> getPreservedOtherEffects() {
        return List.copyOf(preservedOtherEffects);
    }

    public void updateOriginalUsingConvertsTo(Optional<ItemStack> stack) {
        originalUsingConvertsTo = stack
                .filter(s -> !s.isEmpty())
                .map(this::prepareConvertStack);
        DebugLog.infoKey("cadeditor.debug.food.convert_update", originalUsingConvertsTo.map(this::describeStack).orElse("<empty>"));
    }

    public Optional<ItemStack> getUsingConvertsToPreview() {
        return customUsingConvertsTo.filter(stack -> !stack.isEmpty())
                .or(() -> originalUsingConvertsTo.filter(stack -> !stack.isEmpty()))
                .map(ItemStack::copy);
    }

    public Optional<ItemStack> getUsingConvertsToEditorStack() {
        return customUsingConvertsTo.filter(stack -> !stack.isEmpty())
                .or(() -> originalUsingConvertsTo.filter(stack -> !stack.isEmpty()))
                .map(this::prepareConvertStack)
                .map(ItemStack::copy);
    }

    public void useCustomUsingConvertsTo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            customUsingConvertsTo = Optional.empty();
            setUsingConvertsToId("");
            return;
        }
        ItemStack sanitized = prepareConvertStack(stack);
        customUsingConvertsTo = Optional.of(sanitized);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(sanitized.getItem());
        usingConvertsToId = id != null ? id.toString() : "";
        originalUsingConvertsTo = filterStackById(originalUsingConvertsTo, usingConvertsToId);
    }

    private ItemStack prepareConvertStack(ItemStack source) {
        if (source == null || source.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack sanitized = source.copy();
        sanitized.setCount(1);
        sanitized.remove(DataComponents.FOOD);
        sanitized.remove(DataComponents.CONSUMABLE);
        sanitized.remove(DataComponents.USE_REMAINDER);
        DebugLog.infoKey("cadeditor.debug.food.convert_sanitize", describeStack(sanitized));
        return sanitized;
    }

    private Optional<ItemStack> filterStackById(Optional<ItemStack> stack, String id) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return stack.filter(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals(id));
    }

    private String describeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "<empty>";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        StringBuilder builder = new StringBuilder(id.toString());
        builder.append(" x").append(stack.getCount());
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            builder.append(" name=\"").append(name.getString()).append("\"");
        }
        if (stack.get(DataComponents.FOOD) != null) {
            builder.append(" [has food]");
        }
        if (stack.get(DataComponents.CONSUMABLE) != null) {
            builder.append(" [has consumable]");
        }
        return builder.toString();
    }

    public FoodProperties buildFoodProperties() {
        return new FoodProperties(nutrition, saturation, alwaysEat);
    }

    public Consumable buildConsumable(List<ConsumeEffect> statusEffects) {
        Consumable.Builder builder = Consumable.builder()
                .consumeSeconds(Math.max(0.05f, consumeSeconds))
                .hasConsumeParticles(hasConsumeParticles)
                .animation(animation);

        consumeSound.ifPresent(builder::sound);

        for (ConsumeEffect effect : preservedOtherEffects) {
            builder.onConsume(effect);
        }
        for (ConsumeEffect effect : statusEffects) {
            builder.onConsume(effect);
        }
        return builder.build();
    }

    public Optional<UseRemainder> buildUseRemainder(Optional<ItemStack> convertStack) {
        return convertStack.map(UseRemainder::new);
    }

    public record FoodEffectData(MobEffectInstance effect, float probability) {
        public FoodEffectData {
            Objects.requireNonNull(effect, "effect");
            probability = Float.isFinite(probability) ? probability : 0f;
        }

        public FoodEffectData copy() {
            return new FoodEffectData(new MobEffectInstance(effect), probability);
        }
    }
}


