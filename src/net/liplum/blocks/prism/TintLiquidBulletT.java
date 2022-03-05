package net.liplum.blocks.prism;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.entities.bullet.LiquidBulletType;
import mindustry.gen.Bullet;
import mindustry.type.Liquid;

public class TintLiquidBulletT extends LiquidBulletType {
    public Color tintColor;

    public TintLiquidBulletT(Liquid liquid) {
        super(liquid);
        tintColor = liquid.color;
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);
        Draw.color(tintColor, Color.white, b.fout() / 100f);
        Fill.circle(b.x, b.y, orbSize);
        Draw.reset();
    }
}
