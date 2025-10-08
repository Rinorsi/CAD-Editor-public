package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 仅负责“地图标记”编辑的分类，提供更高的行高，避免影响地图其它选项的行距。
 */
public class ItemMapDecorationsCategoryModel extends ItemEditorCategoryModel {
    private MapDecorationEntryModel decorationEntry;

    public ItemMapDecorationsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.MAP_DECORATION, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        MapDecorations decorations = stack.get(DataComponents.MAP_DECORATIONS);
        if (decorations != null && !decorations.decorations().isEmpty()) {
            Map.Entry<String, MapDecorations.Entry> firstDecoration = decorations.decorations().entrySet().iterator().next();
            decorationEntry = new MapDecorationEntryModel(this, firstDecoration.getKey(), firstDecoration.getValue());
        } else {
            decorationEntry = new MapDecorationEntryModel(this);
        }
        getEntries().add(decorationEntry);
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();

        HolderLookup.RegistryLookup<MapDecorationType> decorationLookup = ClientUtil.registryAccess()
                .lookup(Registries.MAP_DECORATION_TYPE)
                .orElse(null);

        Map<String, MapDecorations.Entry> parsedDecorations = new LinkedHashMap<>();
        boolean hasInvalid = false;
        if (decorationEntry != null) {
            Optional<Map.Entry<String, MapDecorations.Entry>> parsed = decorationEntry.buildDecoration(decorationLookup);
            if (decorationEntry.isBlank()) {
                // nothing to persist
            } else if (parsed.isPresent()) {
                parsedDecorations.put(parsed.get().getKey(), parsed.get().getValue());
            } else {
                hasInvalid = true;
            }
        }
        if (hasInvalid) {
            return;
        }
        if (parsedDecorations.isEmpty()) {
            stack.remove(DataComponents.MAP_DECORATIONS);
        } else {
            stack.set(DataComponents.MAP_DECORATIONS, new MapDecorations(parsedDecorations));
        }
    }

    @Override
    public int getEntryHeight() {
        return 120;
    }
}

