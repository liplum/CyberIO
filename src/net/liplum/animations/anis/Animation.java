package net.liplum.animations.anis;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import net.liplum.animations.anims.Frame;
import net.liplum.animations.anims.IAnimated;

public class Animation implements IAnimated {
    private final Frame[] allFrames;
    private boolean loop = true;
    private int curIndex = 0;

    public Animation(Frame... allFrames) {
        this.allFrames = allFrames;
    }

    public Frame[] getAllFrames() {
        return allFrames;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public TextureRegion getCurTR() {
        return allFrames[curIndex].Image;
    }

    @Override
    public void draw(float x, float y, Building tileEntity) {

    }

    public void draw(float x, float y) {
        Draw.rect(getCurTR(), x, y);
    }

    @Override
    public void draw(Color color, float x, float y, Building tileEntity) {

    }

    @Override
    public void draw(Color color, float x, float y) {
        Draw.color(color);
        Draw.rect(getCurTR(), x, y);
        Draw.color();
    }
}
