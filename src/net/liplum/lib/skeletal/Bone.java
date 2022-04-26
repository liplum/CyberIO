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
        this.id = sk.curID++;
    }

    public Bone() {
    }

    public boolean hasNext() {
        return !next.isEmpty();
    }

    /**
     * Directly applied force without transferring force.
     *
     * @param F the force
     */
    public void applyForceDirectly(@Invariant @NotNull Vec2 F) {
        float f = MathH.normal(sk.v1.set(1f, 0f).setAngleRad(a)).inv().dot(F);
        a += f / mass;
    }

    public void applyAngularForceDirectly(float F) {
        a += F / mass;
    }

    public void applyForce(@Invariant @NotNull Vec2 F) {
        applyForceDirectly(F);
        if (sk.enableTransfer) {
            float fx = F.x;
            float fy = F.y;
            sk.v1.set(1f, 0f).setAngleRad(a).scl(1f / length);
            float nx = sk.v1.x;
            float ny = sk.v1.y;
            for (Bone next : next) {
                next.applyForceDirectly(sk.v2.set(nx, ny));
            }
            sk.v1.set(1f, 0f).setAngleRad(a).scl(1f / length).inv();
            float px = sk.v1.x;
            float py = sk.v1.y;
            if (pre != null) {
                pre.applyForceDirectly(sk.v2.set(px, py));
            }
        }
    }

    public void applyAngularForce(float F) {
        applyAngularForceDirectly(F);
        if (sk.enableTransfer) {
            for (Bone next : next) {
                next.applyAngularForceDirectly(F);
            }
            if (pre != null) {
                pre.applyAngularForceDirectly(F);
            }
        }
    }

    /**
     * @param head the based coordinate
     * @param end  the result
     */
    @UseVec("v1")
    public void getEndPos(@Invariant @NotNull Vec2 head, @Changed @NotNull Vec2 end) {
        end.setZero().add(sk.v1.set(length, 0f).setAngleRad(getRealAngle())).add(head);
    }

    /**
     * Get the coordinate based on {@code root}
     */
    @UseVec({"v1",})
    public void getEndPos(@Changed @NotNull Vec2 root) {
        root.add(
                sk.v1.set(length, 0f).setAngleRad(getRealAngle())
        );
    }

    public void update(float delta) {
        w += a * delta;
        angle += w * delta;
        if (limitAngle) {
            angle = Mathf.clamp(angle, minAngle, maxAngle);
        }
        a = 0f;
    }

    public void draw(
            @Invariant @NotNull Vec2 relative,
            @Changed @NotNull Vec2 endPos
    ) {
        this.draw(relative, endPos, 0f);
    }

    /**
     * @param relative the xy coordinate relative to parent bone.
     * @param rotation relative rotation (Unit: radian)
     */
    @UseVec({"v1", "v2",})
    public void draw(
            @Invariant @NotNull Vec2 relative,
            @Changed @NotNull Vec2 endPos,
            float rotation
    ) {
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
