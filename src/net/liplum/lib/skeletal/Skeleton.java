package net.liplum.lib.skeletal;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;

import static java.lang.Math.abs;

public class Skeleton {
    /**
     * The root joint.
     */
    public Bone root;

    public int curID = 0;

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
    public boolean enableTransfer = true;

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
    public Vec2 returned = new Vec2();

    public void update(float delta) {
        if (isLinear)
            updateLinear(delta);
        else
            updateTree(delta);
    }

    public void draw(float relativeX, float relativeY, float rotation) {
        if (isLinear)
            drawLinear(relativeX, relativeY, rotation);
        else
            drawTree(relativeX, relativeY, rotation);
    }

    @Nullable
    public Bone findBoneIn(@NotNull Bone cur, @NotNull String name) {
        if (cur.name.equals(name))
            return cur;
        for (Bone nextBone : cur.next) {
            return findBoneIn(nextBone, name);
        }
        return null;
    }

    /**
     * Using recursive to find a bone.
     *
     * @param name the name of bone sought
     * @return the bone found of null
     */
    @Nullable
    public Bone findFirstByName(@NotNull String name) {
        return findBoneIn(root, name);
    }

    /**
     * Get a coordinate of the bone given relative to the root
     */
    @NotNull
    public Vec2 getRelativePos(@NotNull Bone bone) {
        Vec2 start = returned.setZero();
        Bone cur = bone;
        while (cur != null) {
            bone.getEndPos(start);
            cur = cur.pre;
        }
        return returned;
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
            cur = cur.next.isEmpty() ? null : cur.next.get(0);
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
            cur = cur.next.isEmpty() ? null : cur.next.get(0);
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
                return !cur.next.isEmpty() && i < maxUpdateDepth;
            }

            @Override
            public Bone next() {
                Bone next = cur.next.get(0);
                cur = next;
                i++;
                return next;
            }
        };
    }

    public static final class UFrame {
        /**
         * It has some branches.
         */
        public Bone bone;
        /**
         * The current index of {@link Bone#next}.
         */
        public int curIndex = -1;

        public UFrame(Bone bone) {
            this.bone = bone;
        }

    }

    public LinkedList<UFrame> stack = new LinkedList<>();

    public void updateRecursiveIn(Bone cur, float delta) {
        cur.update(delta);
        for (Bone next : cur.next)
            updateRecursiveIn(cur, delta);
    }

    public void updateRecursive(float delta) {
        updateRecursiveIn(root, delta);
    }

    private void update(Bone cur, float delta) {
        float f = abs(cur.u * cur.w);
        cur.a -= Mathf.sign(cur.w) * f / cur.mass * delta;
        cur.update(delta);
    }

    /**
     * For tree and linear skeleton.
     * It uses pre-it
     *
     * @param delta the time spent
     */
    public void updateTree(float delta) {
        // the stack is used to store branches.
        stack.clear();
        stack.add(new UFrame(root));
        while (!stack.isEmpty()) {
            UFrame curFrame = stack.peek();
            int curIndex = curFrame.curIndex;
            Bone cur = curFrame.bone;
            if (curFrame.curIndex < cur.next.size()) {
                update(cur, delta);

                int nextBoneNumber = cur.next.size();
                if (nextBoneNumber == 0) {
                    curFrame.curIndex++;
                } else if (nextBoneNumber == 1) {
                    // Don't need to create a new frame, just replace the bone.
                    curFrame.bone = cur.next.get(0);
                    curFrame.curIndex = -1;
                } else {
                    // current bone starts a new branch.
                    curFrame.curIndex++;
                    if (curFrame.curIndex < cur.next.size()) {
                        stack.push(new UFrame(cur.next.get(curFrame.curIndex)));
                    }
                }
            } else {
                // It means the iteration arrives at end of current branch.
                stack.pop();
            }
        }
    }

    public static final class RFrame {
        /**
         * It has some branches.
         */
        public Bone bone;
        /**
         * The current index of {@link Bone#next}.
         */
        public int curIndex = -1;

        /**
         * Relative x,y
         */
        public float rx, ry;

        public RFrame(Bone bone, float rx, float ry) {
            this.bone = bone;
            this.rx = rx;
            this.ry = ry;
        }
    }

    /**
     * Contract:
     * <p>
     * Input
     * <ul>
     *     <li>{@linkplain Skeleton#vx}</li>
     * </ul>
     * Output
     * <ul>
     *     <li>{@linkplain Skeleton#vx2}</li>
     * </ul>
     * </p>
     */
    private void draw(Bone cur, float rotation) {
        cur.draw(vx, vx2, rotation);
    }

    public LinkedList<RFrame> rstack = new LinkedList<>();

    /**
     * For tree and linear skeleton.
     * It uses pre-it
     */
    public void drawTree(float relativeX, float relativeY, float rotation) {
        // the stack is used to store branches.
        rstack.clear();
        rstack.add(new RFrame(root, relativeX, relativeY));
        while (!rstack.isEmpty()) {
            RFrame curFrame = rstack.peek();
            int curIndex = curFrame.curIndex;
            Bone cur = curFrame.bone;
            if (curFrame.curIndex < cur.next.size()) {
                //draw this
                vx.set(curFrame.rx, curFrame.ry);
                draw(cur, rotation);

                int nextBoneNumber = cur.next.size();
                if (nextBoneNumber == 0) {
                    curFrame.curIndex++;
                } else if (nextBoneNumber == 1) {
                    // Don't need to create a new frame, just replace the bone.
                    curFrame.bone = cur.next.get(0);
                    curFrame.curIndex = -1;
                    curFrame.rx = vx2.x;
                    curFrame.ry = vx2.y;
                } else {
                    // current bone starts a new branch.
                    curFrame.curIndex++;
                    if (curFrame.curIndex < cur.next.size()) {
                        rstack.push(new RFrame(cur.next.get(curFrame.curIndex), vx2.x, vx2.y));
                    }
                }
            } else {
                // It means the iteration arrives at end of current branch.
                rstack.pop();
            }
        }
    }
}
