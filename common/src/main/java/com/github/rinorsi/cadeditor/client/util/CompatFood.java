package com.github.rinorsi.cadeditor.client.util;

import com.github.rinorsi.cadeditor.client.screen.model.category.item.FoodComponentState.FoodEffectData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public final class CompatFood {
    private CompatFood() {}

    public static Optional<FoodEffectData> makeApplyEffect(MobEffectInstance instance, float probability) {
        if (instance == null) return Optional.empty();
        float clamped = Mth.clamp(Float.isFinite(probability) ? probability : 0f, 0f, 1f);
        if (clamped <= 0f) return Optional.empty();
        return Optional.of(new FoodEffectData(new MobEffectInstance(instance), clamped));
    }
}

