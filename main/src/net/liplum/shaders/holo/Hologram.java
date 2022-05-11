package net.liplum.shaders.holo;

import arc.files.Fi;
import arc.util.Time;
import net.liplum.lib.shaders.ShaderBase;

import static mindustry.Vars.renderer;

public class Hologram extends ShaderBase {
    public final float DefaultBlendHoloColorOpacity = 0.8f;
    public final float DefaultBlendFormerColorOpacity = 0.6f;
    public final float DefaultFlickering = 0.03f;
    public float alpha = 1f;
    public float opacityNoise = 0.2f;
    public float blendHoloColorOpacity = DefaultBlendHoloColorOpacity;
    public float blendFormerColorOpacity = DefaultBlendFormerColorOpacity;
    public float flickering = DefaultFlickering;

    public Hologram(Fi vert, Fi frag) {
        super(vert, frag);
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time / 60f);
        setUniformf("u_alpha", alpha);
        setUniformf("u_opacityNoise", opacityNoise);
        setUniformf("u_flickering", flickering);
        setUniformf("u_blendHoloColorOpacity", blendHoloColorOpacity);
        setUniformf("u_blendFormerColorOpacity", blendFormerColorOpacity);
        if (useEffectBuffer)
            renderer.effectBuffer.getTexture().bind(0);
    }

    @Override
    public void reset() {
        this.alpha = 1f;
        this.opacityNoise = 0.2f;
        this.flickering = DefaultFlickering;
        this.blendHoloColorOpacity = DefaultBlendHoloColorOpacity;
        this.blendFormerColorOpacity = DefaultBlendFormerColorOpacity;
    }
}
