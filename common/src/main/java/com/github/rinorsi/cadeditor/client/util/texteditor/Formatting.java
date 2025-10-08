package com.github.rinorsi.cadeditor.client.util.texteditor;

import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public abstract class Formatting {
    private int start, end;

    public Formatting(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public abstract void apply(MutableComponent text);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Formatting that = (Formatting) o;
        return start == that.start && end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
