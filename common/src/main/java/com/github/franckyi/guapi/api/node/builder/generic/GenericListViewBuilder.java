package com.github.franckyi.guapi.api.node.builder.generic;

import com.github.franckyi.guapi.api.node.ListView;

import java.util.Collection;

public interface GenericListViewBuilder<E, N extends ListView<E>> extends ListView<E>, GenericListNodeBuilder<E, N> {
    default N items(Collection<? extends E> items) {
        return with(n -> n.getItems().addAll(items));
    }
}
