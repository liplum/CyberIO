package net.liplum.animations.anims;

import arc.graphics.Color;

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
    void draw(float x, float y);

    /**
     * Draws current frame of this animation in certain color
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    void draw(Color color, float x, float y);
}
