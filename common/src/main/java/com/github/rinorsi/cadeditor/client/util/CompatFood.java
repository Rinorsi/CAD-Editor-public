package com.github.rinorsi.cadeditor.client.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

public final class CompatFood {
    private static final Logger LOGGER = LogManager.getLogger();
    private CompatFood() {}

    public static Optional<FoodProperties.PossibleEffect> makePossibleEffect(MobEffectInstance inst, float prob) {
        if (inst == null) return Optional.empty();
        float p = Float.isFinite(prob) ? prob : 0f;
        if (p <= 0f) return Optional.empty();
        if (p > 1f) p = 1f;

        Class<FoodProperties.PossibleEffect> c = FoodProperties.PossibleEffect.class;
        try {
            try {
                Method m = c.getDeclaredMethod("create", MobEffectInstance.class, float.class);
                m.setAccessible(true);
                FoodProperties.PossibleEffect pe = c.cast(m.invoke(null, inst, p));
                return Optional.of(pe);
            } catch (NoSuchMethodException ignore) {}

            try {
                Method m = c.getDeclaredMethod("of", MobEffectInstance.class, float.class);
                m.setAccessible(true);
                FoodProperties.PossibleEffect pe = c.cast(m.invoke(null, inst, p));
                return Optional.of(pe);
            } catch (NoSuchMethodException ignore) {}

            Constructor<FoodProperties.PossibleEffect> ctor =
                    c.getDeclaredConstructor(MobEffectInstance.class, float.class);
            ctor.setAccessible(true);
            return Optional.of(ctor.newInstance(inst, p));
        } catch (Throwable t) {
            LOGGER.error("Failed to construct FoodProperties.PossibleEffect reflectively: {}", t.toString());
            return Optional.empty();
        }
    }
}
