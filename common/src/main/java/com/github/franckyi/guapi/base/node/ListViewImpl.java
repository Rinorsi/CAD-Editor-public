package com.github.franckyi.guapi.base.node;

import com.github.franckyi.guapi.api.node.ListView;
import com.github.franckyi.guapi.api.node.builder.ListViewBuilder;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("this-escape")
public final class ListViewImpl<E> extends AbstractListView<E> implements ListViewBuilder<E> {
    public ListViewImpl() {
        super();
    }

    public ListViewImpl(int itemHeight) {
        super(itemHeight);
    }
    
    @SafeVarargs
    @SuppressWarnings("varargs")
    public ListViewImpl(int itemHeight, E... items) {
        this(itemHeight, copyOf(items));
    }

    public ListViewImpl(int itemHeight, Collection<? extends E> items) {
        super(itemHeight, items);
    }

    private static <E> Collection<E> copyOf(E[] items) {
        Collection<E> list = new ArrayList<>(items.length);
        for (E item : items) {
            list.add(item);
        }
        return list;
    }

    @Override
    protected Class<?> getType() {
        return ListView.class;
    }

    @Override
    public String toString() {
        return "ListView" + getItems();
    }
}
