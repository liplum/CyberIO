package net.liplum.blocks.prism;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Tmp;
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
        if (liquid.willBoil()) {
            Draw.color(tintColor, Tmp.c3.set(liquid.gasColor).a(0.4f), b.time / Mathf.randomSeed(b.id, boilTime));
            Fill.circle(b.x, b.y, orbSize * (b.fin() * 1.1f + 1f));
        } else {
            Draw.color(tintColor, Color.white, b.fout() / 100f);
            Fill.circle(b.x, b.y, orbSize);
        }
        Draw.reset();
    }
}
