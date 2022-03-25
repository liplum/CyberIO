package net.liplum.shaders.colorize;

import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;
import net.liplum.lib.shaders.IReusable;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class InvertingColor extends Shader implements IReusable {
    public float progress = 1f;

    public InvertingColor(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_progress", progress);

        renderer.effectBuffer.getTexture().bind(0);
    }

    @Override
    public void reset() {
        this.progress = 1f;
    }
}
