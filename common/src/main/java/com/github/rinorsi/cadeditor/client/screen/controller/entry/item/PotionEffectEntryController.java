package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.SelectionEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.PotionEffectEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.PotionEffectEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.franckyi.guapi.api.util.Predicates;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.github.franckyi.guapi.api.GuapiHelper.translated;

public class PotionEffectEntryController extends SelectionEntryController<PotionEffectEntryModel, PotionEffectEntryView> {
    private final DecimalFormat secondsFormat = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public PotionEffectEntryController(PotionEffectEntryModel model, PotionEffectEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        updateAmplifierView();
        updateDurationView();
        view.getAmbientBox().checkedProperty().bindBidirectional(model.ambientProperty());
        view.getShowParticlesBox().checkedProperty().bindBidirectional(model.showParticlesProperty());
        view.getShowIconBox().checkedProperty().bindBidirectional(model.showIconProperty());
        view.getDurationUnitToggle().activeProperty().bindBidirectional(model.useSecondsProperty());
        view.getDurationUnitToggle().activeProperty().addListener(this::updateDurationUnit);
        view.getAmplifierField().textProperty().addListener(value -> {
            if (view.getAmplifierField().isValid()) {
                model.setAmplifier(Integer.parseInt(value));
            }
        });
        model.amplifierProperty().addListener(this::updateAmplifierView);
        view.getDurationField().textProperty().addListener(value -> {
            if (view.getDurationField().isValid()) {
                if (model.isUseSeconds()) {
                    double seconds = Double.parseDouble(value);
                    if (Double.isFinite(seconds)) {
                        model.setDuration(secondsToTicks(seconds));
                    }
                } else {
                    model.setDuration(Integer.parseInt(value));
                }
            }
        });
        model.durationProperty().addListener(this::updateDurationView);
        view.getAmplifierField().validProperty().addListener(this::updateValidity);
        view.getDurationField().validProperty().addListener(this::updateValidity);
        model.valueProperty().addListener(this::updatePreview);
        updateDurationUnit();
        updatePreview(model.getValue());
    }

    private void updateValidity() {
        model.setValid(view.getAmplifierField().isValid() && view.getDurationField().isValid());
    }

    private void updateAmplifierView() {
        view.getAmplifierField().setText(Integer.toString(model.getAmplifier()));
    }

    private void updateDurationView() {
        if (model.isUseSeconds()) {
            view.getDurationField().setText(secondsFormat.format(model.getDuration() / 20d));
        } else {
            view.getDurationField().setText(Integer.toString(model.getDuration()));
        }
    }

    private void updateDurationUnit() {
        view.getDurationUnitToggle().setLabel(model.isUseSeconds() ? ModTexts.SECONDS : ModTexts.TICKS);
        updateDurationValidator();
        updateDurationView();
    }

    private void updateDurationValidator() {
        if (model.isUseSeconds()) {
            view.getDurationField().setValidator(value -> {
                try {
                    double seconds = Double.parseDouble(value);
                    return Double.isFinite(seconds) && seconds > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
        } else {
            view.getDurationField().setValidator(Predicates.range(1, Integer.MAX_VALUE));
        }
    }

    private int secondsToTicks(double seconds) {
        return Math.max(1, (int) Math.round(seconds * 20d));
    }

    private void updatePreview(String value) {
        ResourceLocation id = parseResourceLocation(value);
        if (id == null) {
            view.setPreviewVisible(false);
            return;
        }
        ClientCache.findEffectSelectionItem(id).ifPresentOrElse(effect -> {
            view.getPreviewSpriteView().setSpriteFactory(effect.getSpriteFactory());
            view.getPreviewLabel().setLabel(translated(effect.getName()).withStyle(ChatFormatting.GRAY));
            view.setPreviewVisible(true);
        }, () -> view.setPreviewVisible(false));
    }
}
