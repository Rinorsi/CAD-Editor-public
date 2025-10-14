package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
public class BlockEntityDataCategoryModel extends BlockEditorCategoryModel {
    private TextEntryModel rawEditor;

    public BlockEntityDataCategoryModel(BlockEditorModel parent) {
        super(ModTexts.gui("block_entity_data"), parent);
    }

    @Override
    protected void setupEntries() {
        String initial = getParent().getContext().getTag() == null ? "{}" : getParent().getContext().getTag().toString();
        rawEditor = new TextEntryModel(this, ModTexts.gui("block_entity_data"), Component.literal(initial), v -> {});
        getEntries().add(rawEditor);
    }

    @Override
    public int getEntryListStart() { return -1; }

    @Override
    public void apply() {
        super.apply();
        String raw = rawEditor.getValue() == null ? "{}" : rawEditor.getValue().getString();
        try {
            getParent().getContext().setTag(TagParser.parseTag(raw));
            rawEditor.setValid(true);
        } catch (CommandSyntaxException e) {
            rawEditor.setValid(false);
        }
    }
}

