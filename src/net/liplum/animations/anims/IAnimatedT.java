package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.util.Nullable;
import org.jetbrains.annotations.NotNull;

public interface IAnimatedT<T, OBJ> extends IAnimated<T> {
    /**
     * Draws current frame of this animation<br/>
     * If {@code data != null}, the animation may be played based on the {@code data}.
     *
     * @param obj  the instance
     * @param x    the central X for drawing of this
     * @param y    the central Y for drawing of this
     * @param data [Nullable] the building who has this
     */
    void draw(@NotNull OBJ obj, float x, float y, @Nullable T data);

    /**
     * Draws current frame of this animation in certain color<br/>
     * If {@code data != null}, the animation may be played based on the {@code data}.
     *
     * @param obj   the instance
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     * @param data  [Nullable] the object who has this
     */
    void draw(@NotNull OBJ obj, Color color, float x, float y, @Nullable T data);

    /**
     * Generates the object by this prototype
     *
     * @return the instance
     */
    OBJ gen();
}
