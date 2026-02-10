package com.github.rinorsi.cadeditor.client.screen.view.selection;

import com.github.franckyi.guapi.api.node.Button;
import com.github.franckyi.guapi.api.node.EnumButton;
import com.github.franckyi.guapi.api.node.ListView;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.mvc.ListSelectionElementMVC;
import com.github.rinorsi.cadeditor.client.screen.view.ScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class ListSelectionScreenView extends ScreenView {
    private ListView<ListSelectionElementModel> listView;
    private TextField searchField;
    private EnumButton<ListSelectionFilter> categoryFilterButton;
    private EnumButton<ListSelectionFilter> namespaceFilterButton;
    private Button loadAllButton;

    public ListSelectionScreenView() {
        super();
    }

    @Override
    protected Node createEditor() {
        return hBox(editor -> {
            editor.add(vBox(), 1);
            editor.add(vBox(center -> {
                center.add(hBox(filterRow -> {
                    categoryFilterButton = enumButton();
                    categoryFilterButton.setVisible(false);
                    categoryFilterButton.setMinWidth(0);
                    categoryFilterButton.setPrefWidth(0);
                    categoryFilterButton.setMaxWidth(0);
                    filterRow.add(categoryFilterButton);
                    namespaceFilterButton = enumButton();
                    namespaceFilterButton.setVisible(false);
                    namespaceFilterButton.setMinWidth(0);
                    namespaceFilterButton.setPrefWidth(0);
                    namespaceFilterButton.setMaxWidth(0);
                    filterRow.add(namespaceFilterButton);
                    filterRow.add(loadAllButton = button(ModTexts.LOAD_ALL));
                    filterRow.spacing(5);
                }));
                center.add(hBox(searchRow -> {
                    searchRow.add(searchField = textField().placeholder(ModTexts.SEARCH), 1);
                }));
                center.add(listView = listView(ListSelectionElementModel.class, 25)
                        .renderer(item -> mvc(ListSelectionElementMVC.INSTANCE, item))
                        .padding(5).childrenFocusable(), 1);
                center.spacing(5).fillWidth();
            }), 4);
            editor.add(vBox(), 1);
            editor.spacing(10).fillHeight();
        });
    }

    public ListView<ListSelectionElementModel> getListView() {
        return listView;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public EnumButton<ListSelectionFilter> getCategoryFilterButton() {
        return categoryFilterButton;
    }

    public EnumButton<ListSelectionFilter> getNamespaceFilterButton() {
        return namespaceFilterButton;
    }

    public EnumButton<ListSelectionFilter> getFilterButton() {
        return categoryFilterButton;
    }

    public Button getLoadAllButton() {
        return loadAllButton;
    }
}
