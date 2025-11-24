package com.github.rinorsi.cadeditor.client.screen.view.entry;

import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.builder.VBoxBuilder;
import com.github.franckyi.guapi.api.util.Align;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.github.franckyi.guapi.api.GuapiHelper.label;
import static com.github.franckyi.guapi.api.GuapiHelper.vBox;

public class InfoEntryView extends EntryView {
    private VBoxBuilder container;

    @Override
    protected Node createContent() {
        container = vBox();
        return container;
    }

    public void setLines(List<Component> lines) {
        container.getChildren().clear();
        for (Component line : lines) {
            var lbl = label(line).textAlign(Align.CENTER_LEFT);
            container.add(lbl);
        }
    }
}
