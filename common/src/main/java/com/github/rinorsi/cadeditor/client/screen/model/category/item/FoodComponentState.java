package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodComponentState {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean enabled;
    private boolean hasEverBeenEnabled;
    private int nutrition = 4;
    private float saturation = 0.3f;
    private boolean alwaysEat;
    private float eatSeconds = 1.6f;
    private String usingConvertsToId = "";
    private Optional<ItemStack> originalUsingConvertsTo = Optional.empty();
    private Optional<ItemStack> customUsingConvertsTo = Optional.empty();
    private List<FoodProperties.PossibleEffect> effects = new ArrayList<>();

    public void loadFrom(ItemStack stack) {
        FoodProperties properties = stack.get(DataComponents.FOOD);
        if (properties != null) {
            hasEverBeenEnabled = true;
            setEnabled(true);
            nutrition = properties.nutrition();
            saturation = properties.saturation();
            alwaysEat = properties.canAlwaysEat();
            eatSeconds = properties.eatSeconds();
            originalUsingConvertsTo = properties.usingConvertsTo().map(this::prepareConvertStack);
            customUsingConvertsTo = originalUsingConvertsTo.map(ItemStack::copy);
            usingConvertsToId = originalUsingConvertsTo
                    .map(ItemStack::getItem)
                    .map(item -> BuiltInRegistries.ITEM.getKey(item).toString())
                    .orElse("");
            effects = new ArrayList<>(properties.effects());
            DebugLog.infoKey("cadeditor.debug.food.loaded", describeStack(stack), usingConvertsToId, originalUsingConvertsTo.map(this::describeStack).orElse("<empty>"));
        } else {
            hasEverBeenEnabled = false;
            setEnabled(false);
            nutrition = 4;
            saturation = 0.3f;
            alwaysEat = false;
            eatSeconds = 1.6f;
            originalUsingConvertsTo = Optional.empty();
            usingConvertsToId = "";
            customUsingConvertsTo = Optional.empty();
            effects = new ArrayList<>();
            DebugLog.infoKey("cadeditor.debug.food.missing", describeStack(stack));
        }
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

    public float getEatSeconds() {
        return eatSeconds;
    }

    public void setEatSeconds(float eatSeconds) {
        this.eatSeconds = Math.max(0.1f, eatSeconds);
    }

    public String getUsingConvertsToId() {
        return usingConvertsToId;
    }

    public void setUsingConvertsToId(String usingConvertsToId) {
        this.usingConvertsToId = usingConvertsToId == null ? "" : usingConvertsToId.trim();
        if (this.usingConvertsToId.isBlank()) {
            customUsingConvertsTo = Optional.empty();
            originalUsingConvertsTo = Optional.empty();
        } else {
            customUsingConvertsTo = filterStackById(customUsingConvertsTo, this.usingConvertsToId);
            originalUsingConvertsTo = filterStackById(originalUsingConvertsTo, this.usingConvertsToId);
        }
    }

    public void prepareForInitialEnable(ItemStack stack) {
        if (hasEverBeenEnabled) {
            return;
        }
        setNutrition(0);
        setSaturation(0f);
        setAlwaysEat(true);
        setEatSeconds(1.6f);
        usingConvertsToId = "";
        originalUsingConvertsTo = Optional.empty();
        customUsingConvertsTo = Optional.empty();
        DebugLog.infoKey("cadeditor.debug.food.prepared", describeStack(stack));
    }

    public List<FoodProperties.PossibleEffect> getEffects() {
        return List.copyOf(effects);
    }

    public void setEffects(List<FoodProperties.PossibleEffect> newEffects) {
        effects = new ArrayList<>(newEffects);
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

    public List<FoodProperties.PossibleEffect> copyEffectsForComponent() {
        if (effects.isEmpty()) {
            return List.of();
        }
        return List.copyOf(effects);
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
        if (id != null) {
            usingConvertsToId = id.toString();
        } else {
            usingConvertsToId = "";
        }
        originalUsingConvertsTo = filterStackById(originalUsingConvertsTo, usingConvertsToId);
    }

    private ItemStack prepareConvertStack(ItemStack source) {
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack sanitized = source.copy();
        int count = Math.max(1, Math.min(64, sanitized.getCount()));
        sanitized.setCount(count);
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
        return builder.toString();
    }
}
