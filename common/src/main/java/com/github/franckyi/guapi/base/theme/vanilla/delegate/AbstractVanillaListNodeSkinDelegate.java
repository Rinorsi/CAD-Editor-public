package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.Color;
import com.github.franckyi.guapi.api.RenderHelper;
import com.github.franckyi.guapi.api.event.MouseButtonEvent;
import com.github.franckyi.guapi.api.event.MouseDragEvent;
import com.github.franckyi.guapi.api.event.MouseEvent;
import com.github.franckyi.guapi.api.event.MouseScrollEvent;
import com.github.franckyi.guapi.api.node.ListNode;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.util.ScreenEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("this-escape")
public abstract class AbstractVanillaListNodeSkinDelegate<N extends ListNode<E>, E, T extends AbstractVanillaListNodeSkinDelegate.NodeEntry<N, E, T>> extends AbstractSelectionList<T> implements VanillaWidgetSkinDelegate {
    protected final N node;
    protected boolean shouldRefreshSize = true;
    protected boolean shouldRefreshList = true;
    protected boolean shouldScrollTo = false;
    protected boolean shouldChangeFocus = false;

    public AbstractVanillaListNodeSkinDelegate(N node) {
        super(Minecraft.getInstance(), 0, 0, 0, node.getItemHeight());
        this.node = node;
        Runnable rs = this::shouldRefreshSize;
        node.xProperty().addListener(rs);
        node.yProperty().addListener(rs);
        node.baseXProperty().addListener(rs);
        node.baseYProperty().addListener(rs);
        node.widthProperty().addListener(rs);
        node.heightProperty().addListener(rs);
        node.fullWidthProperty().addListener(rs);
        node.fullHeightProperty().addListener(rs);
        node.rootProperty().addListener(this::shouldRefreshList);
        node.scrollToProperty().addListener(this::shouldScrollTo);
        node.focusedElementProperty().addListener(this::shouldChangeFocus);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public N getNode() {
        return node;
    }

    @Override
    public int getRowWidth() {
        return node.getWidth() - node.getPadding().getHorizontal() - 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return node.getRight() - 6;
    }

    @Override
    public int getRowLeft() {
        return node.getLeft() + node.getPadding().getLeft();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getEntryAtPosition(mouseX, mouseY) == null && (mouseX < getScrollbarPosition() || mouseX > node.getRight())) {
            setFocused(null);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        if (listener == null) {
            node.setFocusedElement(null);
        } else {
            node.setFocusedElement(((NodeEntry<?, E, ?>) listener).item);
        }
    }

    @Override
    public boolean preRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        boolean res = false;
        if (shouldRefreshSize) {
            refreshSize();
            res = true;
        }
        if (shouldRefreshList) {
            refreshList();
            res = true;
        }
        if (shouldScrollTo) {
            scrollTo();
            res = true;
        }
        if (shouldChangeFocus) {
            changeFocus();
            res = true;
        }
        return res;
    }

    @Override
    public void postRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (T entry : children()) {
            entry.getNode().postRender(guiGraphics, mouseX, mouseY, delta);
        }
    }

    protected void shouldRefreshSize() {
        shouldRefreshSize = true;
    }

    protected void shouldRefreshList() {
        shouldRefreshList = true;
    }

    protected void shouldScrollTo() {
        shouldScrollTo = true;
    }

    protected void shouldChangeFocus() {
        shouldChangeFocus = true;
    }

    protected void refreshSize() {
        width = node.getWidth();
        height = node.getHeight();
        setX(node.getX());
        setY(node.getY());
        shouldRefreshSize = false;
    }

    @Override
    public int getRight() {
        return node.getRight();
    }

    @Override
    public int getBottom() {
        return node.getBottom();
    }

    protected void refreshList() {
        clearEntries();
        createList();
        setScrollAmount(getScrollAmount());
        shouldRefreshList = false;
    }

    protected abstract void createList();

    protected void scrollTo() {
        for (T e : children()) {
            if (e.getItem() == node.getScrollTo()) {
                centerScrollOn(e);
                break;
            }
        }
        shouldScrollTo = false;
    }

    protected void changeFocus() {
        for (T e : children()) {
            if (e.getItem() == node.getFocusedElement()) {
                super.setFocused(e);
                break;
            }
        }
        shouldChangeFocus = false;
    }

    @Override
    public void doTick() {
        for (T child : children()) {
            child.getNode().doTick();
        }
    }

    @Override
    public void mouseClicked(MouseButtonEvent event) {
        VanillaWidgetSkinDelegate.super.mouseClicked(event);
        handleMouseEvent(ScreenEventType.MOUSE_CLICKED, event);
    }

    @Override
    public void mouseReleased(MouseButtonEvent event) {
        VanillaWidgetSkinDelegate.super.mouseReleased(event);
        handleMouseEvent(ScreenEventType.MOUSE_RELEASED, event);
    }

    @Override
    public void mouseDragged(MouseDragEvent event) {
        VanillaWidgetSkinDelegate.super.mouseDragged(event);
        handleMouseEvent(ScreenEventType.MOUSE_DRAGGED, event);
    }

    @Override
    public void mouseScrolled(MouseScrollEvent event) {
        VanillaWidgetSkinDelegate.super.mouseScrolled(event);
        handleMouseEvent(ScreenEventType.MOUSE_SCOLLED, event);
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        VanillaWidgetSkinDelegate.super.mouseMoved(event);
        handleMouseEvent(ScreenEventType.MOUSE_MOVED, event);
    }

    protected <EE extends MouseEvent> void handleMouseEvent(ScreenEventType<EE> target, EE event) {
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX < node.getLeft() || mouseX > node.getRight() || mouseY < node.getTop() || mouseY > node.getBottom()) {
            return;
        }
        int index = 0;
        for (T child : children()) {
            Node childNode = child.getNode();
            if (childNode == null) {
                index++;
                continue;
            }
            double childTop = childNode.getTop();
            double childBottom = childNode.getBottom();
            if (childTop < node.getTop()) {
                childTop = node.getTop();
            }
            if (childBottom > node.getBottom()) {
                childBottom = node.getBottom();
            }
            if (mouseY < childTop || mouseY >= childBottom) {
                index++;
                continue;
            }
            child.getNode().handleEvent(target, event);
            if (event.getTarget() != null) return;
            index++;
        }
    }

    protected abstract static class NodeEntry<N extends ListNode<E>, E, T extends NodeEntry<N, E, T>> extends Entry<T> {
        private final AbstractVanillaListNodeSkinDelegate<N, E, T> list;
        private final E item;
        private Node node;

        public NodeEntry(AbstractVanillaListNodeSkinDelegate<N, E, T> list, E item) {
            this(list, item, null);
        }

        public NodeEntry(AbstractVanillaListNodeSkinDelegate<N, E, T> list, E item, Node node) {
            this.list = list;
            this.item = item;
            this.node = node;
        }

        public AbstractVanillaListNodeSkinDelegate<N, E, T> getList() {
            return list;
        }

        public E getItem() {
            return item;
        }

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        protected void renderBackground(GuiGraphics guiGraphics, int x, int y, int entryWidth, int entryHeight) {
            if (getList().getFocused() == this) {
                RenderHelper.fillRectangle(guiGraphics, x - 2, y - 2,
                        x + entryWidth + 3, y + entryHeight + 2, Color.fromRGBA(255, 255, 255, 79));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return list.node.isChildrenFocusable();
        }
    }
}
