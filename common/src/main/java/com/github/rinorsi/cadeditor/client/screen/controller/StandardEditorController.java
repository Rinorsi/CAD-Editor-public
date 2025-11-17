package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.guapi.api.Color;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.StandardEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ColorSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultEntityListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.StandardEditorView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StandardEditorController extends CategoryEntryScreenController<StandardEditorModel, StandardEditorView> implements EditorController<StandardEditorModel, StandardEditorView> {
    public StandardEditorController(StandardEditorModel model, StandardEditorView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        EditorController.super.bind();
        view.addOpenNBTEditorButton(() -> model.changeEditor(EditorType.NBT));
        view.addOpenSNBTEditorButton(() -> model.changeEditor(EditorType.SNBT));
        if (model.getContext().canSaveToVault()) {
            if (model instanceof ItemEditorModel itemModel) {
                view.addLoadVaultButton(() -> openItemVaultSelection(itemModel));
            } else if (model instanceof EntityEditorModel entityModel) {
                view.addLoadVaultButton(() -> openEntityVaultSelection(entityModel));
            }
        }
        if (model.getContext().getTag() == null) {
            view.getOpenNBTEditorButton().setDisable(true);
            view.getOpenSNBTEditorButton().setDisable(true);
        }
        view.getHeaderLabel().setLabel(model.getContext().getTargetName());
        view.getTextEditorButtons().visibleProperty().bind(model.activeTextEditorProperty().notNull());
        view.setTextEditorSupplier(model::getActiveTextEditor);
        model.activeTextEditorProperty().addListener(editor -> view.updateTextEditorToolbar(editor));
        view.updateTextEditorToolbar(model.getActiveTextEditor());
        view.getChooseCustomColorButton().onAction(e -> {
            e.consume();
            if (model.getActiveTextEditor() != null
                    && model.getActiveTextEditor().supportsColorFormatting()
                    && model.getActiveTextEditor().supportsCustomColorPicker()) {
                ModScreenHandler.openColorSelectionScreen(ColorSelectionScreenModel.Target.TEXT, Color.fromHex(model.getTextEditorCustomColor()), this::updateCustomColor);
            }
        });
        model.textEditorCustomColor().addListener(value -> {
            view.getCustomColorButton().setBackgroundColor(Color.fromHex(value));
            boolean show = value != null
                    && model.getActiveTextEditor() != null
                    && model.getActiveTextEditor().supportsColorFormatting()
                    && model.getActiveTextEditor().supportsCustomColorPicker();
            view.getCustomColorButton().setVisible(show);
        });
        view.getCustomColorButton().onAction(e -> {
            e.consume();
            model.getActiveTextEditor().addColorFormatting(model.getTextEditorCustomColor());
        });
    }

    private void updateCustomColor(String hex) {
        if (model.getActiveTextEditor() == null
                || !model.getActiveTextEditor().supportsColorFormatting()
                || !model.getActiveTextEditor().supportsCustomColorPicker()) {
            return;
        }
        model.setTextEditorCustomColor(hex);
        model.getActiveTextEditor().addColorFormatting(hex);
    }

    private void openItemVaultSelection(ItemEditorModel itemModel) {
        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ClientUtil.parseItemStack(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "editor_vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item_editor", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            itemModel.handleStackReplaced(chosen.copy());
        });
    }

    private void openEntityVaultSelection(EntityEditorModel entityModel) {
        List<VaultEntityListSelectionElementModel> elements = new ArrayList<>();
        Map<String, CompoundTag> entitiesById = new LinkedHashMap<>();
        List<CompoundTag> storedEntities = Vault.getInstance().getEntities();
        for (int i = 0; i < storedEntities.size(); i++) {
            CompoundTag tag = storedEntities.get(i);
            if (tag == null || tag.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "editor_vault_entity_" + i);
            elements.add(new VaultEntityListSelectionElementModel(id, tag));
            entitiesById.put(id.toString(), tag.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_entity_editor", elements, selectedId -> {
            CompoundTag chosen = entitiesById.get(selectedId);
            if (chosen == null) {
                return;
            }
            entityModel.handleEntityReplaced(chosen.copy());
        });
    }

    @Override
    public void updateDoneButton() {
        EditorController.super.updateDoneButton();
    }
}
