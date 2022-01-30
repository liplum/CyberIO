package net.liplum.animations;

import arc.graphics.Color;
import arc.util.Nullable;
import mindustry.gen.Building;

public interface IAnimated {
    void draw(float x, float y, @Nullable Building tileEntity);

    default void draw(float x, float y) {
        this.draw(x, y, null);
    }

    void draw(Color color, float x, float y, @Nullable Building tileEntity);

    default void draw(Color color, float x, float y) {
        this.draw(color, x, y, null);
    }

}
