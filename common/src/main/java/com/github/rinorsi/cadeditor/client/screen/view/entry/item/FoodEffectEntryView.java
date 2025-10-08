package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.builder.HBoxBuilder;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.textField;

public class FoodEffectEntryView extends PotionEffectEntryView {
    private TextField probabilityField;

    @Override
    public void build() {
        super.build();
        if (probabilityField != null) {
            probabilityField.setPlaceholder(ModTexts.gui("effect_chance"));
        }
    }

    @Override
    protected void addExtraBottomInputsBeforeAmplifier(HBoxBuilder bottom) {
        bottom.add(probabilityField = textField().prefHeight(16).tooltip(ModTexts.gui("effect_chance")), 1);
    }

    public TextField getProbabilityField() {
        return probabilityField;
    }
}
