package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.TreeView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

@SuppressWarnings("this-escape")
public class VanillaTreeViewSkinDelegate<E extends TreeView.TreeItem<E>> extends AbstractVanillaListNodeSkinDelegate<TreeView<E>, E, VanillaTreeViewSkinDelegate.NodeEntry<E>> {
    public VanillaTreeViewSkinDelegate(TreeView<E> node) {
        super(node);
        node.rootItemProperty().addListener(this::shouldRefreshList);
        node.showRootProperty().addListener(this::shouldRefreshList);
        node.rootItemProperty().mapToObservableBoolean(TreeView.TreeItem::childrenChangedProperty, false).addListener(newVal -> {
            if (newVal) {
                shouldRefreshList();
                node.getRoot().setChildrenChanged(false);
            }
        });
    }

    @Override
    protected void createList() {
        if (node.rootItemProperty().hasValue()) {
            if (node.isShowRoot()) {
                addChild(node.getRoot(), 0);
            } else {
                for (E child : node.getRoot().getChildren()) {
                    addChild(child, 0);
                }
            }
        }
    }

    private void addChild(E item, int increment) {
        NodeEntry<E> entry = new NodeEntry<>(this, item, node.getRenderer().getView(item), increment);
        children().add(entry);
        if (item == node.getFocusedElement()) {
            setFocused(entry);
        }
        increment++;
        if (item.isExpanded()) {
            for (E child : item.getChildren()) {
                addChild(child, increment);
            }
        }
    }

    protected static class NodeEntry<E extends TreeView.TreeItem<E>> extends AbstractVanillaListNodeSkinDelegate.NodeEntry<TreeView<E>, E, NodeEntry<E>> {
        private static final ResourceLocation TREE_VIEW_WIDGETS = ResourceLocation.fromNamespaceAndPath(com.github.rinorsi.cadeditor.common.ModConstants.MOD_ID, "textures/gui/tree_view_widgets.png");
        private TexturedButton button;
        private final int increment;

        public NodeEntry(VanillaTreeViewSkinDelegate<E> list, E item, Node node, int increment) {
            super(list, item);
            setNode(hBox(root -> {
                if (item.getChildren().isEmpty()) {
                    root.add(hBox().prefSize(16, 16));
                } else {
                    root.add(button = texturedButton(TREE_VIEW_WIDGETS, 32, 32, false).prefSize(16, 16).action(() -> {
                        if (!item.isExpanded()) {
                            chainExpand(item);
                        } else {
                            item.setExpanded(false);
                        }
                        list.shouldRefreshList();
                    }));
                    button.imageXProperty().bind(item.expandedProperty().mapToInt(value -> value ? 16 : 0));
                }
                root.add(node).align(CENTER_LEFT).spacing(5).setParent(list.node);
            }));
            this.increment = increment;
        }

        private void chainExpand(E item) {
            item.setExpanded(true);
            if (item.getChildren().size() == 1) {
                chainExpand(item.getChildren().get(0));
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int incr = increment * getList().node.getChildrenIncrement();
            entryWidth = getList().getMaxScroll() == 0 ? entryWidth + 6 : entryWidth;
            getNode().setX(x + incr);
            getNode().setY(y);
            getNode().setParentPrefWidth(entryWidth - incr);
            getNode().setParentPrefHeight(entryHeight);
            if (button != null) {
                button.setImageY(button.inBounds(mouseX, mouseY) ? 16 : 0);
            }
            renderBackground(guiGraphics, x, y, entryWidth, entryHeight);
            while (getNode().preRender(guiGraphics, mouseX, mouseY, tickDelta)) ;
            getNode().render(guiGraphics, mouseX, mouseY, tickDelta);
        }
    }
}

