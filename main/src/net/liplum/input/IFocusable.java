package net.liplum.input;

public interface IFocusable {
    default void onFocused() {
    }

    default void onLostFocus() {
    }
}
