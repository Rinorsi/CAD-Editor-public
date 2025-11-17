package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.Vault;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.VaultEntityListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.EntityEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.franckyi.guapi.api.GuapiHelper.text;

public class EntityEntryController extends ValueEntryController<EntityEntryModel, EntityEntryView> {
    public EntityEntryController(EntityEntryModel model, EntityEntryView view) {
        super(model, view);
    }

    private static final net.minecraft.network.chat.MutableComponent EMPTY_ENTITY_LABEL =
            text("-").withStyle(ChatFormatting.DARK_GRAY);

    @Override
    public void bind() {
        super.bind();
        view.getEntityField().setPlaceholder(ModTexts.ENTITY);
        view.getEntityField().setValidator(value -> {
            if (value == null || value.isEmpty()) {
                return false;
            }
            return ClientUtil.parseResourceLocation(value) != null;
        });
        view.getEntityField().getSuggestions().setAll(ClientCache.getEntitySuggestions());
        view.getEntityField().textProperty().addListener(this::onEntityChanged);
        model.entityIdProperty().addListener(value -> {
            String text = view.getEntityField().getText();
            if (!Objects.equals(text, value)) {
                view.getEntityField().setText(value);
            }
        });
        model.entityTypeProperty().addListener(type -> updateEntityPreview());
        String initial = model.getEntityId();
        view.getEntityField().setText(initial == null ? "" : initial);
        view.getEntityField().validProperty().addListener(valid -> {
            if (!valid) {
                model.setValid(false);
                view.getEntityField().getTooltip().setAll(ModTexts.Messages.errorNoTargetFound(ModTexts.ENTITY));
            } else {
                view.getEntityField().getTooltip().clear();
            }
            updateEntityPreview();
        });
        view.getSelectEntityButton().getTooltip().add(ModTexts.choose(ModTexts.ENTITY));
        view.getSelectEntityButton().onAction(this::openSelectionScreen);
        view.getPasteFromVaultButton().onAction(this::openVaultSelection);
        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenNbtEditorButton().onAction(() -> openEditor(EditorType.NBT));
        view.getOpenSnbtEditorButton().onAction(() -> openEditor(EditorType.SNBT));
        updateEntityPreview();
    }

    private void onEntityChanged(String value) {
        if (view.getEntityField().isValid()) {
            model.setEntityId(value);
        } else {
            model.setValid(false);
        }
    }

    private void updateEntityPreview() {
        EntityType<?> entityType = model.getEntityType();
        if (entityType != null) {
            view.getEntityNameLabel().setLabel(entityType.getDescription().copy().withStyle(ChatFormatting.GRAY));
            view.getEntityIconView().setItem(createIcon(entityType));
        } else {
            view.getEntityNameLabel().setLabel(EMPTY_ENTITY_LABEL);
            view.getEntityIconView().setItem(ItemStack.EMPTY);
        }
    }

    private ItemStack createIcon(EntityType<?> type) {
        SpawnEggItem egg = SpawnEggItem.byId(type);
        return egg != null ? new ItemStack(egg) : ItemStack.EMPTY;
    }

    private void openSelectionScreen() {
        String current = view.getEntityField().getText();
        ResourceLocation location = ClientUtil.parseResourceLocation(current);
        String normalized = location != null ? location.toString() : current;
        ModScreenHandler.openListSelectionScreen(ModTexts.ENTITY, normalized,
                ClientCache.getEntitySelectionItems(), model::setEntityId);
    }

    private void openEditor(EditorType editorType) {
        ModScreenHandler.openEditor(editorType, new EntityEditorContext(model.copyValue(),
                null, false, context -> model.setValue(context.getTag())));
    }

    private void openVaultSelection() {
        List<VaultEntityListSelectionElementModel> elements = new ArrayList<>();
        Map<String, CompoundTag> entitiesById = new LinkedHashMap<>();
        List<CompoundTag> storedEntities = Vault.getInstance().getEntities();
        for (int i = 0; i < storedEntities.size(); i++) {
            CompoundTag tag = storedEntities.get(i);
            if (tag == null || tag.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cadeditor", "entity_entry_vault_" + i);
            elements.add(new VaultEntityListSelectionElementModel(id, tag));
            entitiesById.put(id.toString(), tag.copy());
        }
        if (elements.isEmpty()) {
            return;
        }
        ModScreenHandler.openListSelectionScreen(ModTexts.VAULT, "vault_entity_entry", elements, selectedId -> {
            CompoundTag chosen = entitiesById.get(selectedId);
            if (chosen == null) {
                return;
            }
            model.setValue(chosen.copy());
        });
    }
}
