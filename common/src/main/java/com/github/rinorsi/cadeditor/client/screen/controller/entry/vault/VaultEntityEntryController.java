package com.github.rinorsi.cadeditor.client.screen.controller.entry.vault;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultEntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultEntityEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public class VaultEntityEntryController extends EntryController<VaultEntityEntryModel, VaultEntityEntryView> {
    public VaultEntityEntryController(VaultEntityEntryModel model, VaultEntityEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getIconView().setItem(getEntityIconItem());
        view.getLabel().labelProperty().bind(model.entityProperty().map(Entity::getName));
        view.getButtonBox().getChildren().remove(view.getResetButton());
        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenNBTEditorButton().onAction(() -> openEditor(EditorType.NBT));
        view.getOpenSNBTEditorButton().onAction(() -> openEditor(EditorType.SNBT));
    }

    private void openEditor(EditorType editorType) {
        ModScreenHandler.openEditor(editorType, new EntityEditorContext(model.getData(),
                null, false, context -> model.setData(context.getTag())));
    }

    private ItemStack getEntityIconItem() {
        Entity entity = model.getEntity();
        if (entity == null) {
            return new ItemStack(Items.SPAWNER);
        }
        SpawnEggItem egg = SpawnEggItem.byId(entity.getType());
        return egg != null ? new ItemStack(egg) : new ItemStack(Items.SPAWNER);
    }
}
