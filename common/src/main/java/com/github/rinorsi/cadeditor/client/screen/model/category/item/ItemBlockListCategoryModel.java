package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BlockSelectionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.client.util.NbtUuidHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemBlockListCategoryModel extends ItemEditorCategoryModel {
    private final String tagName;
    private ListTag newBlocks;

    public ItemBlockListCategoryModel(Component name, ItemEditorModel editor, String tagName) {
        super(name, editor);
        this.tagName = tagName;
    }

    @Override
    protected void setupEntries() {
        // 1) Try legacy NBT list
        ListTag nbtList = NbtHelper.getListOrEmpty(getTag(), tagName);
        if (!nbtList.isEmpty()) {
            for (Tag element : nbtList) {
                if (element instanceof StringTag stringTag) {
                    getEntries().add(createBlockEntry(stringTag.value()));
                }
            }
            return;
        }
        // 2) Fallback to 1.21 components stored under "components"
        var data = getData();
        if (data != null) {
            var components = data.getCompound("components").orElse(null);
            String key = "CanDestroy".equals(tagName) ? "minecraft:can_break" : "minecraft:can_place_on";
            if (components != null) {
                var comp = components.getCompound(key).orElse(null);
                if (comp != null) {
                    var preds = comp.getList("predicates").orElse(null);
                    if (preds != null) {
                        for (Tag predicateTag : preds) {
                            if (predicateTag instanceof CompoundTag predicate && predicate.contains("blocks")) {
                                readComponentBlocks(predicate.get("blocks"));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createBlockEntry("");
    }

    private EntryModel createBlockEntry(String id) {
        return new BlockSelectionEntryModel(this, null, id, this::addBlock);
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.BLOCK;
    }

    @Override
    public void apply() {
        newBlocks = new ListTag();
        super.apply();
        // 1) Keep legacy NBT for UI compatibility
        if (!newBlocks.isEmpty()) {
            getOrCreateTag().put(tagName, newBlocks);
        } else if (getOrCreateTag().contains(tagName)) {
            getOrCreateTag().remove(tagName);
        }
        // 2) Apply 1.21 Data Components to the actual stack
        ItemStack stack = getParent().getContext().getItemStack();
        List<BlockPredicate> predicates = new ArrayList<>();
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.BLOCK);
        if (lookupOpt.isPresent()) {
            var lookup = lookupOpt.get();
            for (Tag t : newBlocks) {
                if (t instanceof StringTag s) {
                    ResourceLocation rl = ResourceLocation.tryParse(s.value());
                    if (rl != null) {
                        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, rl);
                        var holder = lookup.get(key);
                        holder.ifPresent(h -> predicates.add(new BlockPredicate(Optional.of(HolderSet.direct(h)), Optional.empty(), Optional.empty(), DataComponentMatchers.ANY)));
                    }
                }
            }
        }
        AdventureModePredicate predicate = predicates.isEmpty() ? null : new AdventureModePredicate(predicates);
        boolean isDestroy = "CanDestroy".equals(tagName);
        if (predicate != null) {
            if (isDestroy) stack.set(DataComponents.CAN_BREAK, predicate); else stack.set(DataComponents.CAN_PLACE_ON, predicate);
        } else {
            if (isDestroy) stack.remove(DataComponents.CAN_BREAK); else stack.remove(DataComponents.CAN_PLACE_ON);
        }
    }

    private void addBlock(String id) {
        if (id == null) {
            return;
        }
        String trimmed = id.trim();
        if (!trimmed.isEmpty()) {
            newBlocks.add(StringTag.valueOf(trimmed));
        }
    }

    private void readComponentBlocks(Tag blocksTag) {
        if (blocksTag == null) {
            return;
        }
        if (blocksTag instanceof StringTag stringTag) {
            addComponentBlock(stringTag.value());
            return;
        }
        if (blocksTag instanceof ListTag listTag) {
            for (Tag element : listTag) {
                readComponentBlocks(element);
            }
            return;
        }
        if (blocksTag instanceof CompoundTag compoundTag) {
            if (compoundTag.contains("id")) {
                addComponentBlock(NbtHelper.getString(compoundTag, "id", ""));
            }
            if (compoundTag.contains("tag")) {
                addComponentBlock("#" + NbtHelper.getString(compoundTag, "tag", ""));
            }
            if (compoundTag.contains("blocks")) {
                readComponentBlocks(compoundTag.get("blocks"));
            }
        }
    }

    private void addComponentBlock(String id) {
        if (id != null && !id.isEmpty()) {
            getEntries().add(createBlockEntry(id));
        }
    }
}

