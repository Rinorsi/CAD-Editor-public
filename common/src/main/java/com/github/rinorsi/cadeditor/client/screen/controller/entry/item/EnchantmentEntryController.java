package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.franckyi.guapi.api.util.Predicates;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.SelectionEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemEnchantmentsCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.EnchantmentEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.EnchantmentEntryView;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.franckyi.guapi.api.GuapiHelper.translated;

public class EnchantmentEntryController extends SelectionEntryController<EnchantmentEntryModel, EnchantmentEntryView> {
    public EnchantmentEntryController(EnchantmentEntryModel model, EnchantmentEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getPlusButton().onAction(() -> model.levelProperty().incr());
        view.getMinusButton().onAction(() -> model.levelProperty().decr());
        view.getLevelField().setValidator(Predicates.range(0, 256));
        view.getLevelField().textProperty().addListener(value -> {
            if (view.getLevelField().isValid()) {
                int level = Integer.parseInt(value);
                model.setLevel(level);
                view.getPlusButton().setDisable(level == 255);
                view.getMinusButton().setDisable(level == 0);
            } else {
                view.getPlusButton().setDisable(true);
                view.getMinusButton().setDisable(true);
            }
        });
        view.getLevelField().setText(Integer.toString(model.getLevel()));
        model.levelProperty().addListener(value -> view.getLevelField().setText(Integer.toString(value)));
        model.validProperty().bind(view.getLevelField().validProperty());
        model.valueProperty().addListener(this::updatePreview);
        updatePreview(model.getValue());
    }

    @Override
    protected void openSelectionScreen() {
        ItemEnchantmentsCategoryModel category = (ItemEnchantmentsCategoryModel) model.getCategory();
        Set<Identifier> selected = new HashSet<>(category.getExistingEnchantmentIds());
        Identifier currentId = parseResourceLocation(model.getValue());
        if (currentId != null) {
            selected.add(currentId);
        }
        List<? extends ListSelectionElementModel> items = model.getSelectionItems();
        ModScreenHandler.openListSelectionScreen(model.getSelectionScreenTitle(),
                model.getValue().contains(":") ? model.getValue() : "minecraft:" + model.getValue(),
                items,
                model::setValue,
                true,
                ids -> {
                    category.syncSelection(new HashSet<>(ids), model);
                },
                selected);
    }

    private void updatePreview(String value) {
        Identifier id = parseResourceLocation(value);
        if (id == null) {
            view.setPreviewVisible(false);
            return;
        }
        ClientCache.findEnchantmentSelectionItem(id).ifPresentOrElse(item -> {
            view.getPreviewItemView().setItem(item.getItem());
            view.getPreviewLabel().setLabel(translated(item.getName()).withStyle(ChatFormatting.GRAY));
            view.setPreviewVisible(true);
        }, () -> view.setPreviewVisible(false));
    }
}
