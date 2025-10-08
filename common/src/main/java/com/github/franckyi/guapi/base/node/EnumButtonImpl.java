package com.github.franckyi.guapi.base.node;

import com.github.franckyi.guapi.api.node.EnumButton;
import com.github.franckyi.guapi.api.node.builder.EnumButtonBuilder;

import java.util.Collection;

public class EnumButtonImpl<E> extends AbstractEnumButton<E> implements EnumButtonBuilder<E> {
    public EnumButtonImpl() {
    }

    public EnumButtonImpl(E[] values) {
        super(values);
    }

    public EnumButtonImpl(Collection<? extends E> values) {
        super(values);
    }

    public EnumButtonImpl(E[] values, E value) {
        super(values, value);
    }

    public EnumButtonImpl(Collection<? extends E> values, E value) {
        super(values, value);
    }

    public static <EE extends Enum<EE>> EnumButtonImpl<EE> fromEnum(EE value) {
        return new EnumButtonImpl<>(value.getDeclaringClass().getEnumConstants(), value);
    }

    @Override
    protected Class<?> getType() {
        return EnumButton.class;
    }
}
