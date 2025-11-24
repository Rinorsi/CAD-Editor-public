package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.InfoEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.InfoEntryView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class InfoEntryController extends EntryController<InfoEntryModel, InfoEntryView> {
    public InfoEntryController(InfoEntryModel model, InfoEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.setLines(splitLines(model.getText()));
        view.getButtonBox().setVisible(false);
    }

    private List<Component> splitLines(MutableComponent original) {
        List<Component> lines = new ArrayList<>();
        String normalized = original.getString().replace("\\n", "\n");
        if (!normalized.contains("\n")) {
            lines.add(original);
            return lines;
        }
        String[] parts = normalized.split("\\n");
        for (String part : parts) {
            MutableComponent line = Component.literal(part).withStyle(original.getStyle());
            lines.add(line);
        }
        return lines;
    }
}
