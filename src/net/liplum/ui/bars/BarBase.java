package net.liplum.ui.bars;

import mindustry.ui.Bar;

public abstract class BarBase extends Bar {
    public abstract void draw();

    public abstract void reset(float value);
}
