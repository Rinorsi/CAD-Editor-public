package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ArmorColorEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class ItemMapCategoryModel extends ItemEditorCategoryModel {
    private boolean mapIdEnabled;
    private int mapIdValue;
    private boolean mapColorEnabled;
    private int mapColorValue;
    private boolean mapLocked;

    public ItemMapCategoryModel(ItemEditorModel editor) {
        super(ModTexts.MAP, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        MapId idComponent = stack.get(DataComponents.MAP_ID);
        mapIdEnabled = idComponent != null;
        mapIdValue = idComponent != null ? idComponent.id() : 0;
        MapItemColor colorComponent = stack.get(DataComponents.MAP_COLOR);
        mapColorEnabled = colorComponent != null;
        mapColorValue = colorComponent != null ? colorComponent.rgb() : MapItemColor.DEFAULT.rgb();
        mapLocked = stack.get(DataComponents.MAP_POST_PROCESSING) == MapPostProcessing.LOCK;

        getEntries().add(new BooleanEntryModel(this, ModTexts.MAP_ID_TOGGLE, mapIdEnabled,
                value -> mapIdEnabled = value != null && value));
        getEntries().add(new IntegerEntryModel(this, ModTexts.MAP_ID_VALUE, mapIdValue,
                value -> mapIdValue = value == null ? 0 : value));
        ArmorColorEntryModel colorEntry = new ArmorColorEntryModel(this, mapColorEnabled ? mapColorValue : Color.NONE,
                value -> {
                    if (value == Color.NONE) {
                        mapColorEnabled = false;
                        mapColorValue = MapItemColor.DEFAULT.rgb();
                    } else {
                        mapColorEnabled = true;
                        mapColorValue = value;
                    }
                });
        colorEntry.setLabel(ModTexts.MAP_COLOR);
        getEntries().add(colorEntry);
        getEntries().add(new BooleanEntryModel(this, ModTexts.MAP_LOCK, mapLocked,
                value -> mapLocked = value != null && value));

    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (mapIdEnabled) {
            stack.set(DataComponents.MAP_ID, new MapId(mapIdValue));
        } else {
            stack.remove(DataComponents.MAP_ID);
        }
        if (mapColorEnabled) {
            stack.set(DataComponents.MAP_COLOR, new MapItemColor(mapColorValue));
        } else {
            stack.remove(DataComponents.MAP_COLOR);
        }
        if (mapLocked) {
            stack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.LOCK);
        } else {
            stack.remove(DataComponents.MAP_POST_PROCESSING);
        }

        CompoundTag data = getData();
        if (data != null) {
            CompoundTag components = data.getCompound("components").orElse(null);
            if (components != null) {
                components.remove("minecraft:map_id");
                components.remove("minecraft:map_post_processing");
                components.remove("minecraft:map_color");
                components.remove("minecraft:map_decorations");
                if (components.isEmpty()) {
                    data.remove("components");
                }
            }
        }
    }

}

