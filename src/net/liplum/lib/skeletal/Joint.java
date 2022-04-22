package net.liplum.lib.skeletal;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static arc.math.Mathf.sqrt;

public class Joint {
    public Skeleton sk;
    public Skin skin;
    /**
     * <ul>
     * <li>This is {@link Bone#jb} in preBone</li>
     * <li>This is {@link Bone#ja} in nextBone</li>
     * </ul>
     */
    @Nullable
    public Bone preBone, nextBone;
    /**
     * Rotation doesn't affect the velocity , pos or force.
     */
    public float rotation;
    public Vec2 force = new Vec2();
    public Vec2 vel = new Vec2();
    public Vec2 pos = new Vec2();

    public Joint(@NotNull Skeleton sk) {
        this.sk = sk;
    }

    public Joint() {
    }

    @Nullable
    public Joint getNext() {
        // Because this is the ja of nextBone.
        return nextBone != null ? nextBone.jb : null;
    }

    public boolean hasNext() {
        return nextBone != null;
    }

    @Nullable
    public Joint getPre() {
        // Because this is the jb of preBone.
        return preBone != null ? preBone.ja : null;
    }

    public boolean hasPre() {
        return preBone != null;
    }

    public void applyForce(Vec2 f) {
        force.add(f);
    }

    public void applyForce(float x, float y) {
        force.add(x, y);
    }

    public void update(float delta) {
        if (!force.isZero(sk.minForceAmount)) {
            vel.add(force.scl(delta));
        }
        force.setZero();
        if (!vel.isZero(sk.minVelAmount)) {
            pos.add(Tmp.v1.set(vel).scl(delta));
        }
        if (preBone != null) {
            float stretch = preBone.getStretch();
            float sign;
            float d;
            if (stretch >= 0) {
                sign = 1;
                d = stretch;
            } else {
                sign = -1;
                d = -stretch;
            }
            if (!Mathf.zero(d, sk.minDstAmount)) {
                // from ja to jb
                preBone.getDirectionA2B(Tmp.v2);
                // apply distancing force on the ja of preBone.
                preBone.ja.applyForce(Tmp.v2.scl(sign * sk.k * sqrt(d) / preBone.mass));
            }
        }
        if (nextBone != null) {
            float stretch = nextBone.getStretch();
            float sign;
            float d;
            if (stretch >= 0) {
                sign = 1;
                d = stretch;
            } else {
                sign = -1;
                d = -stretch;
            }
            if (!Mathf.zero(d, sk.minDstAmount)) {
                // from jb to ja
                nextBone.getDirectionB2A(Tmp.v2);
                // apply distancing force on the jb of nextBone.
                nextBone.jb.applyForce(Tmp.v2.scl(sign * sk.k * sqrt(d) / nextBone.mass));
            }
        }
    }

    public void draw(float relativeX, float relativeY) {
        this.draw(relativeX, relativeY, 0f);
    }

    public void draw(float relativeX, float relativeY, float rotation) {
        if (skin != null) {
            skin.draw(relativeX + pos.x, relativeY + pos.y, rotation + this.rotation);
        }
    }

    public void setNextBone(Bone bone) {
        bone.sk = sk;
        nextBone = bone;
        bone.ja = this;
    }

    public void setPreBone(Bone bone) {
        bone.sk = sk;
        preBone = bone;
        bone.jb = this;
    }
}
