package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.AddListEntryEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FireworkColorEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class ItemFireworksCategoryModel extends ItemEditorCategoryModel {
    private final List<FireworkExplosionEditor> explosions = new ArrayList<>();
    private int flightDuration;
    private boolean loadedFromStack;

    public ItemFireworksCategoryModel(ItemEditorModel editor) {
        super(ModTexts.FIREWORK_ROCKET, editor);
    }

    @Override
    protected void setupEntries() {
        if (!loadedFromStack) {
            loadFromStack();
            loadedFromStack = true;
        }
        IntegerEntryModel flightEntry = new IntegerEntryModel(this, ModTexts.FIREWORK_FLIGHT_DURATION,
                flightDuration, value -> flightDuration = Math.max(0, Math.min(3, value)), value -> value >= 0 && value <= 3);
        getEntries().add(flightEntry);

        for (int i = 0; i < explosions.size(); i++) {
            addExplosionEntries(explosions.get(i), i);
        }
        getEntries().add(new AddListEntryEntryModel(this, ModTexts.FIREWORK_ADD_EXPLOSION, this::addExplosion));
    }

    private void loadFromStack() {
        ItemStack stack = getParent().getContext().getItemStack();
        Fireworks data = stack.get(DataComponents.FIREWORKS);
        explosions.clear();
        if (data != null) {
            flightDuration = data.flightDuration();
            data.explosions().forEach(explosion -> explosions.add(new FireworkExplosionEditor(explosion)));
        } else {
            flightDuration = 0;
        }
    }

    private void addExplosionEntries(FireworkExplosionEditor editor, int explosionIndex) {
        getEntries().add(new ActionEntryModel(this, ModTexts.fireworkRemoveExplosion(explosionIndex + 1),
                () -> removeExplosion(explosionIndex)));

        EnumEntryModel<FireworkExplosion.Shape> shapeEntry = new EnumEntryModel<>(this,
                labelForExplosion(ModTexts.FIREWORK_SHAPE, explosionIndex), FireworkExplosion.Shape.values(),
                editor.getShape(), editor::setShape);
        BooleanEntryModel trailEntry = new BooleanEntryModel(this,
                labelForExplosion(ModTexts.FIREWORK_TRAIL, explosionIndex), editor.hasTrail(), editor::setHasTrail);
        BooleanEntryModel twinkleEntry = new BooleanEntryModel(this,
                labelForExplosion(ModTexts.FIREWORK_TWINKLE, explosionIndex), editor.hasTwinkle(), editor::setHasTwinkle);

        getEntries().add(shapeEntry);
        getEntries().add(trailEntry);
        getEntries().add(twinkleEntry);

        addColorSection(editor.getColors(), true, explosionIndex);
        addColorSection(editor.getFadeColors(), false, explosionIndex);
    }

    private void addColorSection(List<Integer> colors, boolean primary, int explosionIndex) {
        for (int i = 0; i < colors.size(); i++) {
            int index = i;
            FireworkColorEntryModel entry = new FireworkColorEntryModel(this,
                    explosionColorLabel(primary, explosionIndex, index), colors.get(i), value -> colors.set(index, value),
                    () -> removeColor(colors, index));
            getEntries().add(entry);
        }
        MutableComponent addLabel = primary
                ? labelForExplosion(ModTexts.FIREWORK_ADD_PRIMARY_COLOR, explosionIndex)
                : labelForExplosion(ModTexts.FIREWORK_ADD_FADE_COLOR, explosionIndex);
        getEntries().add(new ActionEntryModel(this, addLabel, () -> addColor(colors)));
    }

    private MutableComponent explosionColorLabel(boolean primary, int explosionIndex, int colorIndex) {
        MutableComponent base = primary
                ? ModTexts.fireworkPrimaryColor(colorIndex + 1)
                : ModTexts.fireworkFadeColor(colorIndex + 1);
        return labelForExplosion(base, explosionIndex);
    }

    private MutableComponent labelForExplosion(MutableComponent base, int explosionIndex) {
        return base.copy().append(" ").append(ModTexts.fireworkExplosion(explosionIndex + 1));
    }

    private void addColor(List<Integer> colors) {
        colors.add(Color.NONE);
        initalize();
    }

    private void removeColor(List<Integer> colors, int index) {
        if (index >= 0 && index < colors.size()) {
            colors.remove(index);
            initalize();
        }
    }

    private void addExplosion() {
        explosions.add(new FireworkExplosionEditor());
        initalize();
    }

    private void removeExplosion(int index) {
        if (index >= 0 && index < explosions.size()) {
            explosions.remove(index);
            initalize();
        }
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        List<FireworkExplosion> built = explosions.stream()
                .map(FireworkExplosionEditor::toComponent)
                .filter(explosion -> explosion.colors() != null && explosion.colors().size() > 0)
                .toList();
        int clampedFlight = Math.max(0, Math.min(3, flightDuration));
        if (built.isEmpty() && clampedFlight == 0) {
            stack.remove(DataComponents.FIREWORKS);
            return;
        }
        stack.set(DataComponents.FIREWORKS, new Fireworks(clampedFlight, built));
    }
}
