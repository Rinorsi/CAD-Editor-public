package com.github.rinorsi.cadeditor.client.screen.controller.entry;

import com.github.rinorsi.cadeditor.client.screen.model.entry.NumberEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.NumberEntryView;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class NumberEntryController<N extends Number> extends ValueEntryController<NumberEntryModel<N>, NumberEntryView> {
    public NumberEntryController(NumberEntryModel<N> model, NumberEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.getTextField().setValidator(getModel().getTextPredicate().and(s -> getModel().validate(getModel().getToNumberFunction().apply(s))));
        view.getTextField().textProperty().addListener(value -> {
            if (view.getTextField().isValid()) {
                model.setValue(getModel().getToNumberFunction().apply(value));
                model.setValid(true);
            }
        });
        model.valueProperty().addListener(value -> view.getTextField().setText(model.getToStringFunction().apply(value)));
        view.getTextField().setText(model.getToStringFunction().apply(model.getValue()));
        view.getTextField().validProperty().addListener(model::setValid);
        view.getTextField().onKeyPress(event -> {
            if (event.isConsumed()) {
                return;
            }
            int keyCode = event.getKeyCode();
            if (keyCode != GLFW.GLFW_KEY_UP && keyCode != GLFW.GLFW_KEY_DOWN) {
                return;
            }
            String currentText = view.getTextField().getText();
            if (!model.getTextPredicate().test(currentText)) {
                return;
            }
            N currentValue;
            try {
                currentValue = model.getToNumberFunction().apply(currentText);
            } catch (Exception e) {
                return;
            }
            double step = model.getArrowStep();
            if (event.isShiftKeyDown()) {
                step = model.getArrowStepShift();
            } else if (event.isControlKeyDown()) {
                step = model.getArrowStepCtrl();
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                step = -step;
            }
            N newValue = model.offsetValue(currentValue, step);
            if (!model.validate(newValue) || Objects.equals(newValue, currentValue)) {
                return;
            }
            model.setValue(newValue);
            event.consume();
        });
    }
}
