package com.github.franckyi.guapi.base.node;

import com.github.franckyi.databindings.api.*;
import com.github.franckyi.guapi.api.event.TypeEvent;
import com.github.franckyi.guapi.api.node.TextField;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

@SuppressWarnings("this-escape")
public abstract class AbstractTextField extends AbstractLabeled implements TextField {
    private final StringProperty textProperty = StringProperty.create("");
    private final IntegerProperty maxLengthProperty = IntegerProperty.create(Integer.MAX_VALUE);
    private final ObjectProperty<Predicate<String>> validatorProperty = ObjectProperty.create(s -> true);
    private final BooleanProperty validationForcedProperty = BooleanProperty.create();
    private final ObservableBooleanValue validProperty;
    private final ObjectProperty<TextRenderer> textRendererProperty = ObjectProperty.create();
    private final IntegerProperty cursorPositionProperty = IntegerProperty.create();
    private final IntegerProperty highlightPositionProperty = IntegerProperty.create();
    private final ObjectProperty<TextFieldEventListener> onTextUpdateProperty = ObjectProperty.create();
    private final ObservableList<String> suggestions = ObservableList.create();
    private final BooleanProperty suggestedProperty = BooleanProperty.create();
    private final ObservableBooleanValue suggestedPropertyReadOnly = ObservableBooleanValue.readOnly(suggestedProperty);
    private final ObjectProperty<Component> placeholderProperty = ObjectProperty.create(EMPTY_TEXT);

    protected AbstractTextField() {
        this("");
    }

    protected AbstractTextField(String value) {
        this(EMPTY_TEXT, value);
    }

    protected AbstractTextField(String label, String value) {
        this(text(label), value);
    }

    protected AbstractTextField(Component label, String value) {
        super(label);
        validProperty = ObservableBooleanValue.observe(() -> getValidator().test(getText()), validatorProperty(), textProperty());
        setText(value);
        textProperty().addListener(this::updateCursorPos);
        textProperty().addListener(this::updateSuggested);
        getSuggestions().addListener(this::updateSuggested);
        focusedProperty().addListener(this::resetSelection);
    }

    @Override
    public StringProperty textProperty() {
        return textProperty;
    }

    @Override
    public IntegerProperty maxLengthProperty() {
        return maxLengthProperty;
    }

    @Override
    public ObjectProperty<Predicate<String>> validatorProperty() {
        return validatorProperty;
    }

    @Override
    public BooleanProperty validationForcedProperty() {
        return validationForcedProperty;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    @Override
    public ObjectProperty<TextRenderer> textRendererProperty() {
        return textRendererProperty;
    }

    @Override
    public IntegerProperty cursorPositionProperty() {
        return cursorPositionProperty;
    }

    @Override
    public IntegerProperty highlightPositionProperty() {
        return highlightPositionProperty;
    }

    @Override
    public ObjectProperty<TextFieldEventListener> onTextUpdateProperty() {
        return onTextUpdateProperty;
    }

    @Override
    public ObservableList<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public ObservableBooleanValue suggestedProperty() {
        return suggestedPropertyReadOnly;
    }

    @Override
    public ObjectProperty<Component> placeholderProperty() {
        return placeholderProperty;
    }

    @Override
    public void charTyped(TypeEvent event) {
        event.consume();
    }

    private void updateCursorPos(String text) {
        if (text == null) {
            setCursorPosition(0);
            setHighlightPosition(0);
        } else {
            if (getCursorPosition() > text.length()) {
                setCursorPosition(getText().length());
            }
            if (getHighlightPosition() > text.length()) {
                setHighlightPosition(getText().length());
            }
        }
    }

    private void updateSuggested() {
        suggestedProperty.setValue(getSuggestions().contains(getText()));
    }

    private void resetSelection() {
        setHighlightPosition(getCursorPosition());
    }
}
