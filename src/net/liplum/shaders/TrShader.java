package net.liplum.shaders;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class TrShader extends Shader {
    public TrShader(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_offset",
                Core.camera.position.x,
                Core.camera.position.y
        );
        renderer.effectBuffer.getTexture().bind(0);
    }
}
