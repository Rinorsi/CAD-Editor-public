package com.github.franckyi.guapi.api.util;

import com.github.franckyi.guapi.api.node.Node;

public enum Align {
    TOP_LEFT(Vertical.TOP, Horizontal.LEFT),
    TOP_CENTER(Vertical.TOP, Horizontal.CENTER),
    TOP_RIGHT(Vertical.TOP, Horizontal.RIGHT),
    CENTER_LEFT(Vertical.CENTER, Horizontal.LEFT),
    CENTER(Vertical.CENTER, Horizontal.CENTER),
    CENTER_RIGHT(Vertical.CENTER, Horizontal.RIGHT),
    BOTTOM_LEFT(Vertical.BOTTOM, Horizontal.LEFT),
    BOTTOM_CENTER(Vertical.BOTTOM, Horizontal.CENTER),
    BOTTOM_RIGHT(Vertical.BOTTOM, Horizontal.RIGHT);

    private final Vertical verticalAlign;
    private final Horizontal horizontalAlign;

    Align(Vertical verticalAlign, Horizontal horizontalAlign) {
        this.verticalAlign = verticalAlign;
        this.horizontalAlign = horizontalAlign;
    }

    public Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public static int getAlignedX(Horizontal align, Node parent, int childWidth) {
        return switch (align) {
            case CENTER -> parent.getX() + parent.getPadding().getLeft() + ((parent.getWidth() - parent.getPadding().getHorizontal()) - childWidth) / 2;
            case RIGHT -> parent.getX() + parent.getPadding().getLeft() + (parent.getWidth() - parent.getPadding().getHorizontal()) - childWidth;
            case LEFT -> parent.getX() + parent.getPadding().getLeft();
        };
    }

    public static int getAlignedY(Vertical align, Node parent, int childHeight) {
        return switch (align) {
            case CENTER -> parent.getY() + parent.getPadding().getTop() + ((parent.getHeight() - parent.getPadding().getVertical()) - childHeight) / 2;
            case BOTTOM -> parent.getY() + parent.getPadding().getTop() + (parent.getHeight() - parent.getPadding().getVertical()) - childHeight;
            case TOP -> parent.getY() + parent.getPadding().getTop();
        };
    }

    public enum Horizontal {
        LEFT, CENTER, RIGHT
    }

    public enum Vertical {
        TOP, CENTER, BOTTOM
    }
}
