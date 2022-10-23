package net.liplum.ui.bars;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.style.Drawable;
import arc.util.pooling.Pools;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

public class ReverseBar extends BarBase {
    public static Rect scissor = new Rect();

    public Floatp fraction;
    public String name = "";
    public float value;
    public float lastValue;
    public float blink;
    public Color blinkColor = new Color();

    public ReverseBar(String name, Color color, Floatp fraction) {
        this.fraction = fraction;
        this.name = Core.bundle.get(name, name);
        this.blinkColor.set(color);
        lastValue = value = fraction.get();
        setColor(color);
    }

    public ReverseBar(Prov<String> name, Prov<Color> color, Floatp fraction) {
        this.fraction = fraction;
        try {
            lastValue = value = Mathf.clamp(fraction.get());
        } catch (Exception e) { //getting the fraction may involve referring to invalid data
            lastValue = value = 0f;
        }
        update(() -> {
            try {
                this.name = name.get();
                this.blinkColor.set(color.get());
                setColor(color.get());
            } catch (Exception e) { //getting the fraction may involve referring to invalid data
                this.name = "";
            }
        });
    }

    public ReverseBar() {

    }

    public void reset(float value) {
        this.value = lastValue = blink = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color) {
        this.fraction = fraction;
        this.lastValue = fraction.get();
        this.blinkColor.set(color);
        setColor(color);
        update(() -> this.name = name.get());
    }

    public Bar blink(Color color) {
        blinkColor.set(color);
        return this;
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

        if (lastValue > computed) {
            blink = 1f;
            lastValue = computed;
        }

        if (Float.isNaN(lastValue)) lastValue = 0;
        if (Float.isInfinite(lastValue)) lastValue = 1f;
        if (Float.isNaN(value)) value = 0;
        if (Float.isInfinite(value)) value = 1f;
        if (Float.isNaN(computed)) computed = 0;
        if (Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        TextureRegion barTopTR = Core.atlas.find("bar-top");
        float leftMargin = width - topWidth;
        top.draw(x + leftMargin, y, topWidth, height);

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
