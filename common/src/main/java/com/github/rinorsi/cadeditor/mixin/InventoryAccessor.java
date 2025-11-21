package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Inventory.class)
public interface InventoryAccessor {
    @Accessor("selected")
    int cadeditor$getSelectedSlot();

    @Accessor("items")
    NonNullList<ItemStack> cadeditor$getItems();
}
