package net.liplum.animations.anis;

public interface TransitionEffect {
    void draw(float progress, Runnable last, Runnable cur);

}
