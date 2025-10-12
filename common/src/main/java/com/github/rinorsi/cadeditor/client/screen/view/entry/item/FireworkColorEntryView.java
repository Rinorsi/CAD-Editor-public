package com.github.rinorsi.cadeditor.client.screen.view.entry.item;

import com.github.franckyi.guapi.api.node.ItemView;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.screen.view.entry.LabeledEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class FireworkColorEntryView extends LabeledEntryView {
    private TexturedButton chooseColorButton;
    private TexturedButton removeColorButton;
    private ItemView previewItem;
    private Label hexLabel;

    @Override
    protected Node createLabeledContent() {
        return hBox(root -> {
            chooseColorButton = texturedButton(ModTextures.COLOR_CUSTOM, 16, 16, false)
                    .tooltip(ModTexts.choose(ModTexts.CUSTOM_COLOR));
            removeColorButton = texturedButton(ModTextures.REMOVE, 16, 16, false)
                    .tooltip(ModTexts.REMOVE);
            previewItem = itemView(new ItemStack(Items.FIREWORK_STAR));
            hexLabel = label("");
            root.add(chooseColorButton);
            root.add(removeColorButton);
            root.add(previewItem);
            root.add(hexLabel, 1);
            root.spacing(5);
        });
    }

    public TexturedButton getChooseColorButton() {
        return chooseColorButton;
    }

    public TexturedButton getRemoveColorButton() {
        return removeColorButton;
    }

    public ItemView getPreviewItem() {
        return previewItem;
    }

    public Label getHexLabel() {
        return hexLabel;
    }
}
