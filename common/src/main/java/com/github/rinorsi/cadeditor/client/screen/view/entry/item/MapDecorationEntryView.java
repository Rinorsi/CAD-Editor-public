package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.EnumButton;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel.DecorationTypeOption;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class MapDecorationEntryView extends EntryView {
    private static final int LABEL_WIDTH = 110;
    private static final int FIELD_PREF_WIDTH = 180;

    private TextField nameField;
    private TextField xField;
    private TextField zField;
    private TextField rotationField;
    private EnumButton<DecorationTypeOption> typeSelector;

    @Override
    protected Node createContent() {
        return vBox(content -> {
            content.add(buildRow(ModTexts.MAP_DECORATION_NAME,
                    nameField = sizedTextField(ModTexts.MAP_DECORATION_NAME)));

            content.add(buildRow(ModTexts.MAP_DECORATION_X,
                    xField = sizedTextField(ModTexts.MAP_DECORATION_X)));
            content.add(buildRow(ModTexts.MAP_DECORATION_Z,
                    zField = sizedTextField(ModTexts.MAP_DECORATION_Z)));
            content.add(buildRow(ModTexts.MAP_DECORATION_ROTATION,
                    rotationField = sizedTextField(ModTexts.MAP_DECORATION_ROTATION)));

            content.add(buildRow(ModTexts.MAP_DECORATION_TYPE,
                    typeSelector = enumButton(DecorationTypeOption.defaults())
                            .prefHeight(16)
                            .prefWidth(FIELD_PREF_WIDTH)
                            .maxWidth(Integer.MAX_VALUE)
                            .textFactory(DecorationTypeOption::getDisplayName)
                            .tooltip(ModTexts.MAP_DECORATION_TYPE)
            ));

            content.spacing(4);
        })
        .maxWidth(Integer.MAX_VALUE);
    }

    private TextField sizedTextField(Component placeholderText) {
        return textField()
                .prefHeight(16)
                .prefWidth(FIELD_PREF_WIDTH)
                .maxWidth(Integer.MAX_VALUE)
                .placeholder(placeholderText);
    }

    private Node buildRow(Component labelText, Node field) {
        return hBox(row -> {
            row.add(createLabel(labelText));
            row.add(field, 1);
            row.spacing(6).align(CENTER_LEFT);
        })
        .maxWidth(Integer.MAX_VALUE);
    }

    private Label createLabel(Component labelText) {
        return label()
                .label(labelText)
                .prefWidth(LABEL_WIDTH)
                .textAlign(CENTER_RIGHT);
    }

    public void setTypeOptions(List<DecorationTypeOption> options) {
        DecorationTypeOption current = typeSelector.getValue();
        typeSelector.getValues().setAll(options);
        if (options.isEmpty()) {
            typeSelector.setValue(null);
            return;
        }
        if (current != null) {
            DecorationTypeOption matched = DecorationTypeOption.find(options, current.getResourceId());
            if (matched != null) {
                typeSelector.setValue(matched);
                return;
            }
        }
        typeSelector.setValue(options.get(0));
    }

    public TextField getNameField() { return nameField; }
    public TextField getXField() { return xField; }
    public TextField getZField() { return zField; }
    public TextField getRotationField() { return rotationField; }
    public EnumButton<DecorationTypeOption> getTypeSelector() { return typeSelector; }
}
