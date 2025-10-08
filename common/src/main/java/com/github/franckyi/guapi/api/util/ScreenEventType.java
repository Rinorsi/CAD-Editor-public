package com.github.franckyi.guapi.api.util;

import com.github.franckyi.guapi.api.EventTarget;
import com.github.franckyi.guapi.api.event.*;

import java.util.Objects;
import java.util.function.BiConsumer;

public record ScreenEventType<E extends ScreenEvent>(String name, boolean mouseEvent,
                                                     BiConsumer<EventTarget, E> onEvent) {
    public static final ScreenEventType<MouseButtonEvent> MOUSE_CLICKED = new ScreenEventType<>("MOUSE_CLICKED", true, EventTarget::mouseClicked);
    public static final ScreenEventType<MouseButtonEvent> MOUSE_RELEASED = new ScreenEventType<>("MOUSE_RELEASED", true, EventTarget::mouseReleased);
    public static final ScreenEventType<MouseDragEvent> MOUSE_DRAGGED = new ScreenEventType<>("MOUSE_DRAGGED", false, EventTarget::mouseDragged);
    public static final ScreenEventType<MouseScrollEvent> MOUSE_SCOLLED = new ScreenEventType<>("MOUSE_SCOLLED", true, EventTarget::mouseScrolled);
    public static final ScreenEventType<KeyEvent> KEY_PRESSED = new ScreenEventType<>("KEY_PRESSED", false, EventTarget::keyPressed);
    public static final ScreenEventType<KeyEvent> KEY_RELEASED = new ScreenEventType<>("KEY_RELEASED", false, EventTarget::keyReleased);
    public static final ScreenEventType<TypeEvent> CHAR_TYPED = new ScreenEventType<>("CHAR_TYPED", false, EventTarget::charTyped);
    public static final ScreenEventType<MouseEvent> MOUSE_MOVED = new ScreenEventType<>("MOUSE_MOVED", true, EventTarget::mouseMoved);
    public static final ScreenEventType<MouseButtonEvent> ACTION = new ScreenEventType<>("ACTION", false, EventTarget::action);

    public String getName() {
        return name;
    }

    public boolean isMouseEvent() {
        return mouseEvent;
    }

    @SuppressWarnings("unchecked")
    public <EE extends MouseEvent> void ifMouseEvent(E event, BiConsumer<ScreenEventType<EE>, EE> thenDo, Runnable elseDo) {
        if (mouseEvent) {
            thenDo.accept((ScreenEventType<EE>) this, (EE) event);
        } else {
            elseDo.run();
        }
    }

    public void onEvent(EventTarget node, E event) {
        onEvent.accept(node, event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenEventType<?> that = (ScreenEventType<?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
