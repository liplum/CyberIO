package net.liplum.lib.skeletal;

import arc.math.Mathf;
import net.liplum.lib.Tx;
import org.jetbrains.annotations.NotNull;

public class Skin {
    public Tx texture;

    public Skin(@NotNull Tx texture) {
        this.texture = texture;
    }

    public Skin() {
    }

    public void draw(float x, float y, float rotation) {
        texture.draw(x, y, rotation);
    }
    public void drawRad(float x, float y, float radian) {
        texture.draw(x, y, Mathf.radiansToDegrees * radian);
    }
}
