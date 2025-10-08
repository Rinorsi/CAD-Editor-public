package com.github.rinorsi.cadeditor.client.screen.model.selection;

import com.github.franckyi.guapi.api.mvc.Model;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record ListSelectionScreenModel(MutableComponent title, String initialValue,
                                       List<? extends ListSelectionElementModel> items,
                                       Consumer<String> action,
                                       boolean multiSelect,
                                       Consumer<List<ResourceLocation>> multiAction,
                                       Set<ResourceLocation> initiallySelected) implements Model {

    public ListSelectionScreenModel(MutableComponent title, String initialValue,
                                    List<? extends ListSelectionElementModel> items,
                                    Consumer<String> action) {
        this(title, initialValue, items, action, false, null, Collections.emptySet());
    }

    public MutableComponent getTitle() {
        return title;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public List<? extends ListSelectionElementModel> getElements() {
        return items;
    }

    public Consumer<String> getAction() {
        return action;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public Consumer<List<ResourceLocation>> getMultiAction() {
        return multiAction;
    }

    public Set<ResourceLocation> getInitiallySelected() {
        return initiallySelected;
    }
}
