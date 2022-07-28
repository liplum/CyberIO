package net.liplum.mdt.render;

public interface IFocusable {
    default void onFocused() {
    }

    default void onLostFocus() {
    }
}
