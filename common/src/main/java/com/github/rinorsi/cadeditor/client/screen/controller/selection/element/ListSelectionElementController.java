package com.github.rinorsi.cadeditor.client.screen.controller.selection.element;

import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SelectableListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.element.ListSelectionElementView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class ListSelectionElementController<M extends ListSelectionElementModel, V extends ListSelectionElementView> extends AbstractController<M, V> {
    private Component fullDisplayName;

    public ListSelectionElementController(M model, V view) {
        super(model, view);
    }

    @Override
    public void bind() {
        fullDisplayName = model.getDisplayName();
        view.getNameLabel().getTooltip().setAll(fullDisplayName.copy());
        view.getRoot().widthProperty().addListener(newValue -> updateDisplayName());
        updateDisplayName();
        view.getIdLabel().setLabel(text(model.getId().toString()).withStyle(ChatFormatting.ITALIC));
        if (model instanceof SelectableListSelectionElementModel selectable) {
            view.enableSelection();
            var checkBox = view.getSelectionCheckBox();
            checkBox.checkedProperty().bindBidirectional(selectable.selectedProperty());
            checkBox.onAction(() -> DebugLog.ui("list_selection_toggle",
                    () -> "entry=" + model.getId() + " state=" + selectable.isSelected()));
            updateDisplayName();
        }
    }

    private void updateDisplayName() {
        view.getNameLabel().setLabel(truncateDisplayName(fullDisplayName));
    }

    private Component truncateDisplayName(Component component) {
        if (component == null) {
            return Component.empty();
        }
        int maxWidth = computeAvailableWidth();
        if (maxWidth <= 0) {
            return Component.empty();
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) {
            return component.copy();
        }
        var font = minecraft.font;
        if (font.width(component) <= maxWidth) {
            return component.copy();
        }
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int availableWidth = Math.max(0, maxWidth - ellipsisWidth);
        String trimmed = font.plainSubstrByWidth(component.getString(), availableWidth);
        while (!trimmed.isEmpty() && font.width(trimmed) > availableWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return Component.literal(ellipsis).withStyle(component.getStyle());
        }
        return Component.literal(trimmed)
                .withStyle(component.getStyle())
                .append(Component.literal(ellipsis).withStyle(component.getStyle()));
    }

    private int computeAvailableWidth() {
        int width = view.getRoot().getWidth() - view.getRoot().getPadding().getHorizontal();
        if (view.getSelectionCheckBox() != null) {
            width -= view.getSelectionCheckBox().getWidth() + view.getRoot().getSpacing();
        }
        return Math.max(width, 0);
    }
}
