package net.liplum.lib.skeletal;

import arc.math.Mathf;
import net.liplum.lib.Tx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Skin {
    @Nullable
    public Tx texture;
    public float dx, dy;

    public Skin(@NotNull Tx texture) {
        this.texture = texture;
    }

    public Skin() {
    }

    public void draw(float x, float y, float rotation) {
        if (texture != null)
            texture.draw(x + dx, y + dx, rotation);
    }

    public void drawRad(float x, float y, float radian) {
        if (texture != null)
            texture.draw(x + dx, y + dx, Mathf.radiansToDegrees * radian);
    }
}
