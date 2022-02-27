package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Animation<T> implements IAnimated<T> {
    @NotNull
    protected final TextureRegion[] allFrames;
    @Nullable
    protected IFrameIndexer<T> indexer;

    public boolean reversed = false;

    public Animation(@NotNull TextureRegion... allFrames) {
        this.allFrames = allFrames;
    }

    @NotNull
    public TextureRegion[] getAllFrames() {
        return allFrames;
    }

    @NotNull
    public Animation<T> indexer(@Nullable IFrameIndexer<T> indexer) {
        this.indexer = indexer;
        return this;
    }

    @Nullable
    public IFrameIndexer<T> getIndexer() {
        return indexer;
    }

    @Nullable
    public TextureRegion getCurTR(@Nullable T data) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index = 0;
        if (indexer != null) {
            index = indexer.getCurIndex(length, data);
        }
        if(index < 0){
            return null;
        }
        if (reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    @Override
    public void draw(float x, float y, T data) {
        TextureRegion curTR = getCurTR(data);
        if (curTR != null) {
            Draw.rect(curTR, x, y);
        }
    }

    @Override
    public void draw(Color color, float x, float y, T data) {
        TextureRegion curTR = getCurTR(data);
        if (curTR != null) {
            Draw.color(color);
            Draw.rect(curTR, x, y);
            Draw.color();
        }
    }
}
