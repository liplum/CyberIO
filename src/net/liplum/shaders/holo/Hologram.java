package net.liplum.shaders.holo;

import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import net.liplum.R;
import net.liplum.shaders.ILoadResource;
import net.liplum.shaders.IReusable;
import net.liplum.utils.AtlasU;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;

public class Hologram extends Shader implements ILoadResource, IReusable {
    public Texture fringe;
    public float randomRange = 0f;
    public float alpha = 1f;
    public static final float DefaultSpeed = 0.5f;
    public float speed = DefaultSpeed;

    public Hologram(String fragName) {
        super(getShaderFi("default.vert"),
                Vars.tree.get(R.S.GenFrag(fragName)));
    }

    @Override
    public void apply() {
        setUniformf("u_time", Time.time + Mathf.range(randomRange));
        setUniformf("u_alpha", alpha);
        setUniformf("u_speed", speed);

        fringe.bind(1);
        renderer.effectBuffer.getTexture().bind(0);
        setUniformi("u_hologram", 1);
    }

    @Override
    public void loadResource() {
        fringe = AtlasU.inCio("hologram").texture;
        fringe.setFilter(Texture.TextureFilter.linear);
    }

    @Override
    public void reset() {
        this.alpha = 1f;
        this.randomRange = 0f;
        this.speed = DefaultSpeed;
    }
}
