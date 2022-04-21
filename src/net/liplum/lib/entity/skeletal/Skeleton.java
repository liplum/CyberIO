package net.liplum.lib.entity.skeletal;

public class Skeleton {
    /**
     * To prevent an endless force spreading.
     * -1 means no limit.
     */
    public int maxSpreadJoint = 5;
    /**
     * If a force's length is less than this, it will disappear.
     */
    public float minForceAmount = 0.01f;

    /**
     * The root joint.
     */
    public Joint root;

    /**
     * If using a circular skeleton, it can prevent endless iteration.
     */
    public float maxUpdate = Integer.MAX_VALUE;

    public void update(float delta) {
        Joint cur = root;
        while (cur != null){
        }
    }
}
