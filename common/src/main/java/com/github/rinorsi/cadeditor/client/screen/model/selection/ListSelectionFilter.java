package com.github.rinorsi.cadeditor.client.screen.model.selection;

import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.function.Predicate;

public final class ListSelectionFilter {
    private final String id;
    private final MutableComponent label;
    private final Predicate<ListSelectionElementModel> predicate;

    public ListSelectionFilter(String id, MutableComponent label, Predicate<ListSelectionElementModel> predicate) {
        this.id = Objects.requireNonNull(id, "id");
        this.label = Objects.requireNonNull(label, "label");
        this.predicate = predicate == null ? element -> true : predicate;
    }

    public String getId() {
        return id;
    }

    public MutableComponent label() {
        return label;
    }

    public boolean test(ListSelectionElementModel element) {
        return predicate.test(element);
    }
}
