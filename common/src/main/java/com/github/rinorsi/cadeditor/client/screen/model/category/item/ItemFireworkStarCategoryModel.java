package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FireworkColorEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.List;

public class ItemFireworkStarCategoryModel extends ItemEditorCategoryModel {
    private final FireworkExplosionEditor explosion = new FireworkExplosionEditor();
    private boolean loadedFromStack;

    public ItemFireworkStarCategoryModel(ItemEditorModel editor) {
        super(ModTexts.FIREWORK_STAR, editor);
    }

    @Override
    protected void setupEntries() {
        if (!loadedFromStack) {
            loadFromStack();
            loadedFromStack = true;
        }
        EnumEntryModel<FireworkExplosion.Shape> shapeEntry = new EnumEntryModel<>(this, ModTexts.FIREWORK_SHAPE,
                FireworkExplosion.Shape.values(), explosion.getShape(), explosion::setShape);
        BooleanEntryModel trailEntry = new BooleanEntryModel(this, ModTexts.FIREWORK_TRAIL,
                explosion.hasTrail(), explosion::setHasTrail);
        BooleanEntryModel twinkleEntry = new BooleanEntryModel(this, ModTexts.FIREWORK_TWINKLE,
                explosion.hasTwinkle(), explosion::setHasTwinkle);
        getEntries().add(shapeEntry);
        getEntries().add(trailEntry);
        getEntries().add(twinkleEntry);

        addColorEntries(explosion.getColors(), true);
        getEntries().add(new ActionEntryModel(this, ModTexts.FIREWORK_ADD_PRIMARY_COLOR,
                () -> addColor(explosion.getColors())));

        addColorEntries(explosion.getFadeColors(), false);
        getEntries().add(new ActionEntryModel(this, ModTexts.FIREWORK_ADD_FADE_COLOR,
                () -> addColor(explosion.getFadeColors())));
    }

    private void loadFromStack() {
        ItemStack stack = getParent().getContext().getItemStack();
        FireworkExplosion current = stack.get(DataComponents.FIREWORK_EXPLOSION);
        if (current != null) {
            explosion.setShape(current.shape());
            copyList(current.colors(), explosion.getColors());
            copyList(current.fadeColors(), explosion.getFadeColors());
            explosion.setHasTrail(current.hasTrail());
            explosion.setHasTwinkle(current.hasTwinkle());
        } else {
            explosion.getColors().clear();
            explosion.getFadeColors().clear();
            explosion.setShape(FireworkExplosion.Shape.SMALL_BALL);
            explosion.setHasTrail(false);
            explosion.setHasTwinkle(false);
        }
    }

    private void addColorEntries(List<Integer> colors, boolean primary) {
        for (int i = 0; i < colors.size(); i++) {
            int index = i;
            FireworkColorEntryModel entry = new FireworkColorEntryModel(this,
                    primary ? ModTexts.fireworkPrimaryColor(index + 1) : ModTexts.fireworkFadeColor(index + 1),
                    colors.get(i), value -> colors.set(index, value),
                    () -> removeColor(colors, index));
            getEntries().add(entry);
        }
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

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        if (explosion.getColors().isEmpty()) {
            stack.remove(DataComponents.FIREWORK_EXPLOSION);
            return;
        }
        stack.set(DataComponents.FIREWORK_EXPLOSION, explosion.toComponent());
    }

    private void copyList(IntList source, List<Integer> destination) {
        destination.clear();
        if (source == null) {
            return;
        }
        for (int i = 0; i < source.size(); i++) {
            destination.add(source.getInt(i));
        }
    }
}
