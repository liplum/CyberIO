package net.liplum.animations.anims.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import net.liplum.animations.anims.IAnimatedT;
import net.liplum.animations.anims.IFrameIndexerT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoAnimationT<T extends Building> extends AutoAnimation implements IAnimatedT<AutoAnimationT<T>.Obj> {
    protected IFrameIndexerT<Obj> indexerT;
    public final float totalDuration;

    /**
     * @param totalDuration how long can this animation be played
     * @param allFrames     every frame which has the same duration
     */
    public AutoAnimationT(float totalDuration, TextureRegion... allFrames) {
        super(totalDuration, allFrames);
        this.totalDuration = Math.max(1, totalDuration);
        this.indexerT = (length, tObj) -> {
            if (length == 0 || tObj.tileEntity == null) {
                return -1;
            }
            float fixedTotalDuration = getFixedTotalDuration(tObj.tileEntity);
            float progress = Time.time % fixedTotalDuration / fixedTotalDuration;//percent
            int index = (int) (progress * length);
            index = Mathf.clamp(index, 0, length);
            return index;
        };
    }

    /**
     * Gets the real duration of this
     *
     * @param tileEntity if isn't a null, the result will base on {@link Building#timeScale()}
     * @return total duration how long this animation can be played
     */
    public float getFixedTotalDuration(@NotNull Building tileEntity) {
        return totalDuration / tileEntity.timeScale();
    }

    @Nullable
    public TextureRegion getCurTR(@NotNull Obj obj) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index = 0;
        if (indexerT != null) {
            index = indexerT.getCurIndex(length, obj);
        } else if (indexer != null) {
            index = getCurIndex(length);
        }
        if (index < 0) {
            return null;
        }
        if (obj.reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    @Override
    public void draw(@NotNull Obj obj, float x, float y) {
        TextureRegion curTR = getCurTR(obj);
        if (curTR != null) {
            Draw.rect(curTR, x, y);
        }
    }

    @Override
    public void draw(@NotNull Obj obj, Color color, float x, float y) {
        TextureRegion curTR = getCurTR(obj);
        if (curTR != null) {
            Draw.color(color);
            Draw.rect(curTR, x, y);
            Draw.color();
        }
    }

    @Override
    public AutoAnimationT<T>.Obj gen() {
        return new Obj();
    }

    public class Obj {
        public T tileEntity;
        public boolean reversed;
    }
}
