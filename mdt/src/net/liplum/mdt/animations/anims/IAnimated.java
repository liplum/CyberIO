package net.liplum.mdt.animations.anims;

import arc.graphics.Color;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of an individual drawable animation unit
 */
public interface IAnimated {
    /**
     * Draws current frame of this animation
     *
     * @param x the central X for drawing of this
     * @param y the central Y for drawing of this
     */
    default void draw(float x, float y) {
        draw(x, y, 0);
    }

    /**
     * Draws current frame of this animation
     *
     * @param x        the central X for drawing of this
     * @param y        the central Y for drawing of this
     * @param rotation the rotation
     */
    void draw(float x, float y, float rotation);

    /**
     * Draws current frame of this animation in certain color
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    void draw(@NotNull Color color, float x, float y, float rotation);

    /**
     * Draws current frame of this animation in certain color
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    default void draw(@NotNull Color color, float x, float y) {
        draw(color, x, y, 0);
    }

    /**
     * Draws current frame of this animation.
     *
     * @param howToRender customize your render behavior
     */
    void draw(@NotNull IHowToRender howToRender);

    /**
     * Draws current frame of this animation using a custom indexer.
     *
     * @param indexer     custom indexer
     * @param howToRender customize your render behavior
     */
    void draw(@NotNull IFrameIndexer indexer, @NotNull IHowToRender howToRender);
}
