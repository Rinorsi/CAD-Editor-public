package com.github.rinorsi.cadeditor.common.loot;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Cross‑platform cache of all loaded loot table IDs, populated by platform-specific hooks.
 */
public final class LootTableIndex {
    private static final Set<ResourceLocation> IDS = new ConcurrentSkipListSet<>();

    private LootTableIndex() {}

    public static void updateAll(Collection<ResourceLocation> ids) {
        IDS.clear();
        if (ids != null) IDS.addAll(ids);
    }

    public static List<ResourceLocation> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(IDS));
    }

    public static boolean isEmpty() { return IDS.isEmpty(); }
}

