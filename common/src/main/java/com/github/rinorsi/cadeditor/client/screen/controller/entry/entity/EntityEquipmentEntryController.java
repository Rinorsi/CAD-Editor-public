package com.github.rinorsi.cadeditor.client.screen.controller.entry.entity;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityEquipmentCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.EntityEquipmentEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.entity.EntityEquipmentEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultItemListSelectionElementModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class EntityEquipmentEntryController extends EntryController<EntityEquipmentEntryModel, EntityEquipmentEntryView> {
    public EntityEquipmentEntryController(EntityEquipmentEntryModel model, EntityEquipmentEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.setListButtonsVisible(false);
        view.disableDeleteButton();
        view.getUpButton().setVisible(false);
        view.getDownButton().setVisible(false);
        view.getChooseItemButton().onAction(() -> openItemSelection(null));
        view.getLoadVaultButton().onAction(this::openVaultSelection);
        view.getChooseItemButton().setDisable(false);

        view.getSlotLabel().setLabel(model.getSlotLabel());
        view.getItemView().itemProperty().bind(model.itemStackProperty());
        model.itemStackProperty().addListener(stack -> updateItemName());
        updateItemName();

        view.getDropChanceField().setValidator(model::isDropChanceTextValid);
        view.getDropChanceField().setText(model.formatDropChance());
        view.getDropChanceField().textProperty().addListener(value -> {
            if (!view.getDropChanceField().isValid()) {
                model.setValid(false);
                return;
            }
            if (!Objects.equals(value, model.formatDropChance())) {
                model.setDropChanceFromText(value);
            }
        });
        model.dropChanceProperty().addListener(value -> {
            String formatted = model.formatDropChance();
            if (!Objects.equals(formatted, view.getDropChanceField().getText())) {
                view.getDropChanceField().setText(formatted);
            }
        });
        view.getDropChanceField().validProperty().addListener(model::setValid);
        view.getDropChanceField().onKeyPress(event -> {
            if (event.isConsumed()) {
                return;
            }
            int key = event.getKeyCode();
            if (key != GLFW.GLFW_KEY_UP && key != GLFW.GLFW_KEY_DOWN) {
                return;
            }
            String text = view.getDropChanceField().getText();
            if (!model.isDropChanceTextValid(text)) {
                return;
            }
            float current = Float.parseFloat(text);
            float step = 0.05f;
            if (event.isShiftKeyDown()) {
                step = 0.1f;
            } else if (event.isControlKeyDown()) {
                step = 0.01f;
            }
            if (key == GLFW.GLFW_KEY_DOWN) {
                step = -step;
            }
            float next = Math.max(0f, Math.min(1f, current + step));
            if (Math.abs(next - current) < EntityEquipmentCategoryModel.DROP_EPSILON) {
                return;
            }
            model.setDropChance(next);
            event.consume();
        });

        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenSnbtEditorButton().onAction(() -> openEditor(EditorType.SNBT));
        view.getClearButton().onAction(() -> {
            model.setItemStack(ItemStack.EMPTY);
        });
    }

    private void updateItemName() {
        ItemStack stack = model.getItemStack();
        Component name = stack.isEmpty() ? Component.literal("-").withStyle(style -> style.withColor(0xA0A0A0)) : stack.getHoverName().copy();
        view.getItemNameLabel().setLabel(name);
        boolean empty = stack.isEmpty();
        view.getOpenSnbtEditorButton().setDisable(empty);
    }

    private void openEditor(EditorType type) {
        ensureItemStack(() -> openEditorNow(type));
    }

    private void openEditorNow(EditorType type) {
        ItemStack initial = model.getItemStack().copy();
        ItemEditorContext context = new ItemEditorContext(initial, null, false, ctx -> {
            ItemStack result = ctx.getItemStack().copy();
            model.setItemStack(result);
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

    private void openItemSelection(Runnable afterSelection) {
        ModScreenHandler.openListSelectionScreen(ModTexts.ITEM, "equipment", ClientCache.getItemSelectionItems(), selection -> {
            if (selection == null || selection.isEmpty()) {
                return;
            }
            try {
                Identifier id = Identifier.parse(selection);
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                    model.setItemStack(new ItemStack(item));
                    if (afterSelection != null) {
                        afterSelection.run();
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void openVaultSelection() {
        List<VaultItemListSelectionElementModel> elements = new ArrayList<>();
        Map<String, ItemStack> stacksById = new LinkedHashMap<>();
        List<CompoundTag> storedItems = Vault.getInstance().getItems();
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = ClientUtil.parseItemStack(ClientUtil.registryAccess(), storedItems.get(i));
            if (stack.isEmpty()) {
                continue;
            }
            Identifier id = Identifier.fromNamespaceAndPath("cadeditor", "equipment_vault_item_" + i);
            elements.add(new VaultItemListSelectionElementModel(id, stack));
            stacksById.put(id.toString(), stack.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_item_equipment", elements, selectedId -> {
            ItemStack chosen = stacksById.get(selectedId);
            if (chosen == null) {
                return;
            }
            model.setItemStack(chosen.copy());
        });
    }
}
