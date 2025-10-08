package com.github.rinorsi.cadeditor.client.screen.controller.entry.vault;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultEntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultEntityEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class VaultEntityEntryController extends EntryController<VaultEntityEntryModel, VaultEntityEntryView> {
    public VaultEntityEntryController(VaultEntityEntryModel model, VaultEntityEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getImageView().setTextureId(getEntityIconTexture());
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

    private ResourceLocation getEntityIconTexture() {
        var entityTexture = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(model.getEntity()).getTextureLocation(model.getEntity());
        return ResourceLocation.fromNamespaceAndPath(entityTexture.getNamespace(), entityTexture.getPath().replace("/entity/", "/entity_icon/"));
    }
}
