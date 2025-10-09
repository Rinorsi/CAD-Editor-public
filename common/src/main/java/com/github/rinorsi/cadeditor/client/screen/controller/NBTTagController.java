package com.github.rinorsi.cadeditor.client.screen.controller;

import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.model.NBTTagModel;
import com.github.rinorsi.cadeditor.client.screen.view.NBTTagView;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.StringSuggestionListSelectionElementModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class NBTTagController extends AbstractController<NBTTagModel, NBTTagView> {
    public NBTTagController(NBTTagModel model, NBTTagView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        view.getNameField().textProperty().bindBidirectional(model.nameProperty());
        view.getValueField().textProperty().bindBidirectional(model.valueProperty());
        if (model.getName() == null) {
            view.getRoot().getChildren().removeAll(view.getNameField(), view.getSeparator());
            if (model.getParent() == null) {
                view.getRoot().getChildren().add(label(text("(root)")));
            } else {
                view.getRoot().getChildren().add(1, label(text("(%d)".formatted(model.getParent().getChildren().indexOf(model)))));
            }
        }
        if (model.getValue() == null) {
            view.getRoot().getChildren().remove(view.getValueField());
            view.getRoot().getChildren().remove(view.getSuggestionButton());
            view.getValueField().getSuggestions().clear();
            view.getSuggestionButton().setDisable(true);
            view.getSuggestionButton().setVisible(false);
        } else {
            if (!view.getRoot().getChildren().contains(view.getValueField())) {
                int insertIndex = view.getRoot().getChildren().indexOf(view.getSuggestionButton());
                if (insertIndex < 0) {
                    insertIndex = view.getRoot().getChildren().size();
                }
                view.getRoot().getChildren().add(insertIndex, view.getValueField());
            }
            model.validProperty().bind(view.getValueField().validProperty());
            view.getRoot().getChildren().remove(view.getSuggestionButton());
            if (model.getTagType() == Tag.TAG_STRING) {
                List<String> suggestions = model.getStringSuggestions();
                if (!suggestions.isEmpty()) {
                    List<String> distinctSuggestions = suggestions.stream()
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    if (distinctSuggestions.isEmpty()) {
                        view.getValueField().getSuggestions().clear();
                        view.getSuggestionButton().setDisable(true);
                        view.getSuggestionButton().setVisible(false);
                    } else {
                        view.getValueField().getSuggestions().setAll(distinctSuggestions);
                        var suggestionButton = view.getSuggestionButton();
                        if (!view.getRoot().getChildren().contains(suggestionButton)) {
                            view.getRoot().getChildren().add(suggestionButton);
                        }
                        suggestionButton.setDisable(false);
                        suggestionButton.setVisible(true);
                        suggestionButton.getTooltip().setAll(List.of(ModTexts.SEARCH));

                        List<StringSuggestionListSelectionElementModel> items = distinctSuggestions.stream()
                                .map(StringSuggestionListSelectionElementModel::new)
                                .collect(Collectors.toList());
                        Map<String, String> idToValue = new LinkedHashMap<>();
                        items.forEach(item -> idToValue.put(item.getId().toString(), item.getValue()));
                        suggestionButton.onAction(() -> {
                            String currentId = idToValue.entrySet()
                                    .stream()
                                    .filter(entry -> Objects.equals(entry.getValue(), model.getValue()))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse("");
                            MutableComponent title = model.getName() != null ? text(model.getName()) : text("value");
                            ModScreenHandler.openListSelectionScreen(
                                    title,
                                    currentId,
                                    items,
                                    selectedId -> {
                                        String selectedValue = idToValue.get(selectedId);
                                        if (selectedValue != null) {
                                            view.getValueField().setText(selectedValue);
                                        }
                                    }
                            );
                        });
                    }
                } else {
                    view.getValueField().getSuggestions().clear();
                    view.getSuggestionButton().setDisable(true);
                    view.getSuggestionButton().setVisible(false);
                }
            } else {
                view.getValueField().getSuggestions().clear();
                view.getSuggestionButton().setDisable(true);
                view.getSuggestionButton().setVisible(false);
            }
        }
    }
}
