package com.github.franckyi.guapi.base.node;

import com.github.franckyi.databindings.api.ObservableList;
import com.github.franckyi.guapi.api.node.ListView;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("this-escape")
public abstract class AbstractListView<E> extends AbstractListNode<E> implements ListView<E> {
    private final ObservableList<E> items = ObservableList.create();

    protected AbstractListView() {
        this(0);
    }

    protected AbstractListView(int itemHeight) {
        this(itemHeight, Collections.emptyList());
    }

    protected AbstractListView(int itemHeight, Collection<? extends E> items) {
        super(itemHeight);
        getItems().addAll(items);
    }

    @Override
    public ObservableList<E> getItems() {
        return items;
    }
}
