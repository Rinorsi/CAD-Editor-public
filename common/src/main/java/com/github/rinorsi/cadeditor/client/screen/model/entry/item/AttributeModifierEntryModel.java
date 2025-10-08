package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.StringProperty;
import com.github.rinorsi.cadeditor.client.screen.model.category.CategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class AttributeModifierEntryModel extends EntryModel {
    private final StringProperty attributeNameProperty;
    private final ObjectProperty<Slot> slotProperty;
    private final ObjectProperty<Operation> operationProperty;
    private final ObjectProperty<Double> amountProperty;
    private final UUID uuid;
    private final AttributeModifierAction action;
    private final String defaultAttributeName;
    private final Slot defaultSlot;
    private final Operation defaultOperation;
    private final double defaultAmount;

    public AttributeModifierEntryModel(CategoryModel category, AttributeModifierAction action) {
        this(category, "", Slot.MAINHAND, Operation.ADD, 0, UUID.randomUUID(), action);
    }

    public AttributeModifierEntryModel(CategoryModel category, String attributeName, String slot, int operation, double amount, UUID uuid, AttributeModifierAction action) {
        this(category, attributeName, Slot.from(slot), Operation.from(operation), amount, uuid, action);
    }

    public AttributeModifierEntryModel(CategoryModel category, String attributeName, Slot slot, Operation operation, double amount, UUID uuid, AttributeModifierAction action) {
        super(category);
        attributeNameProperty = StringProperty.create(attributeName);
        slotProperty = ObjectProperty.create(slot);
        operationProperty = ObjectProperty.create(operation);
        amountProperty = ObjectProperty.create(amount);
        this.uuid = uuid;
        this.action = action;
        defaultAttributeName = attributeName;
        defaultSlot = slot;
        defaultOperation = operation;
        defaultAmount = amount;
    }

    public String getAttributeName() {
        return attributeNameProperty().getValue();
    }

    public StringProperty attributeNameProperty() {
        return attributeNameProperty;
    }

    public void setAttributeName(String value) {
        attributeNameProperty().setValue(value);
    }

    public Slot getSlot() {
        return slotProperty().getValue();
    }

    public ObjectProperty<Slot> slotProperty() {
        return slotProperty;
    }

    public void setSlot(Slot value) {
        slotProperty().setValue(value);
    }

    public Operation getOperation() {
        return operationProperty().getValue();
    }

    public ObjectProperty<Operation> operationProperty() {
        return operationProperty;
    }

    public void setOperation(Operation value) {
        operationProperty().setValue(value);
    }

    public double getAmount() {
        return amountProperty().getValue();
    }

    public ObjectProperty<Double> amountProperty() {
        return amountProperty;
    }

    public void setAmount(Double value) {
        amountProperty().setValue(value);
    }

    @Override
    public void apply() {
        action.apply(attributeNameProperty.getValue(), slotProperty.getValue().getValue(), operationProperty.getValue().getValue(), amountProperty.getValue(), uuid);
    }

    @Override
    public void reset() {
        amountProperty().unbind();
        setAttributeName(defaultAttributeName);
        setSlot(defaultSlot);
        setOperation(defaultOperation);
        setAmount(defaultAmount);
    }

    @Override
    public Type getType() {
        return Type.ATTRIBUTE_MODIFIER;
    }

    @FunctionalInterface
    public interface AttributeModifierAction {
        void apply(String attributeName, String slot, int operation, double amount, UUID uuid);
    }

    public enum Slot {
        MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, ALL;

        private final MutableComponent text;

        Slot() {
            text = ModTexts.gui(getValue());
        }

        public static Slot from(String value) {
            for (Slot slot : values()) {
                if (Objects.equals(slot.getValue(), value)) {
                    return slot;
                }
            }
            return ALL;
        }

        public String getValue() {
            return name().toLowerCase(Locale.ROOT);
        }

        public MutableComponent getText() {
            return text;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public enum Operation {
        ADD, MULTIPLY_BASE, MULTIPLY;

        private final MutableComponent text, tooltip;

        Operation() {
            text = ModTexts.attributeModifierOperationText(getValue());
            tooltip = ModTexts.attributeModifierOperationTooltip(getValue());
        }

        public static Operation from(int value) {
            for (Operation operation : values()) {
                if (operation.getValue() == value) {
                    return operation;
                }
            }
            return null;
        }

        public int getValue() {
            return ordinal();
        }

        public MutableComponent getText() {
            return text;
        }

        public MutableComponent getTooltip() {
            return tooltip;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
