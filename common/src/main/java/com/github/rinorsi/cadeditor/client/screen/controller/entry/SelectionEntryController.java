package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.entry.SelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.SelectionEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.resources.Identifier;

public class SelectionEntryController<M extends SelectionEntryModel, V extends SelectionEntryView> extends StringEntryController<M, V> {
    public SelectionEntryController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getTextField().setPlaceholder(model.getSelectionScreenTitle());
        view.getTextField().getSuggestions().setAll(model.getSuggestions());
        view.getSelectionScreenButton().getTooltip().add(ModTexts.choose(model.getSelectionScreenTitle()));
        view.getSelectionScreenButton().onAction(this::openSelectionScreen);
        if (model.getLabel() == null) {
            view.getRoot().getChildren().remove(view.getLabel());
        }
    }

    protected void openSelectionScreen() {
        ModScreenHandler.openListSelectionScreen(model.getSelectionScreenTitle(),
                model.getValue().contains(":") ? model.getValue() : "minecraft:" + model.getValue(),
                model.getSelectionItems(), model::setValue);
    }

    protected Identifier parseResourceLocation(String value) {
        return ClientUtil.parseResourceLocation(value);
    }
}
