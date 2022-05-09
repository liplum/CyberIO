package net.liplum.shaders.holo;

import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.util.Time;
import net.liplum.lib.shaders.ILoadResource;
import net.liplum.lib.shaders.IReusable;

import static mindustry.Vars.renderer;
import static mindustry.graphics.Shaders.getShaderFi;
import static net.liplum.utils.AtlasHKt.inCio;

public class HologramOld extends Shader implements ILoadResource, IReusable {
    public static final float DefaultSpeed = 0.5f;
    public Texture fringe;
    public float randomRange = 0f;
    public float alpha = 1f;
    public float speed = DefaultSpeed;

    public HologramOld(Fi frag) {
        super(getShaderFi("default.vert"), frag);
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
        fringe = inCio("hologram").texture;
        fringe.setFilter(Texture.TextureFilter.linear);
        fringe.setWrap(Texture.TextureWrap.repeat);
    }

    @Override
    public void reset() {
        this.alpha = 1f;
        this.randomRange = 0f;
        this.speed = DefaultSpeed;
    }
}
