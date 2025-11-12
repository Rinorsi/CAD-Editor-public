package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    @Accessor("exhaustionLevel")
    float cadeditor$getExhaustionLevel();

    @Accessor("exhaustionLevel")
    void cadeditor$setExhaustionLevel(float exhaustion);
}

