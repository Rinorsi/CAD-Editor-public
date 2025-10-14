package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.ArrayList;
import java.util.List;

public class FireworkExplosionEditor {
    private FireworkExplosion.Shape shape;
    private final List<Integer> colors = new ArrayList<>();
    private final List<Integer> fadeColors = new ArrayList<>();
    private boolean hasTrail;
    private boolean hasTwinkle;

    public FireworkExplosionEditor() {
        this(FireworkExplosion.DEFAULT);
    }

    public FireworkExplosionEditor(FireworkExplosion explosion) {
        FireworkExplosion source = explosion == null ? FireworkExplosion.DEFAULT : explosion;
        this.shape = source.shape();
        copyList(source.colors(), colors);
        copyList(source.fadeColors(), fadeColors);
        this.hasTrail = source.hasTrail();
        this.hasTwinkle = source.hasTwinkle();
    }

    private void copyList(IntList src, List<Integer> dst) {
        dst.clear();
        if (src == null) {
            return;
        }
        for (int i = 0; i < src.size(); i++) {
            dst.add(src.getInt(i));
        }
    }

    public FireworkExplosion.Shape getShape() {
        return shape;
    }

    public void setShape(FireworkExplosion.Shape shape) {
        this.shape = shape;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public List<Integer> getFadeColors() {
        return fadeColors;
    }

    public boolean hasTrail() {
        return hasTrail;
    }

    public void setHasTrail(boolean hasTrail) {
        this.hasTrail = hasTrail;
    }

    public boolean hasTwinkle() {
        return hasTwinkle;
    }

    public void setHasTwinkle(boolean hasTwinkle) {
        this.hasTwinkle = hasTwinkle;
    }

    public FireworkExplosion toComponent() {
        IntArrayList primary = new IntArrayList(colors);
        IntArrayList fades = new IntArrayList(fadeColors);
        return new FireworkExplosion(shape, primary, fades, hasTrail, hasTwinkle);
    }
}
