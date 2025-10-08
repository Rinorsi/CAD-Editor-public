package com.github.rinorsi.cadeditor.client.screen.controller.entry.vault;

import com.github.franckyi.guapi.api.Guapi;
import com.github.rinorsi.cadeditor.client.ClientContext;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.logic.ClientVaultActionLogic;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultItemEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultItemEntryView;
import com.github.rinorsi.cadeditor.common.CommonUtil;
import com.github.rinorsi.cadeditor.common.EditorType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class VaultItemEntryController extends EntryController<VaultItemEntryModel, VaultItemEntryView> {
    public VaultItemEntryController(VaultItemEntryModel model, VaultItemEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getItemView().itemProperty().bind(model.itemStackProperty());
        view.getLabel().labelProperty().bind(model.itemStackProperty().map(ItemStack::getHoverName));
        view.getButtonBox().getChildren().remove(view.getResetButton());
        view.getGiveItemButton().setDisable(!(Minecraft.getInstance().player.isCreative() || ClientContext.isModInstalledOnServer()));
        view.getGiveItemButton().onAction(() -> {
            int slot = ClientUtil.findSlot(model.getItemStack());
            if (Minecraft.getInstance().player.isCreative()) {
                Minecraft.getInstance().player.connection.send(new ServerboundSetCreativeModeSlotPacket(slot, model.getItemStack()));
                //Minecraft.getInstance().player.getInventory().setItem(slot, model.getItemStack());
                CommonUtil.showVaultItemGiveSuccess(Minecraft.getInstance().player);
            } else if (ClientContext.isModInstalledOnServer()) {
                ClientVaultActionLogic.giveVaultItem(slot, model.getItemStack());
            } else return;
            Guapi.getScreenHandler().hideScene();
        });
        view.getOpenEditorButton().onAction(() -> openEditor(EditorType.STANDARD));
        view.getOpenNBTEditorButton().onAction(() -> openEditor(EditorType.NBT));
        view.getOpenSNBTEditorButton().onAction(() -> openEditor(EditorType.SNBT));
    }

    private void openEditor(EditorType editorType) {
        ModScreenHandler.openEditor(editorType, new ItemEditorContext(model.getItemStack(),
                null, false, context -> model.setItemStack(context.getItemStack())));
    }
}
