package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.WritableBookPagesEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ItemWritableBookPagesCategoryModel extends ItemEditorCategoryModel {
    private WritableBookPagesEntryModel pagesEntry;
    private List<String> stagedPages = List.of();

    public ItemWritableBookPagesCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("writable_book_content"), editor);
    }

    @Override
    protected void setupEntries() {
        List<String> pages = readPages();
        pagesEntry = new WritableBookPagesEntryModel(this, pages, this::collectPages);
        getEntries().add(pagesEntry);
    }

    @Override
    public int getEntryListStart() {
        return -1;
    }

    @Override
    public int getEntryHeight() {
        return 190;
    }

    @Override
    public EntryModel createNewListEntry() {
        return null;
    }

    @Override
    public void apply() {
        stagedPages = List.of();
        super.apply();

        ItemStack stack = getParent().getContext().getItemStack();
        List<String> pages = stagedPages;
        boolean hasContent = pages.stream().anyMatch(text -> !text.isBlank());
        if (!hasContent) {
            stack.remove(DataComponents.WRITABLE_BOOK_CONTENT);
            pages = List.of();
        } else {
            List<Filterable<String>> filterables = pages.stream()
                    .limit(WritableBookContent.MAX_PAGES)
                    .map(text -> Filterable.passThrough(sanitizePage(text)))
                    .toList();
            stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(filterables));
        }
    }

    private List<String> readPages() {
        ItemStack stack = getParent().getContext().getItemStack();
        List<String> result = new ArrayList<>();
        WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (content != null) {
            content.getPages(false)
                    .limit(WritableBookContent.MAX_PAGES)
                    .map(this::sanitizePage)
                    .forEach(result::add);
        }
        if (!result.isEmpty()) {
            return result;
        }
        return result;
    }

    private void collectPages(List<String> pages) {
        List<String> sanitized = new ArrayList<>(pages.size());
        pages.stream()
                .limit(WritableBookContent.MAX_PAGES)
                .map(this::sanitizePage)
                .forEach(sanitized::add);
        stagedPages = List.copyOf(sanitized);
    }

    private String sanitizePage(String text) {
        String value = text == null ? "" : text.replace("\r", "");
        if (value.length() > WritableBookContent.PAGE_EDIT_LENGTH) {
            return value.substring(0, WritableBookContent.PAGE_EDIT_LENGTH);
        }
        return value;
    }
}
