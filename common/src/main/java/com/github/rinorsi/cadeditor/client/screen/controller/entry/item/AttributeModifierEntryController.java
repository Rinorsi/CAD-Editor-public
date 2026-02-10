package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.AttributeModifierEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.AttributeModifierEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import static com.github.franckyi.guapi.api.GuapiHelper.translated;

public class AttributeModifierEntryController extends EntryController<AttributeModifierEntryModel, AttributeModifierEntryView> {

    public AttributeModifierEntryController(AttributeModifierEntryModel model, AttributeModifierEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getAmountField().textProperty().addListener(value -> {
            if (view.getAmountField().isValid()) {
                model.setAmount(Double.parseDouble(value));
            }
        });
        model.amountProperty().addListener(value -> view.getAmountField().setText(Double.toString(value)));
        view.getAttributeNameField().textProperty().bindBidirectional(model.attributeNameProperty());
        view.getSlotButton().valueProperty().bindBidirectional(model.slotProperty());
        view.getOperationButton().valueProperty().bindBidirectional(model.operationProperty());
        view.getAmountField().setText(Double.toString(model.getAmount()));
        view.getAmountField().validProperty().addListener(model::setValid);
        view.getAttributeNameField().getSuggestions().setAll(ClientCache.getAttributeSuggestions());
        view.getAttributeListButton().onAction(this::openAttributeList);
        model.attributeNameProperty().addListener(this::updateAttributePreview);
        updateAttributePreview(model.getAttributeName());
    }

    private void openAttributeList() {
        ModScreenHandler.openListSelectionScreen(ModTexts.ATTRIBUTE,
                model.getAttributeName().contains(":") ? model.getAttributeName() : "minecraft:" + model.getAttributeName(),
                ClientCache.getAttributeSelectionItems(), model::setAttributeName);
    }

    private void updateAttributePreview(String value) {
        Identifier id = ClientUtil.parseResourceLocation(value);
        if (id == null) {
            view.getAttributePreviewLabel().setVisible(false);
            return;
        }
        ClientCache.findAttributeSelectionItem(id).ifPresentOrElse(item -> {
            view.getAttributePreviewLabel().setLabel(translated(item.getName()).withStyle(ChatFormatting.GRAY));
            view.getAttributePreviewLabel().setVisible(true);
        }, () -> view.getAttributePreviewLabel().setVisible(false));
    }

}
