package com.github.rinorsi.cadeditor.client.screen.controller.entry.entity;

import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeValuesEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.entity.VillagerTradeValuesEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

public class VillagerTradeValuesEntryController extends EntryController<VillagerTradeValuesEntryModel, VillagerTradeValuesEntryView> {
    private final DecimalFormat floatFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private final VillagerTradeEntryModel tradeModel;

    public VillagerTradeValuesEntryController(VillagerTradeValuesEntryModel model, VillagerTradeValuesEntryView view) {
        super(model, view);
        this.tradeModel = model.getTradeModel();
    }

    @Override
    public void bind() {
        super.bind();
        view.setListButtonsVisible(false);
        updateTitle(model.getListIndex());
        model.listIndexProperty().addListener(value -> {
            view.setListButtonsVisible(false);
            updateTitle(value);
        });

        configureIntegerField(view.getMaxUsesField(), tradeModel::setMaxUses, 1);
        configureIntegerField(view.getUsesField(), tradeModel::setUses, 0);
        configureIntegerField(view.getDemandField(), tradeModel::setDemand, null);
        configureIntegerField(view.getSpecialPriceField(), tradeModel::setSpecialPrice, null);
        configureFloatField(view.getPriceMultiplierField(), tradeModel::setPriceMultiplier);
        configureIntegerField(view.getXpField(), tradeModel::setXp, 0);

        tradeModel.maxUsesProperty().addListener(value -> syncIntegerField(view.getMaxUsesField(), value));
        tradeModel.usesProperty().addListener(value -> syncIntegerField(view.getUsesField(), value));
        tradeModel.demandProperty().addListener(value -> syncIntegerField(view.getDemandField(), value));
        tradeModel.specialPriceProperty().addListener(value -> syncIntegerField(view.getSpecialPriceField(), value));
        tradeModel.priceMultiplierProperty().addListener(value -> syncFloatField(view.getPriceMultiplierField(), value));
        tradeModel.xpProperty().addListener(value -> syncIntegerField(view.getXpField(), value));

        view.getRewardExpBox().checkedProperty().bindBidirectional(tradeModel.rewardExpProperty());

        refreshFieldTexts();
        updateEditorValidity();
    }

    private void updateTitle(int index) {
        view.getTradeTitleLabel().setLabel(ModTexts.trade(index + 1));
    }

    private void configureIntegerField(TextField field, Consumer<Integer> consumer, Integer minValue) {
        field.setValidator(text -> {
            try {
                int value = Integer.parseInt(text);
                return minValue == null || value >= minValue;
            } catch (NumberFormatException ex) {
                return false;
            }
        });
        field.textProperty().addListener(text -> {
            if (field.isValid()) {
                consumer.accept(Integer.parseInt(text));
            }
            updateEditorValidity();
        });
    }

    private void configureFloatField(TextField field, Consumer<Float> consumer) {
        field.setValidator(text -> {
            try {
                return Float.isFinite(Float.parseFloat(text));
            } catch (NumberFormatException ex) {
                return false;
            }
        });
        field.textProperty().addListener(text -> {
            if (field.isValid()) {
                consumer.accept(Float.parseFloat(text));
            }
            updateEditorValidity();
        });
    }

    private void refreshFieldTexts() {
        view.getMaxUsesField().setText(Integer.toString(tradeModel.getMaxUses()));
        view.getUsesField().setText(Integer.toString(tradeModel.getUses()));
        view.getDemandField().setText(Integer.toString(tradeModel.getDemand()));
        view.getSpecialPriceField().setText(Integer.toString(tradeModel.getSpecialPrice()));
        view.getPriceMultiplierField().setText(floatFormat.format(tradeModel.getPriceMultiplier()));
        view.getXpField().setText(Integer.toString(tradeModel.getXp()));
    }

    private void syncIntegerField(TextField field, int value) {
        String text = Integer.toString(value);
        if (!text.equals(field.getText())) {
            field.setText(text);
        }
    }

    private void syncFloatField(TextField field, Float value) {
        String text = floatFormat.format(value == null ? 0f : value);
        if (!text.equals(field.getText())) {
            field.setText(text);
        }
    }

    private void updateEditorValidity() {
        boolean fieldsValid = view.getMaxUsesField().isValid()
                && view.getUsesField().isValid()
                && view.getDemandField().isValid()
                && view.getSpecialPriceField().isValid()
                && view.getPriceMultiplierField().isValid()
                && view.getXpField().isValid();
        tradeModel.setEditorFieldsValid(fieldsValid);
    }

    @Override
    protected void resetModel() {
        super.resetModel();
        refreshFieldTexts();
        updateEditorValidity();
        view.setListButtonsVisible(false);
    }
}
