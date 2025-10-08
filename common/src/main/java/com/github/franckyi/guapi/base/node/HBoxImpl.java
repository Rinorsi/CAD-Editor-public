package com.github.franckyi.guapi.base.node;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.builder.HBoxBuilder;

import java.util.Collection;

public class HBoxImpl extends AbstractHBox implements HBoxBuilder {
    public HBoxImpl() {
    }

    public HBoxImpl(int spacing) {
        super(spacing);
    }

    public HBoxImpl(Node... children) {
        super(children);
    }

    public HBoxImpl(Collection<? extends Node> children) {
        super(children);
    }

    public HBoxImpl(int spacing, Node... children) {
        super(spacing, children);
    }

    public HBoxImpl(int spacing, Collection<? extends Node> children) {
        super(spacing, children);
    }

    @Override
    protected Class<?> getType() {
        return HBox.class;
    }

    @Override
    public String toString() {
        return "HBox" + getChildren();
    }
}
