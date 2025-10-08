package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel.DecorationTypeOption;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.MapDecorationEntryView;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapDecorationEntryController extends EntryController<MapDecorationEntryModel, MapDecorationEntryView> {
    private boolean updating = false;
    private List<DecorationTypeOption> typeOptions = List.of();
    private Map<String, DecorationTypeOption> typeOptionById = Map.of();

    public MapDecorationEntryController(MapDecorationEntryModel model, MapDecorationEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();

        view.getNameField().textProperty().addListener(model.nameProperty()::setValue);
        model.nameProperty().addListener(view.getNameField()::setText);
        view.getNameField().setValidator(v -> v == null || v.isBlank() || !v.contains("|"));

        HolderLookup.RegistryLookup<MapDecorationType> decorationLookup = ClientUtil.registryAccess()
                .lookup(Registries.MAP_DECORATION_TYPE)
                .orElse(null);
        typeOptions = DecorationTypeOption.collect(decorationLookup);
        view.setTypeOptions(typeOptions);
        typeOptions = List.copyOf(view.getTypeSelector().getValues());
        typeOptionById = typeOptions.stream()
                .collect(LinkedHashMap::new, (map, option) -> map.put(option.getResourceId(), option), Map::putAll);

        view.getTypeSelector().valueProperty().addListener(val -> {
            if (updating) return;
            if (val == null) return;
            String id = val.getResourceId();
            if (id == null || id.isBlank()) return;

            updating = true;
            model.typeProperty().setValue(id);
            updating = false;
        });

        model.typeProperty().addListener(value -> {
            if (updating) return;
            if (value == null || value.isBlank()) return; 

            DecorationTypeOption option = resolveOption(value);
            if (option == null) return;

            if (option != view.getTypeSelector().getValue()) {
                updating = true;
                view.getTypeSelector().setValue(option);
                updating = false;
            }
        });

        view.getXField().textProperty().addListener(model.xTextProperty()::setValue);
        model.xTextProperty().addListener(view.getXField()::setText);

        view.getZField().textProperty().addListener(model.zTextProperty()::setValue);
        model.zTextProperty().addListener(view.getZField()::setText);

        view.getRotationField().textProperty().addListener(model.rotationTextProperty()::setValue);
        model.rotationTextProperty().addListener(view.getRotationField()::setText);

        view.getNameField().setText(model.nameProperty().getValue());
        view.getXField().setText(model.xTextProperty().getValue());
        view.getZField().setText(model.zTextProperty().getValue());
        view.getRotationField().setText(model.rotationTextProperty().getValue());

        DecorationTypeOption initType = null;
        String typeId = model.typeProperty().getValue();
        if (typeId != null && !typeId.isBlank()) {
            initType = resolveOption(typeId);
        }

        updating = true;
        if (initType != null) {
            view.getTypeSelector().setValue(initType);
        } else {
            DecorationTypeOption fallback = view.getTypeSelector().getValue();
            if (fallback == null && !typeOptions.isEmpty()) {
                fallback = typeOptions.get(0);
                view.getTypeSelector().setValue(fallback);
            }
            if (fallback != null) {
                model.typeProperty().setValue(fallback.getResourceId());
            } else {
                model.typeProperty().setValue("");
            }
        }
        updating = false;
    }

    private DecorationTypeOption resolveOption(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return null;
        }
        DecorationTypeOption direct = typeOptionById.get(resourceId);
        if (direct != null) {
            return direct;
        }
        var normalized = DecorationTypeOption.normalizeId(resourceId);
        if (normalized == null) {
            return null;
        }
        return typeOptionById.get(normalized.toString());
    }
}
