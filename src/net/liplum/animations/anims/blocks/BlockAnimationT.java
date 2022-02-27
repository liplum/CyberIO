package net.liplum.animations.anims.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.IAnimatedT;
import net.liplum.animations.anims.IFrameIndexerT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockAnimationT<T extends Building> extends Animation implements IAnimatedT<BlockAnimationT<T>.Obj> {
    protected IFrameIndexerT<Obj> indexerT;

    /**
     * @param allFrames every frame which has the same duration
     */
    public BlockAnimationT(TextureRegion... allFrames) {
        super(allFrames);
    }

    @NotNull
    public BlockAnimationT<T> indexerT(@Nullable IFrameIndexerT<Obj> indexerT) {
        this.indexerT = indexerT;
        return this;
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
    public Obj gen() {
        return new Obj();
    }

    public class Obj {
        public T tileEntity;
        public boolean reversed;
    }
}
