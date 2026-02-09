package com.github.franckyi.guapi.base.theme.vanilla.delegate;

import com.github.franckyi.guapi.api.node.Slider;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("this-escape")
public class VanillaSliderSkinDelegate extends AbstractSliderButton implements VanillaWidgetSkinDelegate {
    private final Slider node;

    public VanillaSliderSkinDelegate(Slider node) {
        super(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Component.empty(), node.getValue());
        this.node = node;
        initNodeWidget(node);
        node.valueProperty().addListener(this::updateValue);
        node.labelFactoryProperty().addListener(this::updateMessage);
        updateValue();
        updateMessage();
    }

    private void updateValue() {
        double max = node.getMaxValue() - node.getMinValue();
        double value = node.getValue() - node.getMinValue();
        this.value = value / max;
        updateMessage();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        updateNodeFromMouse(event.x());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        updateNodeFromMouse(event.x());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (deltaX + deltaY > 0) {
            node.increment();
        } else if (deltaX + deltaY < 0) {
            node.decrement();
        }
        return false;
    }

    private void updateNodeFromMouse(double mouseX) {
        updateNode((mouseX - (getX() + 4)) / (width - 8));
    }

    private void updateNode(double newRawValue) {
        double range = node.getMaxValue() - node.getMinValue();
        double value = Mth.clamp(newRawValue, 0, 1) * range + node.getMinValue();
        double fixedValue = value - (value % node.getStep());
        node.setValue(fixedValue);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT) {
            node.increment();
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_LEFT) {
            node.decrement();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateMessage() {
        setMessage(node.getLabelFactory().apply(node.getValue()));
    }

    @Override
    protected void applyValue() {
    }
}
