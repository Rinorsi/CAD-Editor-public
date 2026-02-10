package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.debug.DebugLog;
import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.EditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.rinorsi.cadeditor.client.util.SnbtHelper;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
public class BlockEntityDataCategoryModel extends BlockEditorCategoryModel {
    private TextEntryModel rawEditor;
    private String initialRaw = "{}";

    public BlockEntityDataCategoryModel(BlockEditorModel parent) {
        super(ModTexts.gui("block_entity_data"), parent);
    }

    @Override
    protected void setupEntries() {
        String initial = getParent().getContext().getTag() == null ? "{}" : getParent().getContext().getTag().toString();
        initialRaw = initial;
        rawEditor = new TextEntryModel(this, ModTexts.gui("block_entity_data"), Component.literal(initial), v -> {});
        getEntries().add(rawEditor);
    }

    @Override
    public int getEntryListStart() { return -1; }

    @Override
    public void apply() {
        super.apply();
        String raw = rawEditor.getValue() == null ? "{}" : rawEditor.getValue().getString();
        if (raw.equals(initialRaw)) {
            DebugLog.info("[BlockEntityData] raw unchanged, skip overwrite.");
            rawEditor.setValid(true);
            return;
        }
        try {
            getParent().getContext().setTag(SnbtHelper.parse(raw));
            DebugLog.info("[BlockEntityData] raw changed, applied parsed tag.");
            initialRaw = raw;
            rawEditor.setValid(true);
        } catch (CommandSyntaxException e) {
            DebugLog.info("[BlockEntityData] raw invalid SNBT, apply aborted.");
            rawEditor.setValid(false);
        }
    }
}

