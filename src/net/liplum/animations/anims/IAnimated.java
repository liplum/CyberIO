package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.util.Nullable;
import mindustry.gen.Building;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of an individual drawable animation unit
 */
public interface IAnimated {
    /**
     * Draws current frame of this animation<br/>
     * If {@code tileEntity != null}, the animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param x          the central X for drawing of this
     * @param y          the central Y for drawing of this
     * @param tileEntity [Nullable] the building who has this
     */
    void draw(float x, float y, @Nullable Building tileEntity);

    /**
     * Draws current frame of this animation<br/>
     * The animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param tileEntity [NotNull] the building who has this
     */
    default void draw(@NotNull Building tileEntity) {
        draw(tileEntity.x, tileEntity.y, tileEntity);
    }

    /**
     * Draws current frame of this animation
     *
     * @param x the central X for drawing of this
     * @param y the central Y for drawing of this
     */
    default void draw(float x, float y) {
        this.draw(x, y, null);
    }

    /**
     * Draws current frame of this animation in certain color<br/>
     * If {@code tileEntity != null}, the animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param color      the color
     * @param x          the central X for drawing of this
     * @param y          the central Y for drawing of this
     * @param tileEntity [Nullable] the building who has this
     */
    void draw(Color color, float x, float y, @Nullable Building tileEntity);

    /**
     * Draws current frame of this animation in certain color<br/>
     * The animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param color      the color
     * @param tileEntity [Nullable] the building who has this
     */
    default void draw(Color color, @NotNull Building tileEntity) {
        draw(color, tileEntity.x, tileEntity.y, tileEntity);
    }

    /**
     * Draws current frame of this animation in certain color
     *
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    default void draw(Color color, float x, float y) {
        this.draw(color, x, y, null);
    }

}
