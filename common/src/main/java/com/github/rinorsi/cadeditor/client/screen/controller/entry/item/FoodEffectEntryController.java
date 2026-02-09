package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodEffectEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.FoodEffectEntryView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FoodEffectEntryController extends PotionEffectEntryController {
    private final DecimalFormat probabilityFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public FoodEffectEntryController(FoodEffectEntryModel model, FoodEffectEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        FoodEffectEntryView foodView = getFoodView();
        FoodEffectEntryModel foodModel = getFoodModel();
        foodView.getProbabilityField().setValidator(this::isProbabilityValid);
        foodView.getProbabilityField().textProperty().addListener(value -> {
            if (foodView.getProbabilityField().isValid()) {
                foodModel.setProbability(parseProbability(value));
            }
        });
        foodModel.probabilityProperty().addListener(value -> updateProbabilityView(foodModel, foodView));
        foodView.getProbabilityField().validProperty().addListener(() -> updateFoodValidity(foodModel, foodView));
        foodView.getAmplifierField().validProperty().addListener(() -> updateFoodValidity(foodModel, foodView));
        foodView.getDurationField().validProperty().addListener(() -> updateFoodValidity(foodModel, foodView));
        updateProbabilityView(foodModel, foodView);
        updateFoodValidity(foodModel, foodView);
    }

    private void updateProbabilityView(FoodEffectEntryModel model, FoodEffectEntryView view) {
        view.getProbabilityField().setText(probabilityFormat.format(model.getProbability()));
    }

    private void updateFoodValidity(FoodEffectEntryModel model, FoodEffectEntryView view) {
        boolean valid = view.getAmplifierField().isValid() && view.getDurationField().isValid()
                && view.getProbabilityField().isValid();
        model.setValid(valid);
    }

    private boolean isProbabilityValid(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) && parsed >= 0d && parsed <= 1d;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double parseProbability(String value) {
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) {
                return 0d;
            }
            if (parsed < 0d) {
                return 0d;
            }
            if (parsed > 1d) {
                return 1d;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private FoodEffectEntryModel getFoodModel() {
        return (FoodEffectEntryModel) model;
    }

    private FoodEffectEntryView getFoodView() {
        return (FoodEffectEntryView) view;
    }
}
