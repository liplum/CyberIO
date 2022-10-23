package net.liplum.ui.bars;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.style.Drawable;
import arc.util.pooling.Pools;
import mindustry.gen.Tex;
import mindustry.ui.Fonts;

public class ScrollBar extends BarBase {
    public static Rect scissor = new Rect();

    public Floatp fraction;
    public String name = "";
    public float progress;

    public ScrollBar(String name, Color color, Floatp fraction) {
        this.fraction = fraction;
        this.name = Core.bundle.get(name, name);
        progress = fraction.get();
        setColor(color);
    }

    public ScrollBar(Prov<String> name, Prov<Color> color, Floatp fraction) {
        this.fraction = fraction;
        try {
            progress = Mathf.clamp(fraction.get());
        } catch (Exception e) { //getting the fraction may involve referring to invalid data
            progress = 0f;
        }
        update(() -> {
            try {
                this.name = name.get();
                setColor(color.get());
            } catch (Exception e) { //getting the fraction may involve referring to invalid data
                this.name = "";
            }
        });
    }

    public ScrollBar() {

    }

    public void reset(float value) {
        this.progress = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color) {
        this.fraction = fraction;
        setColor(color);
        update(() -> this.name = name.get());
    }

    @Override
    public void draw() {
        if (fraction == null)
            return;

        float computed;
        try {
            computed = Mathf.clamp(fraction.get());
        } catch (Exception e) { //getting the fraction may involve referring to invalid data
            computed = 0f;
        }

        if (Float.isNaN(progress)) progress = 0;
        if (Float.isInfinite(progress)) progress = 1f;
        if (Float.isNaN(computed)) computed = 0;
        if (Float.isInfinite(computed)) computed = 1f;

        progress = Mathf.lerpDelta(progress, computed, 0.15f);

        Drawable bar = Tex.bar;

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);

        Drawable top = Tex.barTop;
        float topWidth = width * progress;

        TextureRegion barTopTR = Core.atlas.find("bar-top");
        if (topWidth > barTopTR.width) {
            float leftMargin = width - topWidth;
            top.draw(x + leftMargin, y, topWidth, height);
        } else {
            if (ScissorStack.push(scissor.set(x, y, topWidth, height))) {
                top.draw(x, y, barTopTR.width, height);
                ScissorStack.pop();
            }
        }

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);
        font.setColor(Color.white);
        font.draw(name,
            x + width / 2f - lay.width / 2f,
            y + height / 2f + lay.height / 2f + 1
        );

        Pools.free(lay);
    }
}
