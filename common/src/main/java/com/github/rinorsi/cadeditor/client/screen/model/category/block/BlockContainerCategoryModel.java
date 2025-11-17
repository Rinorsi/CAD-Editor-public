package com.github.rinorsi.cadeditor.client.screen.model.category.block;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.BlockEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.TextEntryModel;
import com.github.rinorsi.cadeditor.client.util.ComponentJsonHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

public class BlockContainerCategoryModel extends BlockEditorCategoryModel {
    public BlockContainerCategoryModel(BlockEditorModel editor) {
        super(ModTexts.CONTAINER, editor);
    }

    @Override
    protected void setupEntries() {
        String lock = getData().getString("Lock").orElse("");
        getEntries().addAll(
                new TextEntryModel(this, ModTexts.CUSTOM_NAME, getCustomName(), this::setCustomName),
                new StringEntryModel(this, ModTexts.LOCK_CODE, lock, this::setLockCode)
        );
    }

    private MutableComponent getCustomName() {
        String s = getData().getString("CustomName").orElse("");
        if (s.isEmpty()) {
            return null;
        }
        return ComponentJsonHelper.decode(s, ClientUtil.registryAccess());
    }

    private void setCustomName(MutableComponent value) {
        if (value != null && !value.getString().isEmpty()) {
            String json = ComponentJsonHelper.encode(value, ClientUtil.registryAccess());
            if (!json.isEmpty()) {
                getData().putString("CustomName", json);
            }
        } else if (getData().getString("CustomName").orElse("").isEmpty()) {
            getData().remove("CustomName");
        } else {
            getData().putString("CustomName", "");
        }
    }

    private void setLockCode(String value) {
        if (value != null && !value.isEmpty()) {
            getData().putString("Lock", value);
        } else {
            getData().remove("Lock");
        }
    }
}
