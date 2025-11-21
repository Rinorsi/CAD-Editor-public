package com.github.rinorsi.cadeditor.client.screen.controller.selection;

import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.mvc.AbstractController;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SelectableListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.view.selection.ListSelectionScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListSelectionScreenController extends AbstractController<ListSelectionScreenModel, ListSelectionScreenView> {
    private static final Logger LOGGER = LogManager.getLogger("CAD-Editor/Selection");
    private ListSelectionFilter activeFilter;
    private boolean showAllItems;

    public ListSelectionScreenController(ListSelectionScreenModel model, ListSelectionScreenView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        view.getHeaderLabel().setLabel(ModTexts.title(ModTexts.choose(model.getTitle())));
        view.getSearchField().textProperty().addListener(this::filter);
        setupFilterButton();
        setupLoadAllButton();
        if (model.isMultiSelect()) {
            model.getElements().forEach(item -> initializeSelectableItem(item));
        } else {
            model.getElements().forEach(item -> {
                if (item.getId().toString().equals(model.getInitialValue())) {
                    view.getListView().setFocusedElement(item);
                }
            });
            view.getListView().focusedElementProperty().addListener(this::refreshButton);
        }
        view.getCancelButton().onAction(Guapi.getScreenHandler()::hideScene);
        if (model.isMultiSelect()) {
            view.getDoneButton().onAction(this::confirmMultiSelection);
        } else {
            view.getDoneButton().onAction(this::confirmSingleSelection);
        }
        refreshButton();
        filter("");
    }

    private void setupFilterButton() {
        var filters = model.getFilters();
        if (filters == null || filters.isEmpty()) {
            view.getFilterButton().setVisible(false);
            view.getFilterButton().setMinWidth(0);
            view.getFilterButton().setPrefWidth(0);
            view.getFilterButton().setMaxWidth(0);
            return;
        }
        view.getFilterButton().setVisible(true);
        view.getFilterButton().setMinWidth(80);
        view.getFilterButton().setPrefWidth(100);
        view.getFilterButton().setMaxWidth(140);
        view.getFilterButton().getValues().setAll(filters);
        view.getFilterButton().setTextFactory(ListSelectionFilter::label);
        Optional<ListSelectionFilter> initial = filters.stream()
                .filter(filter -> filter.getId().equals(model.getInitialFilterId()))
                .findFirst();
        activeFilter = initial.orElse(filters.get(0));
        view.getFilterButton().setValue(activeFilter);
        view.getFilterButton().valueProperty().addListener(() -> {
            activeFilter = view.getFilterButton().getValue();
            filter(view.getSearchField().getText());
        });
    }

    private void setupLoadAllButton() {
        view.getLoadAllButton().setLabel(ModTexts.LOAD_ALL);
        view.getLoadAllButton().onAction(() -> {
            showAllItems = true;
            view.getLoadAllButton().setLabel(ModTexts.SHOWING_ALL);
            view.getLoadAllButton().setDisable(true);
            filter(view.getSearchField().getText());
        });
    }

    private void filter(String filter) {
        var stream = model.getElements()
                .stream()
                .filter(item -> activeFilter == null || activeFilter.test(item))
                .filter(item -> item.matches(filter));
        if (!showAllItems) {
            stream = stream.limit(ClientConfiguration.INSTANCE.getSelectionScreenMaxItems());
        }
        view.getListView().getItems().setAll(stream.toList());
        refreshButton();
    }

    private void refreshButton() {
        if (model.isMultiSelect()) {
            view.getDoneButton().setDisable(false);
        } else {
            view.getDoneButton().setDisable(view.getListView().getFocusedElement() == null);
        }
    }

    private void initializeSelectableItem(ListSelectionElementModel item) {
        if (item instanceof SelectableListSelectionElementModel selectable) {
            ResourceLocation id = item.getId();
            selectable.setSelected(model.getInitiallySelected().contains(id));
            selectable.selectedProperty().addListener(this::refreshButton);
        }
    }

    private void confirmSingleSelection() {
        ListSelectionElementModel focused = view.getListView().getFocusedElement();
        if (focused == null) {
            return;
        }
        runAndClose(() -> {
            if (model.getAction() != null) {
                model.getAction().accept(focused.getId().toString());
            }
        });
    }

    private void confirmMultiSelection() {
        runAndClose(() -> {
            if (model.getMultiAction() == null) {
                return;
            }
            List<ResourceLocation> selectedIds = model.getElements().stream()
                    .filter(SelectableListSelectionElementModel.class::isInstance)
                    .map(SelectableListSelectionElementModel.class::cast)
                    .filter(SelectableListSelectionElementModel::isSelected)
                    .map(item -> ((ListSelectionElementModel) item).getId())
                    .collect(Collectors.toCollection(() -> new ArrayList<ResourceLocation>()));
            model.getMultiAction().accept(selectedIds);
        });
    }

    private void runAndClose(Runnable action) {
        try {
            if (action != null) {
                action.run();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to apply selection for '{}'", model.getTitle().getString(), e);
            ClientUtil.showMessage(ModTexts.Messages.ERROR_GENERIC);
        } finally {
            Guapi.getScreenHandler().hideScene();
        }
    }

}