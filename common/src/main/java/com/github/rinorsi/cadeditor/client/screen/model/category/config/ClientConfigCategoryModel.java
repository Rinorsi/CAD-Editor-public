package com.github.rinorsi.cadeditor.client.screen.model.category.config;

import com.github.franckyi.guapi.api.util.DebugMode;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.ClientInit;
import com.github.rinorsi.cadeditor.client.screen.model.ConfigEditorScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;

public class ClientConfigCategoryModel extends ConfigCategoryModel {
    private StringEntryModel guapiThemeEntry;
    private EnumEntryModel<DebugMode> guapiDebugModeEntry;
    private IntegerEntryModel selectionScreenMaxItemsEntry;

    public ClientConfigCategoryModel(ConfigEditorScreenModel editor) {
        super(ModTexts.CLIENT, editor);
    }

    @Override
    protected void setupEntries() {
        getEntries().addAll(
                guapiThemeEntry = new StringEntryModel(this, ModTexts.THEME, ClientConfiguration.INSTANCE.getGuapiTheme(), ClientConfiguration.INSTANCE::setGuapiTheme).withWeight(2),
                guapiDebugModeEntry = new EnumEntryModel<>(this, ModTexts.DEBUG_MODE, DebugMode.values(), ClientConfiguration.INSTANCE.getGuapiDebugMode(), ClientConfiguration.INSTANCE::setGuapiDebugMode)
                        .withTextFactory(DebugMode::toComponent)
                        .withWeight(2),
                selectionScreenMaxItemsEntry = new IntegerEntryModel(this, ModTexts.SELECTION_SCREEN_MAX_ITEMS, ClientConfiguration.INSTANCE.getSelectionScreenMaxItems(), ClientConfiguration.INSTANCE::setSelectionScreenMaxItems).withWeight(2),
                new ActionEntryModel(this, ModTexts.RELOAD_CONFIG, this::reload)
        );
    }

    private void reload() {
        ClientConfiguration.load();
        syncEntries();
    }

    public void syncEntries() {
        guapiThemeEntry.setValue(ClientConfiguration.INSTANCE.getGuapiTheme());
        guapiDebugModeEntry.setValue(ClientConfiguration.INSTANCE.getGuapiDebugMode());
        selectionScreenMaxItemsEntry.setValue(ClientConfiguration.INSTANCE.getSelectionScreenMaxItems());
    }

    @Override
    public void apply() {
        super.apply();
        ClientConfiguration.save();
        ClientInit.syncGuapiConfig();
    }
}
