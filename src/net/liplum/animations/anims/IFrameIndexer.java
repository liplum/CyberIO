package net.liplum.animations.anims;


import org.jetbrains.annotations.Nullable;

public interface IFrameIndexer<T> {
    /**
     * Gets the index of the current frame.
     * @param length the length of all frames
     * @param data the data
     * @return the index.If it has no frame or don't want to show any image, return -1.
     */
    int getCurIndex(int length, @Nullable T data);
}
