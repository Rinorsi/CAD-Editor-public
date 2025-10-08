package com.github.rinorsi.cadeditor.fabric.loot;

import com.github.rinorsi.cadeditor.common.loot.LootTableIndex;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public final class FabricLootTableTracker {
    private FabricLootTableTracker() {}

    public static void register() {
        // Fires when all loot tables are loaded/reloaded.
        LootTableEvents.ALL_LOADED.register((resourceManager, registry) -> updateIndex(registry));
    }

    private static void updateIndex(Registry<LootTable> registry) {
        try {
            List<ResourceLocation> ids = new ArrayList<>();
            registry.keySet().forEach(ids::add);
            LootTableIndex.updateAll(ids);
        } catch (Throwable ignored) {
            // Keep index empty if anything goes wrong; UI will fall back to other strategies.
        }
    }
}

