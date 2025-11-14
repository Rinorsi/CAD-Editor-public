package com.github.rinorsi.cadeditor.neoforge.loot;

import com.github.rinorsi.cadeditor.common.loot.LootTableIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class ForgeLootTableTracker {
    private ForgeLootTableTracker() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ForgeLootTableTracker::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ForgeLootTableTracker::onServerStopping);
        NeoForge.EVENT_BUS.addListener(ForgeLootTableTracker::onAddReloadListener);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        updateIndex(event.getServer());
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        LootTableIndex.updateAll(Collections.emptyList());
    }

    private static void onAddReloadListener(AddReloadListenerEvent event) {
        final ReloadableServerResources resources = event.getServerResources();
        PreparableReloadListener listener = new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(@NotNull net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                            @NotNull net.minecraft.util.profiling.ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(@NotNull Void unused,
                                 @NotNull net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                 @NotNull net.minecraft.util.profiling.ProfilerFiller profiler) {
                updateIndex(resources);
            }
        };
        event.addListener(listener);
    }

    private static void updateIndex(MinecraftServer server) {
        if (server == null) {
            LootTableIndex.updateAll(Collections.emptyList());
            return;
        }
        updateIndex(server.reloadableRegistries());
    }

    private static void updateIndex(ReloadableServerResources resources) {
        if (resources == null) {
            LootTableIndex.updateAll(Collections.emptyList());
            return;
        }
        updateIndex(resources.fullRegistries());
    }

    private static void updateIndex(ReloadableServerRegistries.Holder registries) {
        if (registries == null) {
            LootTableIndex.updateAll(Collections.emptyList());
            return;
        }
        try {
            Collection<ResourceLocation> keys = registries.getKeys(Registries.LOOT_TABLE);
            LootTableIndex.updateAll(new ArrayList<>(keys));
        } catch (Throwable ignored) {
            LootTableIndex.updateAll(Collections.emptyList());
        }
    }
}
