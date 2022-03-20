package net.liplum.shaders.holo;

import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;
import net.liplum.shaders.IReusable;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class Hologram2 extends Shader implements IReusable {
    public float alpha = 1f;
    public float opacityNoise = 0.2f;
    public boolean blendHoloColor = true;
    public boolean blendFormerColor = true;
    public final float DefaultFlickering = 0.03f;
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
        setUniformi("u_blendHoloColorB", blendHoloColor ? 1 : 0);
        setUniformi("u_blendFormerColorB", blendFormerColor ? 1 : 0);

        renderer.effectBuffer.getTexture().bind(0);
    }

    @Override
    public void reset() {
        this.alpha = 1f;
        this.opacityNoise = 0.2f;
        this.flickering = DefaultFlickering;
        this.blendHoloColor = true;
        this.blendFormerColor = true;
    }
}
