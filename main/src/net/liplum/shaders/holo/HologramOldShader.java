package net.liplum.shaders.holo;

import arc.files.Fi;
import arc.graphics.Texture;
import arc.math.Mathf;
import arc.util.Time;
import net.liplum.lib.shaders.ShaderBase;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.renderer;
import static net.liplum.utils.AtlasHKt.inCio;

public class HologramOldShader extends ShaderBase {
    public static final float DefaultSpeed = 0.5f;
    public Texture fringe;
    public float randomRange = 0f;
    public float alpha = 1f;
    public float speed = DefaultSpeed;

    public HologramOldShader(@NotNull Fi vert, @NotNull Fi frag) {
        super(vert, frag);
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time + Mathf.range(randomRange));
        setUniformf("u_alpha", alpha);
        setUniformf("u_speed", speed);

        setUniformi("u_hologram", 1);
        if (useEffectBuffer)
            renderer.effectBuffer.getTexture().bind(0);
        fringe.bind(1);
    }

    @Override
    public void loadResource() {
        fringe = inCio("hologram").texture;
        fringe.setFilter(Texture.TextureFilter.linear);
        fringe.setWrap(Texture.TextureWrap.repeat);
    }

    @Override
    public void reset() {
        this.alpha = 1f;
        this.randomRange = 0f;
        this.speed = DefaultSpeed;
    }
}
