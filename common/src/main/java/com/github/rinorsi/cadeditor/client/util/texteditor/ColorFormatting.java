package com.github.rinorsi.cadeditor.client.util.texteditor;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.Objects;

public class ColorFormatting extends Formatting {
    private String color;

    public ColorFormatting(int start, int end, String color) {
        super(start, end);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public void apply(MutableComponent text) {
        TextColor.parseColor(color).result().ifPresent(color -> text.withStyle(style -> style.withColor(color)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ColorFormatting that = (ColorFormatting) o;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }
}
