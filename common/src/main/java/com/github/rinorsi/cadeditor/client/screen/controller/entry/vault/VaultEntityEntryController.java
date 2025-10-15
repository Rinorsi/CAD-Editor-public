package com.github.rinorsi.cadeditor.client.screen.controller.entry.vault;

import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultEntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultEntityEntryView;
import com.github.rinorsi.cadeditor.common.EditorType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
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
        Entity entity = model.getEntity();
        if (entity == null) {
            return ResourceLocation.withDefaultNamespace("textures/entity_icon/missing.png");
        }
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(entity);
        EntityRenderState state = (EntityRenderState) renderer.createRenderState(entity, 0.0f);
        ResourceLocation texture = null;
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer && state instanceof LivingEntityRenderState livingState) {
            texture = ((LivingEntityRenderer) livingRenderer).getTextureLocation(livingState);
        }
        if (texture == null) {
            for (var method : renderer.getClass().getMethods()) {
                if (!method.getName().equals("getTextureLocation") || method.getParameterCount() != 1) continue;
                Class<?> parameter = method.getParameterTypes()[0];
                if (!parameter.isInstance(state)) continue;
                try {
                    texture = (ResourceLocation) method.invoke(renderer, state);
                    break;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        if (texture == null) {
            texture = entity.getType().builtInRegistryHolder().unwrapKey()
                    .map(key -> ResourceLocation.fromNamespaceAndPath(key.location().getNamespace(), "textures/entity/" + key.location().getPath() + ".png"))
                    .orElse(ResourceLocation.withDefaultNamespace("textures/entity/missing.png"));
        }
        String rawPath = texture.getPath();
        if (!rawPath.startsWith("textures/")) {
            rawPath = "textures/" + rawPath;
        }
        String iconPath = rawPath.contains("/entity/")
                ? rawPath.replace("/entity/", "/entity_icon/")
                : "textures/entity_icon/" + rawPath.substring("textures/".length());
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), iconPath);
    }
}
