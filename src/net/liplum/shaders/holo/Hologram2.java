package net.liplum.shaders.holo;

import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;
import net.liplum.lib.shaders.IReusable;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class Hologram2 extends Shader implements IReusable {
    public final float DefaultBlendHoloColorOpacity = 0.8f;
    public final float DefaultBlendFormerColorOpacity = 0.6f;
    public final float DefaultFlickering = 0.03f;
    public float alpha = 1f;
    public float opacityNoise = 0.2f;
    public float blendHoloColorOpacity = DefaultBlendHoloColorOpacity;
    public float blendFormerColorOpacity = DefaultBlendFormerColorOpacity;
    public float flickering = DefaultFlickering;

    public Hologram2(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time / 60f);
        setUniformf("u_alpha", alpha);
        setUniformf("u_opacityNoise", opacityNoise);
        setUniformf("u_flickering", flickering);
        setUniformf("u_blendHoloColorOpacity", blendHoloColorOpacity);
        setUniformf("u_blendFormerColorOpacity", blendFormerColorOpacity);

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
