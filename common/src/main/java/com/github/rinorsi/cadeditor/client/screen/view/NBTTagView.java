package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.mvc.View;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.TextField;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.rinorsi.cadeditor.client.ModTextures;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class NBTTagView implements View {
    private final ResourceLocation texture;
    private final MutableComponent tooltip;
    private Predicate<String> validator;
    private HBox root;
    private TextField nameField;
    private Label separator;
    private TextField valueField;
    private TexturedButton suggestionButton;

    public NBTTagView(ResourceLocation texture, MutableComponent tooltip, Predicate<String> validator) {
        this(texture, tooltip);
        this.validator = validator;
    }

    public NBTTagView(ResourceLocation texture, MutableComponent tooltip) {
        this.texture = texture;
        this.tooltip = tooltip;
    }

    @Override
    public void build() {
        root = hBox(root -> {
            root.add(imageView(texture, 16, 16).tooltip(tooltip));
            root.add(nameField = textField().prefHeight(14).prefWidth(120));
            root.add(separator = label(":"));
            root.add(valueField = textField().prefHeight(14));
            root.add(suggestionButton = texturedButton(ModTextures.SEARCH, 16, 16, false));
            root.spacing(5).align(CENTER_LEFT);
        });
        if (validator != null) {
            valueField.setValidator(validator);
        }
        suggestionButton.setVisible(false);
        suggestionButton.setDisable(true);
    }

    @Override
    public HBox getRoot() {
        return root;
    }

    public TextField getNameField() {
        return nameField;
    }

    public Label getSeparator() {
        return separator;
    }

    public TextField getValueField() {
        return valueField;
    }

    public TexturedButton getSuggestionButton() {
        return suggestionButton;
    }
}
