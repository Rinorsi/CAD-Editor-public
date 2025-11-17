package com.github.rinorsi.cadeditor.client.screen.controller.entry.entity;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.VillagerTradeItemsEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.entity.VillagerTradeItemsEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VillagerTradeItemsEntryController extends EntryController<VillagerTradeItemsEntryModel, VillagerTradeItemsEntryView> {
    private final VillagerTradeEntryModel tradeModel;

    public VillagerTradeItemsEntryController(VillagerTradeItemsEntryModel model, VillagerTradeItemsEntryView view) {
        super(model, view);
        this.tradeModel = model.getTradeModel();
    }

    @Override
    public void bind() {
        super.bind();
        updateTitle(model.getListIndex());
        model.listIndexProperty().addListener(this::updateTitle);

        bindItemControls(tradeModel.primaryItemProperty(), view.getPrimaryItemView(), view.getPrimaryItemNameLabel(),
                view.getPrimaryChooseButton(), view.getPrimaryEditButton(), view.getPrimarySnbtButton(),
                view.getPrimaryVaultButton(), view.getPrimaryClearButton());
        bindItemControls(tradeModel.secondaryItemProperty(), view.getSecondaryItemView(), view.getSecondaryItemNameLabel(),
                view.getSecondaryChooseButton(), view.getSecondaryEditButton(), view.getSecondarySnbtButton(),
                view.getSecondaryVaultButton(), view.getSecondaryClearButton());
        bindItemControls(tradeModel.resultItemProperty(), view.getResultItemView(), view.getResultItemNameLabel(),
                view.getResultChooseButton(), view.getResultEditButton(), view.getResultSnbtButton(),
                view.getResultVaultButton(), view.getResultClearButton());
    }

    private void updateTitle(int index) {
        view.getTradeTitleLabel().setLabel(ModTexts.trade(index + 1));
    }

    private void bindItemControls(ObjectProperty<ItemStack> property, ItemView itemView, Label nameLabel,
                                  TexturedButton chooseButton, TexturedButton editButton, TexturedButton snbtButton,
                                  TexturedButton vaultButton, TexturedButton clearButton) {
        itemView.itemProperty().bind(property);
        Runnable refresh = () -> updateItemDisplay(property.getValue(), nameLabel, editButton, snbtButton);
        property.addListener(stack -> refresh.run());
        refresh.run();

        chooseButton.onAction(() -> openItemSelection(property, refresh));
        editButton.onAction(() -> openEditor(property, refresh, EditorType.STANDARD));
        snbtButton.onAction(() -> openEditor(property, refresh, EditorType.SNBT));
        vaultButton.onAction(() -> openVaultSelection(property, refresh));
        clearButton.onAction(() -> {
            property.setValue(ItemStack.EMPTY);
            refresh.run();
        });
    }

    private void updateItemDisplay(ItemStack stack, Label nameLabel, TexturedButton editButton, TexturedButton snbtButton) {
        MutableComponent name = stack.isEmpty()
                ? Component.literal("-").withStyle(style -> style.withColor(0xA0A0A0))
                : stack.getHoverName().copy();
        if (!stack.isEmpty() && stack.getCount() > 1) {
            name.append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
        nameLabel.setLabel(name);
        updateItemTooltip(stack, nameLabel);
        boolean empty = stack.isEmpty();
        editButton.setDisable(empty);
        snbtButton.setDisable(empty);
    }

    private void updateItemTooltip(ItemStack stack, Label label) {
        var tooltip = label.getTooltip();
        tooltip.clear();
        if (stack.isEmpty()) {
            tooltip.add(ModTexts.choose(ModTexts.ITEM).copy().withStyle(ChatFormatting.GRAY));
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(ModTexts.COUNT.copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.valueOf(stack.getCount())).withStyle(ChatFormatting.GOLD)));
        tooltip.add(ModTexts.OPEN_SNBT_EDITOR.copy().withStyle(ChatFormatting.GRAY));
    }

    private void openItemSelection(ObjectProperty<ItemStack> property, Runnable afterSelection) {
        String initial = property.getValue().isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(property.getValue().getItem()).toString();
        ModScreenHandler.openListSelectionScreen(ModTexts.ITEM, initial, ClientCache.getItemSelectionItems(), selection -> {
            if (selection == null || selection.isEmpty()) {
                return;
            }
            try {
                ResourceLocation id = ResourceLocation.parse(selection);
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                    property.setValue(new ItemStack(item));
                    afterSelection.run();
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void openEditor(ObjectProperty<ItemStack> property, Runnable refresher, EditorType type) {
        ensureItemStack(property, refresher, () -> openEditorNow(property, refresher, type));
    }

    private void openEditorNow(ObjectProperty<ItemStack> property, Runnable refresher, EditorType type) {
        ItemStack initial = property.getValue().copy();
        ItemEditorContext context = new ItemEditorContext(initial, null, false, ctx -> {
            ItemStack result = ctx.getItemStack().copy();
            property.setValue(result);
            refresher.run();
        });
        ModScreenHandler.openEditor(type, context);
    }

    private void openVaultSelection(ObjectProperty<ItemStack> property, Runnable refresher) {
        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<net.minecraft.nbt.CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ClientUtil.parseItemStack(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "villager_trade_vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "villager_trade_vault", elements, selection -> {
            ItemStack chosen = stacksById.get(selection);
            if (chosen == null) {
                return;
            }
            property.setValue(chosen.copy());
            refresher.run();
        });
    }

    private void ensureItemStack(ObjectProperty<ItemStack> property, Runnable refresher, Runnable onReady) {
        if (!property.getValue().isEmpty()) {
            onReady.run();
            return;
        }
        openItemSelection(property, () -> {
            refresher.run();
            if (!property.getValue().isEmpty()) {
                onReady.run();
            }
        });
    }

    @Override
    protected void resetModel() {
        super.resetModel();
        updateTitle(model.getListIndex());
    }
}
