package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.CheckBox;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.SpriteView;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.ToggleButton;
import com.github.franckyi.guapi.api.node.builder.HBoxBuilder;
import com.github.franckyi.guapi.api.util.Predicates;
import com.github.rinorsi.cadeditor.client.screen.view.entry.SelectionEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class PotionEffectEntryView extends SelectionEntryView {
    private TextField amplifierField;
    private TextField durationField;
    private CheckBox ambientBox;
    private CheckBox showParticlesBox;
    private CheckBox showIconBox;
    private ToggleButton durationUnitToggle;
    private SpriteView previewSpriteView;
    private Label previewLabel;
    private HBox previewBox;

    @Override
    public void build() {
        super.build();
        getTextField().setPlaceholder(ModTexts.EFFECT);
        previewBox = hBox(preview -> {
            preview.add(previewSpriteView = spriteView(sprite -> sprite.imageWidth(18).imageHeight(18)));
            preview.add(previewLabel = label().prefHeight(16));
            preview.align(CENTER_LEFT).spacing(4);
        });
        setPreview(previewBox);
        setPreviewVisible(false);
    }

    @Override
    protected Node createLabeledContent() {
        return vBox(root -> {
            root.add(super.createLabeledContent());
            root.add(hBox(bottom -> {
                addExtraBottomInputsBeforeAmplifier(bottom);
                bottom.add(amplifierField = textField().prefHeight(16).validator(Predicates.range(0, Integer.MAX_VALUE))
                        .tooltip(ModTexts.AMPLIFIER), 1);
                bottom.add(durationField = textField().prefHeight(16).validator(Predicates.range(1, Integer.MAX_VALUE))
                        .tooltip(ModTexts.DURATION), 1);
                addExtraBottomInputsBeforeUnitToggle(bottom);
                bottom.add(durationUnitToggle = toggleButton(ModTexts.TICKS).prefHeight(16).prefWidth(44));
                addExtraBottomInputs(bottom);
                bottom.add(hBox(boxes -> {
                    boxes.add(ambientBox = checkBox().tooltip(ModTexts.AMBIENT));
                    boxes.add(showParticlesBox = checkBox().tooltip(ModTexts.SHOW_PARTICLES));
                    boxes.add(showIconBox = checkBox().tooltip(ModTexts.SHOW_ICON));
                    boxes.spacing(5);
                }));
                bottom.spacing(5);
            }));
            root.spacing(5).fillWidth();
        });
    }

    protected void addExtraBottomInputsBeforeAmplifier(HBoxBuilder bottom) {
    }

    protected void addExtraBottomInputsBeforeUnitToggle(HBoxBuilder bottom) {
    }

    protected void addExtraBottomInputs(HBoxBuilder bottom) {
    }

    public TextField getAmplifierField() {
        return amplifierField;
    }

    public TextField getDurationField() {
        return durationField;
    }

    public CheckBox getAmbientBox() {
        return ambientBox;
    }

    public CheckBox getShowParticlesBox() {
        return showParticlesBox;
    }

    public CheckBox getShowIconBox() {
        return showIconBox;
    }

    public ToggleButton getDurationUnitToggle() {
        return durationUnitToggle;
    }

    public SpriteView getPreviewSpriteView() {
        return previewSpriteView;
    }

    public Label getPreviewLabel() {
        return previewLabel;
    }

    public HBox getPreviewBox() {
        return previewBox;
    }
}
