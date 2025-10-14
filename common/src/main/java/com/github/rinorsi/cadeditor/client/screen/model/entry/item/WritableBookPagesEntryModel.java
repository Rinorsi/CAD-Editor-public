package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.franckyi.databindings.api.ObservableList;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemEditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class WritableBookPagesEntryModel extends EntryModel {
    private final ObservableList<String> pages = ObservableList.create();
    private final List<String> defaultPages = new ArrayList<>();
    private final IntegerProperty selectedIndexProperty = IntegerProperty.create(0);
    private final Consumer<List<String>> onApply;

    public WritableBookPagesEntryModel(ItemEditorCategoryModel category, List<String> initialPages, Consumer<List<String>> onApply) {
        super(category);
        this.onApply = Objects.requireNonNull(onApply, "onApply");
        if (initialPages == null || initialPages.isEmpty()) {
            pages.add("");
        } else {
            initialPages.stream().limit(WritableBookContent.MAX_PAGES).forEach(pages::add);
        }
        defaultPages.addAll(pages);
        ensureValidSelection();
    }

    public ObservableList<String> pages() {
        return pages;
    }

    public int getSelectedIndex() {
        return selectedIndexProperty().getValue();
    }

    public void setSelectedIndex(int index) {
        int clamped = Math.max(0, Math.min(index, Math.max(pages.size() - 1, 0)));
        selectedIndexProperty().setValue(clamped);
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndexProperty;
    }

    public String getSelectedPage() {
        int index = getSelectedIndex();
        return index >= 0 && index < pages.size() ? pages.get(index) : "";
    }

    public void setSelectedPage(String text) {
        int index = getSelectedIndex();
        if (index < 0 || index >= pages.size()) {
            return;
        }
        String trimmed = sanitizePage(text);
        pages.set(index, trimmed);
    }

    public void selectPrevious() {
        setSelectedIndex(getSelectedIndex() - 1);
    }

    public void selectNext() {
        setSelectedIndex(getSelectedIndex() + 1);
    }

    public void insertAfterCurrent() {
        if (pages.size() >= WritableBookContent.MAX_PAGES) {
            return;
        }
        int insertAt = Math.min(getSelectedIndex() + 1, pages.size());
        pages.add(insertAt, "");
        setSelectedIndex(insertAt);
    }

    public void removeCurrent() {
        if (pages.size() <= 1) {
            pages.set(0, "");
            setSelectedIndex(0);
            return;
        }
        int index = getSelectedIndex();
        if (index < 0 || index >= pages.size()) {
            return;
        }
        pages.remove(index);
        setSelectedIndex(Math.min(index, pages.size() - 1));
    }

    public int getPageCount() {
        return pages.size();
    }

    @Override
    public void apply() {
        onApply.accept(List.copyOf(pages));
    }

    @Override
    public void reset() {
        pages.clear();
        pages.addAll(defaultPages);
        ensureValidSelection();
    }

    @Override
    public Type getType() {
        return Type.WRITABLE_BOOK_PAGES;
    }

    private void ensureValidSelection() {
        if (pages.isEmpty()) {
            pages.add("");
        }
        int clamped = Math.max(0, Math.min(selectedIndexProperty.getValue(), pages.size() - 1));
        selectedIndexProperty.setValue(clamped);
    }

    private static String sanitizePage(String text) {
        String value = text == null ? "" : text;
        if (value.length() > WritableBookContent.PAGE_EDIT_LENGTH) {
            return value.substring(0, WritableBookContent.PAGE_EDIT_LENGTH);
        }
        return value;
    }
}
