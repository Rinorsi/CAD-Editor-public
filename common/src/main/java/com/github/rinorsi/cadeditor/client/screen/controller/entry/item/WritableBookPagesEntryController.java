package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.WritableBookPagesEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.WritableBookPagesEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.WritableBookContent;

public class WritableBookPagesEntryController extends EntryController<WritableBookPagesEntryModel, WritableBookPagesEntryView> {
    private boolean updating;

    public WritableBookPagesEntryController(WritableBookPagesEntryModel model, WritableBookPagesEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        getView().setListButtonsVisible(false);
        getView().getResetButton().setVisible(false);

        var area = getView().getEditorArea();
        area.setValidator(this::isLengthValid);
        area.textProperty().addListener(value -> {
            if (!updating) {
                getModel().setSelectedPage(value);
                updateStatusLabel(value);
            }
        });

        getView().getPrevButton().onAction(() -> {
            getModel().selectPrevious();
            refresh();
        });
        getView().getNextButton().onAction(() -> {
            getModel().selectNext();
            refresh();
        });
        getView().getAddButton().onAction(() -> {
            getModel().insertAfterCurrent();
            refresh();
        });
        getView().getRemoveButton().onAction(() -> {
            getModel().removeCurrent();
            refresh();
        });

        getModel().pages().addListener(this::refresh);
        getModel().selectedIndexProperty().addListener(i -> refresh());

        refresh();
    }

    private void refresh() {
        updating = true;
        try {
            String text = getModel().getSelectedPage();
            var area = getView().getEditorArea();
            area.setText(text);
            int caret = text == null ? 0 : text.length();
            area.setCursorPosition(caret);
            area.setHighlightPosition(caret);
            updateStatusLabel(text);
            updateButtonStates();
        } finally {
            updating = false;
        }
    }

    private void updateStatusLabel(String value) {
        int length = value == null ? 0 : value.length();
        int remaining = WritableBookContent.PAGE_EDIT_LENGTH - length;
        int index = getModel().getSelectedIndex();
        int total = Math.max(1, getModel().getPageCount());
        Component status = ModTexts.gui("book_page_indicator").copy()
                .append(Component.literal(" " + (index + 1) + "/" + total).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                .append(ModTexts.gui("book_char_count").copy())
                .append(Component.literal(" " + length + "/" + WritableBookContent.PAGE_EDIT_LENGTH)
                        .withStyle(remaining >= 0 ? ChatFormatting.GRAY : ChatFormatting.RED));
        getView().getStatusLabel().setLabel(status);
    }

    private void updateButtonStates() {
        int index = getModel().getSelectedIndex();
        int total = getModel().getPageCount();
        getView().getPrevButton().setDisable(index <= 0);
        getView().getNextButton().setDisable(index >= total - 1);
        getView().getRemoveButton().setDisable(total <= 1);
        getView().getAddButton().setDisable(total >= WritableBookContent.MAX_PAGES);
    }

    private boolean isLengthValid(String value) {
        return value == null || value.length() <= WritableBookContent.PAGE_EDIT_LENGTH;
    }
}
