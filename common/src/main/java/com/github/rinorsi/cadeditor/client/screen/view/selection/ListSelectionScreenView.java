package com.github.rinorsi.cadeditor.client.screen.view.selection;

import com.github.franckyi.guapi.api.node.ListView;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.mvc.ListSelectionElementMVC;
import com.github.rinorsi.cadeditor.client.screen.view.ScreenView;
import com.github.rinorsi.cadeditor.common.ModTexts;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class ListSelectionScreenView extends ScreenView {
    private ListView<ListSelectionElementModel> listView;
    private TextField searchField;

    public ListSelectionScreenView() {
        super();
    }

    @Override
    protected Node createEditor() {
        return hBox(editor -> {
            editor.add(vBox(), 1);
            editor.add(vBox(center -> {
                center.add(searchField = textField().placeholder(ModTexts.SEARCH));
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
}
