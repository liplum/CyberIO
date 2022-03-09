package net.liplum.animations.anims;


public interface IFrameIndexerObj {
    /**
     * Gets the index of the current frame.
     *
     * @param obj    who has this indexer
     * @param length the length of all frames which is more than 0.
     * @return the index.If it has no frame or don't want to show any image, return -1.
     */
    int getCurIndex(AnimationObj obj, int length);
}
