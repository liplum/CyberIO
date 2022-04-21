package net.liplum.lib.entity.skeletal;

import arc.math.geom.Vec2;
import org.jetbrains.annotations.Nullable;

public class Joint {
    public Skeleton skeleton;
    public Skin skin;
    @Nullable
    public Bone nextBone;
    public Bone preBone;
    /**
     * Rotation doesn't affect the velocity , pos or force.
     */
    public float rotation;
    public Vec2 force = new Vec2();
    public Vec2 speed = new Vec2();
    public Vec2 pos = new Vec2();
    public void update(float delta){

    }
}
