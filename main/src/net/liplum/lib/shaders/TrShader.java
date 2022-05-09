package net.liplum.lib.shaders;

import arc.Core;
import arc.files.Fi;
import arc.graphics.gl.Shader;
import arc.util.Time;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class TrShader extends Shader {
    public TrShader(Fi frag) {
        super(getShaderFi("default.vert"), frag);
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time);
        setUniformf("u_resolution",
                Core.graphics.getWidth(),
                Core.graphics.getHeight()
        );
        setUniformf("u_offset",
                Core.camera.position.x,
                Core.camera.position.y
        );
        renderer.effectBuffer.getTexture().bind(0);
    }
}
