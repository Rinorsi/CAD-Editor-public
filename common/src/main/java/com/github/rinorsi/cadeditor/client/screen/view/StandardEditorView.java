package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.TexturedButton;
import com.github.franckyi.guapi.api.node.builder.TexturedButtonBuilder;
import com.github.rinorsi.cadeditor.client.ModTextures;
import com.github.rinorsi.cadeditor.client.util.texteditor.StyleType;
import com.github.rinorsi.cadeditor.client.util.texteditor.TextEditorActionHandler;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class StandardEditorView extends CategoryEntryScreenView {
    private List<TexturedButton> colorButtons;
    private TexturedButton customColorButton;
    private TexturedButtonBuilder chooseCustomColorButton;
    private HBox textEditorButtons;
    private Supplier<TextEditorActionHandler> textEditorSupplier;

    public StandardEditorView() {
        super();
    }

    @Override
    public void build() {
        super.build();
        colorButtons = Arrays.asList(
                createTextColorButton(BLACK, ModTextures.COLOR_BLACK, ModTexts.BLACK),
                createTextColorButton(DARK_BLUE, ModTextures.COLOR_DARK_BLUE, ModTexts.DARK_BLUE),
                createTextColorButton(DARK_GREEN, ModTextures.COLOR_DARK_GREEN, ModTexts.DARK_GREEN),
                createTextColorButton(DARK_AQUA, ModTextures.COLOR_DARK_AQUA, ModTexts.DARK_AQUA),
                createTextColorButton(DARK_RED, ModTextures.COLOR_DARK_RED, ModTexts.DARK_RED),
                createTextColorButton(DARK_PURPLE, ModTextures.COLOR_DARK_PURPLE, ModTexts.DARK_PURPLE),
                createTextColorButton(GOLD, ModTextures.COLOR_GOLD, ModTexts.GOLD),
                createTextColorButton(GRAY, ModTextures.COLOR_GRAY, ModTexts.GRAY),
                createTextColorButton(DARK_GRAY, ModTextures.COLOR_DARK_GRAY, ModTexts.DARK_GRAY),
                createTextColorButton(BLUE, ModTextures.COLOR_BLUE, ModTexts.BLUE),
                createTextColorButton(GREEN, ModTextures.COLOR_GREEN, ModTexts.GREEN),
                createTextColorButton(AQUA, ModTextures.COLOR_AQUA, ModTexts.AQUA),
                createTextColorButton(RED, ModTextures.COLOR_RED, ModTexts.RED),
                createTextColorButton(LIGHT_PURPLE, ModTextures.COLOR_LIGHT_PURPLE, ModTexts.LIGHT_PURPLE),
                createTextColorButton(YELLOW, ModTextures.COLOR_YELLOW, ModTexts.YELLOW),
                createTextColorButton(WHITE, ModTextures.COLOR_WHITE, ModTexts.WHITE)
        );
        buttonBarRight.getChildren().add(0, textEditorButtons = hBox(buttons -> {
            buttons.add(hBox(middle -> {
                middle.add(createTextButton(StyleType.BOLD, ModTextures.TEXT_BOLD, ModTexts.BOLD));
                middle.add(createTextButton(StyleType.ITALIC, ModTextures.TEXT_ITALIC, ModTexts.ITALIC));
                middle.add(createTextButton(StyleType.UNDERLINED, ModTextures.TEXT_UNDERLINED, ModTexts.UNDERLINED));
                middle.add(createTextButton(StyleType.STRIKETHROUGH, ModTextures.TEXT_STRIKETHROUGH, ModTexts.STRIKETHROUGH));
                middle.add(createTextButton(StyleType.OBFUSCATED, ModTextures.TEXT_OBFUSCATED, ModTexts.OBFUSCATED));
                middle.spacing(2);
            }));
            buttons.add(hBox(right -> {
                right.add(texturedButton(ModTextures.RESET_COLOR, 7, 16, false)
                        .tooltip(ModTexts.RESET_COLOR)
                        .action(e -> {
                            if (textEditorSupplier != null) {
                                e.consume();
                                textEditorSupplier.get().removeColorFormatting();
                            }
                        }));
                right.add(vBox(colors -> {
                    colors.add(hBox(2, colorButtons.subList(0, colorButtons.size() / 2)));
                    colors.add(hBox(2, colorButtons.subList(colorButtons.size() / 2, colorButtons.size())));
                    colors.spacing(2);
                }));
                right.add(customColorButton = texturedButton(null, 1, 1, false)
                        .prefSize(7, 16).visible(false)
                        .tooltip(ModTexts.CUSTOM_COLOR));
                right.add(chooseCustomColorButton = texturedButton(ModTextures.COLOR_CUSTOM, 16, 16, false)
                        .tooltip(ModTexts.choose(ModTexts.CUSTOM_COLOR)));
                right.spacing(2);
            }));
            buttons.spacing(10);
        }).align(CENTER_RIGHT));
    }

    private TexturedButtonBuilder createTextButton(StyleType target, ResourceLocation id, MutableComponent tooltipText) {
        return texturedButton(id, 16, 16, false)
                .tooltip(tooltipText)
                .action(e -> {
                    if (textEditorSupplier != null) {
                        e.consume();
                        textEditorSupplier.get().addStyleFormatting(target);
                    }
                });
    }

    private TexturedButtonBuilder createTextColorButton(String color, ResourceLocation id, MutableComponent tooltipText) {
        return texturedButton(id, 7, 7, false)
                .tooltip(tooltipText)
                .action(e -> {
                    if (textEditorSupplier != null) {
                        e.consume();
                        textEditorSupplier.get().addColorFormatting(color);
                    }
                });
    }

    public TexturedButton getCustomColorButton() {
        return customColorButton;
    }

    public TexturedButton getChooseCustomColorButton() {
        return chooseCustomColorButton;
    }

    public HBox getTextEditorButtons() {
        return textEditorButtons;
    }

    public void setTextEditorSupplier(Supplier<TextEditorActionHandler> supplier) {
        textEditorSupplier = supplier;
    }
}
