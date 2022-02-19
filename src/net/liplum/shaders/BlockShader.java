package net.liplum.shaders;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;

import static mindustry.graphics.Shaders.getShaderFi;

public class BlockShader extends Shader {
    public BlockShader(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.Gen(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time / Scl.scl(1f));
        setUniformf("u_offset",
                Core.camera.position.x,
                Core.camera.position.y
        );
    }
}
