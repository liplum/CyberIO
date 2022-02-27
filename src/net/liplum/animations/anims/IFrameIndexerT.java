package net.liplum.animations.anims;

import org.jetbrains.annotations.NotNull;

public interface IFrameIndexerT<T> {
    /**
     * Gets the index of the current frame.
     *
     * @param length the length of all frames
     * @param data   the instance
     * @return the index.If it has no frame or don't want to show any image, return -1.
     */
    int getCurIndex(int length, @NotNull T data);
}
