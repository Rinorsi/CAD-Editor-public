package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.LootTableListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

public class LootTableSelectionEntryModel extends SelectionEntryModel {
    public LootTableSelectionEntryModel(CategoryModel category, String value, Consumer<String> action) {
        super(category, ModTexts.LOOT_TABLE, value, action);
    }

    @Override
    public List<String> getSuggestions() {
        return List.of("minecraft:chests/", "minecraft:entities/", "minecraft:blocks/", "minecraft:gameplay/");
    }

    @Override
    public net.minecraft.network.chat.MutableComponent getSelectionScreenTitle() {
        return ModTexts.LOOT_TABLE;
    }

    @SuppressWarnings("unused")
    @Override
    public List<? extends ListSelectionElementModel> getSelectionItems() {
        List<ListSelectionElementModel> out = new ArrayList<>();

        // 0) Use platform-provided index if available (authoritative)
        try {
            var indexed = com.github.rinorsi.cadeditor.common.loot.LootTableIndex.getAll();
            if (!indexed.isEmpty()) {
                for (var id : indexed) {
                    out.add(new LootTableListSelectionElementModel(id));
                }
            }
        } catch (Throwable ignored) {}

        if (out.isEmpty()) try {
            var mc = Minecraft.getInstance();
            var srv = mc.getSingleplayerServer();
            if (srv != null) {
                var rm = srv.getResourceManager();
                var map = rm.listResources("loot_tables", rl -> rl.getPath().endsWith(".json"));
                for (var entry : map.keySet()) {
                    String path = entry.getPath();
                    if (!path.startsWith("loot_tables/")) continue;
                    String clean = path.substring("loot_tables/".length(), path.length() - ".json".length());
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(entry.getNamespace(), clean);
                    out.add(new LootTableListSelectionElementModel(id));
                }
            }
        } catch (Throwable ignored) {}

        if (out.isEmpty()) {
            try {
                Class<?> helper = Class.forName("net.fabricmc.fabric.api.resource.ResourceManagerHelper");
                Class<?> rtype = Class.forName("net.fabricmc.api.EnvType"); // not needed, but keep reflective scope small
                Class<?> resType = Class.forName("net.fabricmc.fabric.api.resource.ResourceType");
                var serverDataField = resType.getField("SERVER_DATA");
                Object SERVER_DATA = serverDataField.get(null);
                var getMethod = helper.getMethod("get", resType);
                Object helperInst = getMethod.invoke(null, SERVER_DATA);
                var getRm = helperInst.getClass().getMethod("getResourceManager");
                Object rm = getRm.invoke(helperInst);
                var listResources = rm.getClass().getMethod("listResources", String.class, java.util.function.Predicate.class);
                @SuppressWarnings("unchecked")
                var map = (java.util.Map<ResourceLocation, Object>) listResources.invoke(rm, "loot_tables", (java.util.function.Predicate<ResourceLocation>) rl -> rl.getPath().endsWith(".json"));
                for (var entry : map.keySet()) {
                    String path = entry.getPath();
                    if (!path.startsWith("loot_tables/")) continue;
                    String clean = path.substring("loot_tables/".length(), path.length() - ".json".length());
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(entry.getNamespace(), clean);
                    out.add(new LootTableListSelectionElementModel(id));
                }
            } catch (Throwable ignored) {}
        }

        if (out.isEmpty()) {
            try {
                var loader = Class.forName("net.fabricmc.loader.api.FabricLoader");
                var getInstance = loader.getMethod("getInstance");
                Object fl = getInstance.invoke(null);
                var getAllMods = fl.getClass().getMethod("getAllMods");
                @SuppressWarnings("unchecked")
                var mods = (java.util.Collection<Object>) getAllMods.invoke(fl);
                for (Object mod : mods) {
                    var getOrigin = mod.getClass().getMethod("getOrigin");
                    Object origin = getOrigin.invoke(mod);
                    var getPaths = origin.getClass().getMethod("getPaths");
                    @SuppressWarnings("unchecked")
                    var paths = (java.util.List<java.nio.file.Path>) getPaths.invoke(origin);
                    for (var root : paths) {
                        try {
                            java.nio.file.FileSystem fs = null;
                            java.nio.file.Path base = root;
                            if (java.nio.file.Files.isRegularFile(root)) {
                                fs = java.nio.file.FileSystems.newFileSystem(root, (ClassLoader) null);
                                base = fs.getPath("/");
                            }
                            java.nio.file.Path dataDir = base.resolve("data");
                            if (java.nio.file.Files.exists(dataDir)) {
                                try (var walk = java.nio.file.Files.walk(dataDir)) {
                                    walk.filter(p -> {
                                            String s = p.toString().replace('\\','/');
                                            return p.getFileName().toString().endsWith(".json") && s.contains("/loot_tables/");
                                        })
                                        .forEach(p -> {
                                            try {
                                                // data/<ns>/loot_tables/<path>.json -> <ns>:<path>
                                                int idx = p.toString().indexOf("/loot_tables/");
                                                if (idx <= 0) return;
                                                // namespace = child of data
                                                java.nio.file.Path rel = dataDir.relativize(p);
                                                String namespace = rel.getName(0).toString();
                                                String sub = rel.subpath(1, rel.getNameCount()).toString().replace('\\','/');
                                                if (!sub.startsWith("loot_tables/")) return;
                                                String clean = sub.substring("loot_tables/".length(), sub.length() - ".json".length());
                                                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, clean);
                                                out.add(new LootTableListSelectionElementModel(id));
                                            } catch (Throwable ignored2) {}
                                        });
                                }
                            }
                            if (fs != null) fs.close();
                        } catch (Throwable ignoredPath) {}
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (out.isEmpty()) {
            try {
                java.nio.file.Path runDir = java.nio.file.Paths.get("fabric", "run");
                java.util.List<java.nio.file.Path> candidates = new java.util.ArrayList<>();
                candidates.add(runDir.resolve(".fabric").resolve("processedMods"));
                candidates.add(runDir.resolve("mods"));
                for (var dir : candidates) {
                    if (!java.nio.file.Files.exists(dir)) continue;
                    try (var stream = java.nio.file.Files.list(dir)) {
                        for (var path : (Iterable<java.nio.file.Path>) stream::iterator) {
                            try {
                                if (java.nio.file.Files.isDirectory(path)) {
                                    var dataDir = path.resolve("data");
                                    if (java.nio.file.Files.exists(dataDir)) {
                                        try (var walk = java.nio.file.Files.walk(dataDir)) {
                                            walk.filter(p -> {
                                                    String s = p.toString().replace('\\','/');
                                                    return p.getFileName().toString().endsWith(".json") && s.contains("/loot_tables/");
                                                })
                                                .forEach(p -> {
                                                    try {
                                                        java.nio.file.Path rel = dataDir.relativize(p);
                                                        String namespace = rel.getName(0).toString();
                                                        String sub = rel.subpath(1, rel.getNameCount()).toString().replace('\\','/');
                                                        if (!sub.startsWith("loot_tables/")) return;
                                                        String clean = sub.substring("loot_tables/".length(), sub.length() - ".json".length());
                                                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, clean);
                                                        out.add(new LootTableListSelectionElementModel(id));
                                                    } catch (Throwable ignored2) {}
                                                });
                                        }
                                    }
                                } else if (java.nio.file.Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar")) {
                                    java.nio.file.FileSystem fs = null;
                                    try {
                                        fs = java.nio.file.FileSystems.newFileSystem(path, (ClassLoader) null);
                                        var dataDir = fs.getPath("data");
                                        if (java.nio.file.Files.exists(dataDir)) {
                                            try (var walk = java.nio.file.Files.walk(dataDir)) {
                                                walk.filter(p -> {
                                                        String s = p.toString().replace('\\','/');
                                                        return p.getFileName().toString().endsWith(".json") && s.contains("/loot_tables/");
                                                    })
                                                    .forEach(p -> {
                                                        try {
                                                            java.nio.file.Path rel = dataDir.relativize(p);
                                                            String namespace = rel.getName(0).toString();
                                                            String sub = rel.subpath(1, rel.getNameCount()).toString().replace('\\','/');
                                                            if (!sub.startsWith("loot_tables/")) return;
                                                            String clean = sub.substring("loot_tables/".length(), sub.length() - ".json".length());
                                                            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, clean);
                                                            out.add(new LootTableListSelectionElementModel(id));
                                                        } catch (Throwable ignored2) {}
                                                    });
                                            }
                                        }
                                    } finally {
                                        if (fs != null) try { fs.close(); } catch (Throwable ignored3) {}
                                    }
                                }
                            } catch (Throwable ignoredEntry) {}
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        // if (out.isEmpty()) {
        //     String ns = "minecraft";
        //     String[] commons = new String[] {
        //         "chests/simple_dungeon", "chests/abandoned_mineshaft", "chests/ancient_city", "chests/desert_pyramid",
        //         "chests/jungle_temple", "chests/shipwreck_supplies", "chests/village/village_weaponsmith",
        //         "entities/zombie", "entities/creeper", "entities/skeleton",
        //         "blocks/oak_log", "blocks/diamond_ore", "gameplay/fishing/fish"
        //     };
        //     for (String p : commons) {
        //         out.add(new ListSelectionElementModel("cadeditor.text.item", ResourceLocation.fromNamespaceAndPath(ns, p)));
        //     }
        // }

        out.sort(Comparator.naturalOrder());
        return out;
    }
}
