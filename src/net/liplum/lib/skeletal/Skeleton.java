package net.liplum.lib.skeletal;

import arc.graphics.g2d.Draw;
import arc.util.Tmp;

import java.util.Iterator;

import static java.lang.Math.min;

public class Skeleton {
    /**
     * If a force's length is less than this, it will be ignored.
     */
    public float minForceAmount = 0.001f;
    /**
     * If a velocity's length is less than this, it will be ignored.
     */
    public float minVelAmount = 0.001f;
    /**
     * If a distance is less than this, it will be ignored.
     */
    public float minDstAmount = 0.001f;

    /**
     * The root joint.
     */
    public Joint root;

    /**
     * If using a circular skeleton, it can prevent endless iteration.
     */
    public float maxUpdateDepth = Integer.MAX_VALUE;
    /**
     * The friction is always the opposite of velocity.
     * So only its value is needed.
     */
    public float friction = 1f;
    /**
     * For attenuating/pulling force.
     */
    public float k = 1f;
    /**
     * For friction.
     */
    public float u = 0.2f;
    public boolean isLinear = true;

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
        Joint cur = root;
        int i = 0;
        while (cur != null && i < maxUpdateDepth) {
            if (!cur.vel.isZero(minVelAmount)) {
                float f = min(friction * cur.vel.len(), friction);
                cur.applyForce(Tmp.v1.set(cur.vel).nor().scl(-f));
            }
            cur.update(delta);
            cur = cur.getNext();
            i++;
        }
    }

    /**
     * Only for linear skeleton.
     */
    public void drawLinear(float relativeX, float relativeY, float rotation) {
        Joint cur = root;
        int i = 0;
        float z = Draw.z();
        while (cur != null && i < maxUpdateDepth) {
            Draw.z(z + 1f);
            cur.draw(relativeX, relativeY, rotation);
            Draw.z(z);
            Bone nextBone = cur.nextBone;
            if (nextBone == null) {
                cur = null;
            } else {
                nextBone.draw(relativeX, relativeY, rotation);
                cur = nextBone.jb;
            }
            i++;
        }
    }

    public Iterator<Joint> linearIt() {
        return new Iterator<Joint>() {
            Joint cur = root;
            int i = 0;

            @Override
            public boolean hasNext() {
                return cur.hasNext() && i < maxUpdateDepth;
            }

            @Override
            public Joint next() {
                Joint next = cur.getNext();
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
