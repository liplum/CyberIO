package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.util.Nullable;
import mindustry.gen.Building;
import org.jetbrains.annotations.NotNull;

public interface IAnimatedBlockT<OBJ> extends IAnimatedBlock, IAnimatedT<Building, OBJ> {
    /**
     * Draws current frame of this animation<br/>
     * If {@code tileEntity != null}, the animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param obj        the instance
     * @param x          the central X for drawing of this
     * @param y          the central Y for drawing of this
     * @param tileEntity [Nullable] the building who has this
     */
    void draw(@NotNull OBJ obj, float x, float y, @Nullable Building tileEntity);

    /**
     * Draws current frame of this animation<br/>
     * The animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param obj        the instance
     * @param tileEntity [NotNull] the building who has this
     */
    default void draw(@NotNull OBJ obj, @NotNull Building tileEntity) {
        draw(obj, tileEntity.x, tileEntity.y, tileEntity);
    }

    /**
     * Draws current frame of this animation
     *
     * @param obj the instance
     * @param x   the central X for drawing of this
     * @param y   the central Y for drawing of this
     */
    default void draw(@NotNull OBJ obj, float x, float y) {
        this.draw(obj, x, y, null);
    }

    /**
     * Draws current frame of this animation in certain color<br/>
     * The animation may be played based on the {@link Building#timeScale()} of it.
     *
     * @param obj        the instance
     * @param color      the color
     * @param tileEntity [Nullable] the building who has this
     */
    default void draw(@NotNull OBJ obj, Color color, @NotNull Building tileEntity) {
        draw(obj, color, tileEntity.x, tileEntity.y, tileEntity);
    }

    /**
     * Draws current frame of this animation in certain color
     *
     * @param obj   the instance
     * @param color the color
     * @param x     the central X for drawing of this
     * @param y     the central Y for drawing of this
     */
    default void draw(@NotNull OBJ obj, Color color, float x, float y) {
        this.draw(obj, color, x, y, null);
    }
}
