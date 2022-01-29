package net.liplum.animations;

import arc.graphics.g2d.TextureRegion;

public class Frame {
    public final TextureRegion Image;
    public final float Duration;

    public Frame(TextureRegion image, float duration) {
        Image = image;
        this.Duration = duration;
    }
}
