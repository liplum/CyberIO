package net.liplum.lib.shaders;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class ScanlineShader extends Shader implements IReusable {
    public float width = 0f;
    public float height = 0f;

    public ScanlineShader(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_size", width, height);
        setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
        setUniformf("u_offset",
                Core.camera.position.x,
                Core.camera.position.y
        );
        renderer.effectBuffer.getTexture().bind(0);
    }

    @Override
    public void reset() {
        this.width = 0f;
        this.height = 0f;
    }
}