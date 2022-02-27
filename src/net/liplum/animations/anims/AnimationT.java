package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationT<T> extends Animation implements IAnimatedT<AnimationT.Obj<T>> {
    protected IFrameIndexerT<T> indexerT;

    public AnimationT(@NotNull TextureRegion... allFrames) {
        super(allFrames);
    }

    @Nullable
    public TextureRegion getCurTR(@NotNull T data, @NotNull Obj<T> obj) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index = 0;
        if (indexerT != null) {
            index = indexerT.getCurIndex(length, data);
        }
        if (index < 0) {
            return null;
        }
        if (obj.reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    @NotNull
    public AnimationT<T> indexerT(@Nullable IFrameIndexerT<T> indexerT) {
        this.indexerT = indexerT;
        return this;
    }

    @Override
    public void draw(@NotNull Obj<T> obj, float x, float y) {
        TextureRegion curTR = getCurTR(obj.data, obj);
        if (curTR != null) {
            Draw.rect(curTR, x, y);
        }
    }

    @Override
    public void draw(@NotNull Obj<T> obj, Color color, float x, float y) {
        TextureRegion curTR = getCurTR(obj.data, obj);
        if (curTR != null) {
            Draw.color(color);
            Draw.rect(curTR, x, y);
            Draw.color();
        }
    }

    @Override
    public Obj<T> gen() {
        return new Obj<>(this);
    }

    public static class Obj<T> {
        public AnimationT<T> prototype;
        public T data;
        public boolean reversed;

        public Obj(AnimationT<T> prototype) {
            this.prototype = prototype;
        }
    }
}
