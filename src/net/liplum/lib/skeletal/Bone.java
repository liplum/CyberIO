package net.liplum.lib.skeletal;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import net.liplum.utils.MathH;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Bone {
    public Skeleton sk;
    public Skin skin;
    public int id = 0;
    public String name = "";
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
     * Unit: radian
     */
    public float minAngle = -Mathf.PI, maxAngle = Mathf.PI;
    /**
     * Use {@linkplain Bone#minAngle} and {@linkplain Bone#maxAngle} to limit the angle
     */
    public boolean limitAngle = false;
    /**
     * Whether this bone's angle is relative to previous bone
     */
    public boolean relative = false;
    /**
     * frictional coefficient.
     */
    public float u = 0.1f;
    @Nullable
    public Bone pre;
    @NotNull
    public List<Bone> next = new ArrayList<>();

    public Bone(@NotNull Skeleton sk) {
        this.sk = sk;
    }

    public Bone() {
    }

    public boolean hasNext() {
        return !next.isEmpty();
    }

    /**
     * Directly applied force without transfering force.
     *
     * @param F the force
     */
    public void applyForceDirectly(Vec2 F) {
        float f = MathH.normal(sk.v1.set(1f, 0f).setAngleRad(a)).dot(F);
        a += f / mass;
    }

    private void applyForceProximity(Vec2 F) {
        applyForceDirectly(F);
        if (sk.enableTransfer) {
            float fx = F.x;
            float fy = F.y;
            sk.v1.set(1f, 0f).setAngleRad(a).add(sk.v2.set(fx, fy)).scl(1f / (length * length));
            float nx = sk.v1.x;
            float ny = sk.v1.y;
            for (Bone next : next) {
                next.applyForceFromPre(sk.v2.set(nx, ny));
            }
            sk.v1.set(1f, 0f).setAngleRad(a).inv().add(sk.v2.set(fx, fy)).scl(1f / (length * length));
            float px = sk.v1.x;
            float py = sk.v1.y;
            if (pre != null) {
                pre.applyForceFromNext(sk.v2.set(px, py));
            }
        }
    }

    private void applyForceFromPre(Vec2 F) {
        applyForceDirectly(F);
        if (sk.enableTransfer) {
            sk.v1.set(1f, 0f).setAngleRad(a).add(F).scl(1f / (length * length));
            float x = sk.v1.x;
            float y = sk.v1.y;
            for (Bone next : next) {
                next.applyForceFromPre(sk.v2.set(x, y));
            }
        }
    }


    private void applyForceFromNext(Vec2 F) {
        applyForceDirectly(F);
        if (sk.enableTransfer) {
            sk.v1.set(1f, 0f).setAngleRad(a).inv().add(F).scl(1f / (length * length));
            float x = sk.v1.x;
            float y = sk.v1.y;
            if (pre != null) {
                pre.applyForceFromNext(sk.v2.set(x, y));
            }
        }
    }

    public void applyForce(Vec2 F) {
        applyForceDirectly(F);
        if (sk.enableTransfer) {
            float fx = F.x;
            float fy = F.y;
            sk.v1.set(1f, 0f).setAngleRad(a);
            float nx = sk.v1.x;
            float ny = sk.v1.y;
            for (Bone next : next) {
                next.applyForceDirectly(sk.v2.set(nx, ny));
            }
            sk.v1.set(1f, 0f).setAngleRad(a).inv();
            float px = sk.v1.x;
            float py = sk.v1.y;
            if (pre != null) {
                pre.applyForceDirectly(sk.v2.set(px, py));
            }
        }
    }

    public void getEndPos(Vec2 head, Vec2 end) {
        end.setZero().add(sk.v1.set(length, 0f).setAngleRad(getRealAngle())).add(head);
    }

    public void update(float delta) {
        w += a * delta;
        angle += w * delta;
        if (limitAngle) {
            angle = Mathf.clamp(angle, minAngle, maxAngle);
        }
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
        skin.drawRad(relative.x + pos.x, relative.y + pos.y, getRealAngle() + rotation);
    }

    public float getRealAngle() {
        if (relative && pre != null) {
            return angle + pre.getRealAngle();
        } else {
            return angle;
        }
    }

    public void addNext(@NotNull Bone bone) {
        next.add(bone);
        bone.pre = this;
        if (next.size() > 1) {
            sk.isLinear = false;
        }
    }

    public void addPre(@NotNull Bone bone) {
        pre = bone;
        bone.next.add(this);
        if (next.size() > 1) {
            sk.isLinear = false;
        }
    }

    @Override
    public String toString() {
        return "Name:" +
                name +
                ',' +
                "ID:" +
                id +
                ',' +
                "mass:" +
                mass +
                ',' +
                "length:" +
                length +
                ',' +
                "w:" +
                w +
                ',' +
                "a:" +
                a +
                ',' +
                "angle:" +
                angle +
                ',' +
                "u:" +
                u;
    }
}
