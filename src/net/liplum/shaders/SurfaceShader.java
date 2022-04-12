package net.liplum.shaders;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class SurfaceShader extends Shader {
    Texture noise;
    public SurfaceShader(String fragName) {
        super(getShaderFi("screenspace.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
        loadNoise();
    }

    public SurfaceShader(String vertRaw, String fragRaw) {
        super(vertRaw, fragRaw);
    }

    public void loadNoise() {
        AssetDescriptor<Texture> ad = Core.assets.load("sprites/noise.png", Texture.class);
        ad.loaded = t -> {
            t.setFilter(Texture.TextureFilter.linear);
            t.setWrap(Texture.TextureWrap.repeat);
        };
    }

    @Override
    public void apply() {
        setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
        setUniformf("u_resolution", Core.camera.width, Core.camera.height);
        setUniformf("u_time", Time.time);

        if (hasUniform("u_noise")) {
            if(noise == null){
                noise = Core.assets.get("sprites/noise.png", Texture.class);
            }
            noise.bind(1);
            renderer.effectBuffer.getTexture().bind(0);

            setUniformi("u_noise", 1);
        }
    }
}
