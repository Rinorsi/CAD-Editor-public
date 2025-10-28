package com.github.rinorsi.cadeditor.client.screen.model.category;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.ObservableBooleanValue;
import com.github.franckyi.databindings.api.ObservableList;
import com.github.franckyi.guapi.api.mvc.Model;
import com.github.rinorsi.cadeditor.client.screen.model.CategoryEntryScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.AddListEntryEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

@SuppressWarnings("this-escape")
public abstract class CategoryModel implements Model {
    private final ObjectProperty<Component> nameProperty;
    private final ObservableBooleanValue selectedProperty;
    private final ObservableBooleanValue validProperty;
    private final ObservableList<EntryModel> entries = ObservableList.create();
    private final CategoryEntryScreenModel<?> parent;
    private boolean initialized;

    protected CategoryModel(Component name, CategoryEntryScreenModel<?> parent) {
        this.nameProperty = ObjectProperty.create(name);
        this.parent = parent;
        this.selectedProperty = parent.selectedCategoryProperty().is(this);
        this.validProperty = getEntries().allMatch(EntryModel::isValid, EntryModel::validProperty);
    }

    @Override
    public void initalize() {
        if (initialized) {
            getEntries().clear();
        } else {
            initialized = true;
            getEntries().addListener(this::updateEntryListIndexes);
        }
        setupEntries();
        if (hasEntryList()) {
            if (canAddEntryInList()) {
                getEntries().add(new AddListEntryEntryModel(this, ModTexts.addListEntry(getAddListEntryButtonTooltip()).withStyle(ChatFormatting.GREEN)));
            }
            updateEntryListIndexes();
        }
    }

    protected abstract void setupEntries();

    public Component getName() {
        return nameProperty().getValue();
    }

    public ObjectProperty<Component> nameProperty() {
        return nameProperty;
    }

    public void setName(Component value) {
        nameProperty().setValue(value);
    }

    public boolean isSelected() {
        return selectedProperty().getValue();
    }

    public ObservableBooleanValue selectedProperty() {
        return selectedProperty;
    }

    public boolean isValid() {
        return validProperty().getValue();
    }

    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    public CategoryEntryScreenModel<?> getParent() {
        return parent;
    }

    public ObservableList<EntryModel> getEntries() {
        return entries;
    }

    public void updateEntryListIndexes() {
        if (hasEntryList()) {
            for (int i = 0; i < getEntryListSize(); i++) {
                EntryModel entry = getEntries().get(getEntryListIndex(i));
                entry.setListSize(getEntryListSize());
                entry.setListIndex(i);
            }
        }
    }

    public int getEntryListStart() {
        return -1;
    }

    private int getEntryListIndex(int index) {
        return getEntryListStart() + index;
    }

    private int getEntryListSize() {
        return getEntries().size() - getEntryListStart() - (canAddEntryInList() ? 1 : 0);
    }

    private boolean hasEntryList() {
        return getEntryListStart() >= 0;
    }

    protected boolean canAddEntryInList() {
        return true;
    }

    public void addEntryInList() {
        getEntries().add(getEntries().size() - 1, createNewListEntry());
    }

    public EntryModel createNewListEntry() {
        return null;
    }

    protected MutableComponent getAddListEntryButtonTooltip() {
        return EMPTY_TEXT;
    }

    public void moveEntryUp(int index) {
        Collections.swap(getEntries(), getEntryListIndex(index), getEntryListIndex(index) - 1);
    }

    public void moveEntryDown(int index) {
        Collections.swap(getEntries(), getEntryListIndex(index), getEntryListIndex(index) + 1);
    }

    public void deleteEntry(int index) {
        getEntries().remove(getEntryListIndex(index));
    }

    public int getEntryHeight() {
        return 24;
    }

    public void apply() {
        getEntries().forEach(EntryModel::apply);
    }
}
