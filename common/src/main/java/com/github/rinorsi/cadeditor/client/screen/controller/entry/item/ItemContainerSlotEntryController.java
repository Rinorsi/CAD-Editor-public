package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ItemContainerSlotEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.ItemContainerSlotEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemContainerSlotEntryController extends EntryController<ItemContainerSlotEntryModel, ItemContainerSlotEntryView> {
    public ItemContainerSlotEntryController(ItemContainerSlotEntryModel model, ItemContainerSlotEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.setListButtonsVisible(false);
        view.disableDeleteButton();
        view.getUpButton().setVisible(false);
        view.getDownButton().setVisible(false);

        view.getSlotLabel().setLabel(model.getSlotLabel());
        model.listIndexProperty().addListener(value -> view.getSlotLabel().setLabel(model.getSlotLabel()));
        view.getItemView().itemProperty().bind(model.itemStackProperty());
        model.itemStackProperty().addListener(stack -> updateItemName());
        updateItemName();

        view.getChooseItemButton().onAction(() -> openItemSelection(null));
        view.getLoadVaultButton().onAction(this::openVaultSelection);
        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenSnbtEditorButton().onAction(() -> openEditor(EditorType.SNBT));
        view.getClearButton().onAction(() -> {
            model.setItemStack(ItemStack.EMPTY);
            updateItemName();
        });
    }

    private void updateItemName() {
        ItemStack stack = model.getItemStack();
        MutableComponent name = stack.isEmpty() ? Component.literal("-").withStyle(style -> style.withColor(0xA0A0A0)) : stack.getHoverName().copy();
        if (!stack.isEmpty() && stack.getCount() > 1) {
            name.append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
        view.getItemNameLabel().setLabel(name);
        updateItemTooltip(stack);
        view.getOpenSnbtEditorButton().setDisable(stack.isEmpty());
    }

    private void updateItemTooltip(ItemStack stack) {
        var tooltip = view.getItemNameLabel().getTooltip();
        tooltip.clear();
        if (stack.isEmpty()) {
            tooltip.add(ModTexts.choose(ModTexts.ITEM).copy().withStyle(ChatFormatting.GRAY));
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(ModTexts.COUNT.copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.valueOf(stack.getCount())).withStyle(ChatFormatting.GOLD)));
        tooltip.add(ModTexts.OPEN_SNBT_EDITOR.copy().withStyle(ChatFormatting.GRAY));
    }

    private void openItemSelection(Runnable afterSelection) {
        ModScreenHandler.openListSelectionScreen(ModTexts.ITEM, "container_slot", ClientCache.getItemSelectionItems(), selection -> {
            if (selection == null || selection.isEmpty()) {
                return;
            }
            try {
                Identifier id = Identifier.parse(selection);
                    BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                        model.setItemStack(new ItemStack(item));
                        updateItemName();
                        if (afterSelection != null) {
                            afterSelection.run();
                        }
                    });
            } catch (Exception ignored) {
            }
        });
    }

    private void openEditor(EditorType type) {
        ensureItemStack(() -> openEditorNow(type));
    }

    private void openEditorNow(EditorType type) {
        ItemStack initial = model.getItemStack().copy();
        ItemEditorContext context = new ItemEditorContext(initial, null, false, ctx -> {
            ItemStack result = ctx.getItemStack().copy();
            model.setItemStack(result);
            updateItemName();
        });
        ModScreenHandler.openEditor(type, context);
    }

    private void ensureItemStack(Runnable onReady) {
        if (!model.getItemStack().isEmpty()) {
            onReady.run();
            return;
        }
        openItemSelection(onReady);
    }

    private void openVaultSelection() {
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        if (storedItems.isEmpty()) {
            return;
        }
        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ClientUtil.parseItemStack(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            Identifier id = Identifier.withDefaultNamespace("inventory_vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_inventory_slot", elements, selected -> {
            ItemStack chosen = stacksById.get(selected);
            if (chosen != null) {
                model.setItemStack(chosen.copy());
                updateItemName();
            }
        });
    }
}
