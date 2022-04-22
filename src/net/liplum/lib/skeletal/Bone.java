package net.liplum.lib.skeletal;

import arc.math.geom.Vec2;
import net.liplum.utils.MathH;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Bone {
    public Skeleton sk;
    public Skin skin;
    /**
     * Mustn't be zero. It's 1.0 as default.
     */
    public float mass = 1f;
    /**
     * The length can't be zero.
     */
    public float length = 1f;
    /**
     * The angular velocity.
     */
    public float w;
    /**
     * The angular acceleration.
     */
    public float a;
    /**
     * Unit: radian
     */
    public float angle;
    /**
     * frictional coefficient.
     */
    public float u = 0.1f;

    public List<Bone> pre = new ArrayList<>();
    public List<Bone> next = new ArrayList<>();

    public Bone(@NotNull Skeleton sk) {
        this.sk = sk;
    }

    public Bone() {
    }

    public void applyForce(Vec2 F) {
        float f = MathH.normal(sk.v1.set(1f, 0f).setAngleRad(a)).dot(F);
        a += f / mass;
    }

    public void getEndPos(Vec2 head, Vec2 end) {
        end.setZero().add(sk.v1.set(length, 0f).setAngleRad(angle)).add(head);
    }

    public void update(float delta) {
        w += a * delta;
        angle += w * delta;
        a = 0f;
    }

    public void draw(Vec2 relative, Vec2 endPos) {
        this.draw(relative, endPos, 0f);
    }

    /**
     * @param relative the xy coordinate relative to parent bone.
     * @param rotation relative rotation (Unit: radian)
     */
    public void draw(Vec2 relative, Vec2 endPos, float rotation) {
        getEndPos(relative, endPos);
        Vec2 pos = sk.v2.set(endPos).minus(relative).scl(0.5f);
        skin.drawRad(relative.x + pos.x, relative.y + pos.y, angle + rotation);
    }

    public void addNext(@NotNull Bone bone) {
        next.add(bone);
        bone.pre.add(this);
    }

    public void addPre(@NotNull Bone bone) {
        pre.add(bone);
        bone.next.add(this);
    }
}
