package net.liplum.lib.skeletal;

import arc.math.Mathf;
import arc.math.geom.Vec2;

import java.util.Iterator;

import static java.lang.Math.abs;

public class Skeleton {
    /**
     * If a force's length is less than this, it will be ignored.
     */
    public float minForceAmount = 0.000001f;
    /**
     * If a velocity's length is less than this, it will be ignored.
     */
    public float minVelAmount = 0.000001f;

    /**
     * The root joint.
     */
    public Bone root;

    /**
     * If using a circular skeleton, it can prevent endless iteration.
     */
    public float maxUpdateDepth = Integer.MAX_VALUE;
    /**
     * For applying force.
     */
    public float k = 1f;
    /**
     * For friction.
     */
    public float u = 0.2f;
    public boolean isLinear = true;

    public Vec2 v1 = new Vec2();
    public Vec2 v2 = new Vec2();
    public Vec2 v3 = new Vec2();
    /**
     * Only for skeleton.
     */
    public Vec2 vx = new Vec2();
    /**
     * Only for skeleton.
     */
    public Vec2 vx2 = new Vec2();

    public void update(float delta) {
        if (isLinear)
            updateLinear(delta);
        else
            updateTree(delta);
    }

    /**
     * Only for linear skeleton.
     *
     * @param delta the time spent
     */
    public void updateLinear(float delta) {
        Bone cur = root;
        int i = 0;
        while (cur != null && i < maxUpdateDepth) {
            float f = abs(cur.u * cur.w);
            cur.a -= Mathf.sign(cur.w) * f / cur.mass * delta;
            cur.update(delta);
            cur = cur.next;
            i++;
        }
    }

    /**
     * Only for linear skeleton.
     */
    public void drawLinear(float relativeX, float relativeY, float rotation) {
        Bone cur = root;
        int i = 0;
        vx.set(relativeX, relativeY);
        while (cur != null && i < maxUpdateDepth) {
            cur.draw(vx, vx2, rotation);
            cur = cur.next;
            vx.set(vx2);
            i++;
        }
    }

    public Iterator<Bone> linearIt() {
        return new Iterator<Bone>() {
            Bone cur = root;
            int i = 0;

            @Override
            public boolean hasNext() {
                return cur.next != null && i < maxUpdateDepth;
            }

            @Override
            public Bone next() {
                Bone next = cur.next;
                cur = next;
                i++;
                return next;
            }
        };
    }

    /**
     * For tree and linear skeleton.
     *
     * @param delta the time spent
     */
    public void updateTree(float delta) {
    }
}
