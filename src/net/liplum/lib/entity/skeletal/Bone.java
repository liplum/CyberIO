package net.liplum.lib.entity.skeletal;

public class Bone {
    public Skeleton skeleton;
    public Skin skin;
    /**
     * Always from a to b.
     */
    public Joint ja, jb;
    /**
     * Mustn't be zero. It's 1.0 as default.
     */
    public float mass = 1f;
    /**
     * The length can't be zero.
     */
    public float length = 1f;

    public float getRotation() {
        return jb.pos.angle(ja.pos);
    }
}
