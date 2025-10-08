package com.github.rinorsi.cadeditor.client.screen.mvc;

import com.github.franckyi.guapi.api.mvc.MVC;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.*;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.entity.EntityEquipmentEntryController;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.item.*;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.item.ToolRuleEntryController;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.item.MapDecorationEntryController;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.vault.VaultEntityEntryController;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.vault.VaultItemEntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.*;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.EntityEquipmentEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.*;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ToolRuleEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.MapDecorationEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultEntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.vault.VaultItemEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.*;
import com.github.rinorsi.cadeditor.client.screen.view.entry.entity.EntityEquipmentEntryView;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.*;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.ToolRuleEntryView;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.MapDecorationEntryView;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultEntityEntryView;
import com.github.rinorsi.cadeditor.client.screen.view.entry.vault.VaultItemEntryView;

import java.util.function.Supplier;

public final class EntryMVC implements MVC<EntryModel, EntryView, EntryController<EntryModel, EntryView>> {
    public static final EntryMVC INSTANCE = new EntryMVC();

    @Override
    public EntryView setup(EntryModel model) {
        return switch (model.getType()) {
            case STRING ->
                    MVC.createViewAndBind((StringEntryModel) model, StringEntryView::new, StringEntryController::new);
            case NUMBER ->
                    MVC.createViewAndBind((NumberEntryModel<?>) model, NumberEntryView::new, NumberEntryController::new);
            case TEXT -> MVC.createViewAndBind((TextEntryModel) model, TextEntryView::new, TextEntryController::new);
            case ENUM -> createEnumViewAndBind((EnumEntryModel<?>) model);
            case ACTION ->
                    MVC.createViewAndBind((ActionEntryModel) model, ActionEntryView::new, ActionEntryController::new);
            case ADD_LIST_ENTRY ->
                    MVC.createViewAndBind((AddListEntryEntryModel) model, AddListEntryEntryView::new, AddListEntryEntryController::new);
            case BOOLEAN ->
                    MVC.createViewAndBind((BooleanEntryModel) model, BooleanEntryView::new, BooleanEntryController::new);
            case ITEM -> throw new AssertionError("Not implemented");
            case ENTITY ->
                    MVC.createViewAndBind((EntityEntryModel) model, EntityEntryView::new, EntityEntryController::new);
            case ENCHANTMENT ->
                    MVC.createViewAndBind((EnchantmentEntryModel) model, EnchantmentEntryView::new, EnchantmentEntryController::new);
            case HIDE_FLAG ->
                    MVC.createViewAndBind((HideFlagEntryModel) model, HideFlagEntryView::new, HideFlagEntryController::new);
            case ATTRIBUTE_MODIFIER ->
                    MVC.createViewAndBind((AttributeModifierEntryModel) model, AttributeModifierEntryView::new, AttributeModifierEntryController::new);
            case SELECTION ->
                    MVC.createViewAndBind((SelectionEntryModel) model, SelectionEntryView::new, SelectionEntryController::new);
            case SELECTION_POTION ->
                    MVC.createViewAndBind(((PotionSelectionEntryModel) model), PotionSelectionEntryView::new, PotionSelectionEntryController::new);
            case POTION_EFFECT ->
                    MVC.createViewAndBind(((PotionEffectEntryModel) model), PotionEffectEntryView::new, PotionEffectEntryController::new);
            case FOOD_EFFECT -> {
                    FoodEffectEntryModel foodModel = (FoodEffectEntryModel) model;
                    foodModel.initalize();
                    FoodEffectEntryView foodView = new FoodEffectEntryView();
                    foodView.build();
                    FoodEffectEntryController foodController = new FoodEffectEntryController(foodModel, foodView);
                    foodController.bind();
                    yield foodView;
                }
            case ARMOR_COLOR ->
                    MVC.createViewAndBind((ArmorColorEntryModel) model, ArmorColorEntryView::new, ArmorColorEntryController::new);
            case VAULT_ITEM ->
                    MVC.createViewAndBind((VaultItemEntryModel) model, VaultItemEntryView::new, VaultItemEntryController::new);
            case ENTITY_EQUIPMENT ->
                    MVC.createViewAndBind((EntityEquipmentEntryModel) model, EntityEquipmentEntryView::new, EntityEquipmentEntryController::new);
            case VAULT_ENTITY ->
                    MVC.createViewAndBind((VaultEntityEntryModel) model, VaultEntityEntryView::new, VaultEntityEntryController::new);
            case CONTAINER_SLOT ->
                    MVC.createViewAndBind((ItemContainerSlotEntryModel) model, ItemContainerSlotEntryView::new, ItemContainerSlotEntryController::new);
            case MAP_DECORATION ->
                    MVC.createViewAndBind((MapDecorationEntryModel) model, MapDecorationEntryView::new, MapDecorationEntryController::new);
            case TOOL_RULE ->
                    MVC.createViewAndBind((ToolRuleEntryModel) model, ToolRuleEntryView::new, ToolRuleEntryController::new);
        };
    }

    private <E> EntryView createEnumViewAndBind(EnumEntryModel<E> model) {
        return MVC.createViewAndBind(model, (Supplier<EnumEntryView<E>>) EnumEntryView::new, EnumEntryController::new);
    }
}
