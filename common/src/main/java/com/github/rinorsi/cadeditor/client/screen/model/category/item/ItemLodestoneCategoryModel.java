
package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.Optional;

public class ItemLodestoneCategoryModel extends ItemEditorCategoryModel {
    private boolean hasTarget;
    private String dimensionId = "minecraft:overworld";
    private int posX;
    private int posY;
    private int posZ;
    private boolean tracked;

    private StringEntryModel dimensionEntry;

    public ItemLodestoneCategoryModel(ItemEditorModel editor) {
        super(ModTexts.LODESTONE, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker != null) {
            tracked = tracker.tracked();
            Optional<GlobalPos> target = tracker.target();
            if (target.isPresent()) {
                GlobalPos pos = target.get();
                hasTarget = true;
                dimensionId = pos.dimension().location().toString();
                BlockPos bp = pos.pos();
                posX = bp.getX();
                posY = bp.getY();
                posZ = bp.getZ();
            } else {
                hasTarget = false;
            }
        } else {
            tracked = false;
            hasTarget = false;
            posY = 64;
        }

        getEntries().add(new BooleanEntryModel(this, ModTexts.LODESTONE_TARGET_TOGGLE, hasTarget,
                value -> hasTarget = value != null && value));
        dimensionEntry = new StringEntryModel(this, ModTexts.DIMENSION, dimensionId,
                value -> dimensionId = value == null ? "" : value.trim());
        getEntries().add(dimensionEntry);
        getEntries().add(new IntegerEntryModel(this, ModTexts.POSITION_X, posX, value -> posX = value == null ? 0 : value));
        getEntries().add(new IntegerEntryModel(this, ModTexts.POSITION_Y, posY, value -> posY = value == null ? 0 : value));
        getEntries().add(new IntegerEntryModel(this, ModTexts.POSITION_Z, posZ, value -> posZ = value == null ? 0 : value));
        getEntries().add(new BooleanEntryModel(this, ModTexts.LODESTONE_TRACKED, tracked,
                value -> tracked = value != null && value));
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        boolean invalid = false;
        if (hasTarget) {
            ResourceLocation id = ResourceLocation.tryParse(dimensionId);
            if (id == null) {
                dimensionEntry.setValid(false);
                invalid = true;
            } else {
                dimensionEntry.setValid(true);
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);
                GlobalPos pos = GlobalPos.of(dimension, new BlockPos(posX, posY, posZ));
                stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(pos), tracked));
            }
        } else {
            dimensionEntry.setValid(true);
            if (tracked) {
                stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.empty(), tracked));
            } else {
                stack.remove(DataComponents.LODESTONE_TRACKER);
            }
        }
        if (invalid) {
            return;
        }
        CompoundTag data = getData();
        if (data != null) {
            CompoundTag components = data.getCompound("components").orElse(null);
            if (components != null) {
                components.remove("minecraft:lodestone_tracker");
                if (components.isEmpty()) {
                    data.remove("components");
                }
            }
        }
    }
}
