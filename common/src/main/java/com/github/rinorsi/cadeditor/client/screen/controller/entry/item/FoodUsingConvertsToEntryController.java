package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.SelectionEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.FoodUsingConvertsToEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.FoodUsingConvertsToEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FoodUsingConvertsToEntryController extends SelectionEntryController<FoodUsingConvertsToEntryModel, FoodUsingConvertsToEntryView> {
    public FoodUsingConvertsToEntryController(FoodUsingConvertsToEntryModel model, FoodUsingConvertsToEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getLoadVaultButton().onAction(this::openVaultSelection);
        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenSnbtEditorButton().onAction(() -> openEditor(EditorType.SNBT));
        view.getItemPreview().itemProperty().bind(model.previewStackProperty());
        model.previewStackProperty().addListener(stack -> {
            boolean visible = stack != null && !stack.isEmpty();
            view.setPreviewVisible(visible);
            view.getOpenEditorButton().setDisable(!visible);
            view.getOpenSnbtEditorButton().setDisable(!visible);
        });
        view.setPreviewVisible(!model.getPreviewStack().isEmpty());
        view.getOpenEditorButton().setDisable(model.getPreviewStack().isEmpty());
        view.getOpenSnbtEditorButton().setDisable(model.getPreviewStack().isEmpty());
    }

    @Override
    protected void openSelectionScreen() {
        ModScreenHandler.openListSelectionScreen(model.getSelectionScreenTitle(),
                model.getValue().contains(":") ? model.getValue() : "minecraft:" + model.getValue(),
                model.getSelectionItems(), selection -> {
                    if (selection == null || selection.isEmpty()) {
                        return;
                    }
                    try {
                        ResourceLocation id = ResourceLocation.parse(selection);
                        Item item = BuiltInRegistries.ITEM.get(id);
                        if (item != null) {
                            model.useStack(new ItemStack(item));
                        }
                    } catch (Exception ignored) {
                    }
                });
    }

    private void openVaultSelection() {
        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "food_convert_vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item_food_convert", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            model.useStack(chosen.copy());
        });
    }

    private void openEditor(EditorType type) {
        ensureStackReady(() -> openEditorNow(type));
    }

    private void ensureStackReady(Runnable action) {
        if (!model.getPreviewStack().isEmpty()) {
            action.run();
            return;
        }
        openSelectionScreenWithCallback(action);
    }

    private void openSelectionScreenWithCallback(Runnable afterSelection) {
        ModScreenHandler.openListSelectionScreen(model.getSelectionScreenTitle(),
                model.getValue().contains(":") ? model.getValue() : "minecraft:" + model.getValue(),
                model.getSelectionItems(), selection -> {
                    if (selection == null || selection.isEmpty()) {
                        return;
                    }
                    try {
                        ResourceLocation id = ResourceLocation.parse(selection);
                        Item item = BuiltInRegistries.ITEM.get(id);
                        if (item != null) {
                            model.useStack(new ItemStack(item));
                            if (afterSelection != null) {
                                afterSelection.run();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                });
    }

    private void openEditorNow(EditorType type) {
        Optional<ItemStack> editable = model.getEditableStack();
        ItemStack stack = editable.map(ItemStack::copy).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return;
        }
        ItemEditorContext context = new ItemEditorContext(stack, null, false, ctx -> {
            ItemStack result = ctx.getItemStack().copy();
            model.useStack(result);
        });
        ModScreenHandler.openEditor(type, context);
    }
}
