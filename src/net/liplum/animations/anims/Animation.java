package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Animation implements IAnimated {
    @NotNull
    public final TextureRegion[] allFrames;
    @Nullable
    protected IFrameIndexer indexer;

    public boolean reversed = false;

    public Animation(@NotNull TextureRegion... allFrames) {
        this.allFrames = allFrames;
    }

    @NotNull
    public Animation indexer(@Nullable IFrameIndexer indexer) {
        this.indexer = indexer;
        return this;
    }

    @Nullable
    public IFrameIndexer getIndexer() {
        return indexer;
    }

    public int getCurIndex(int length) {
        if (indexer != null) {
            return indexer.getCurIndex(length);
        }
        return -1;
    }

    @Nullable
    public TextureRegion getCurTR(@Nullable IFrameIndexer indexer) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index;
        if (indexer != null) {
            index = indexer.getCurIndex(length);
        } else {
            index = this.getCurIndex(length);
        }
        if (index < 0) {
            return null;
        }
        if (reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    @Nullable
    public TextureRegion getCurTR() {
        return getCurTR(null);
    }

    @Override
    public void draw(float x, float y) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            Draw.rect(curTR, x, y);
        }
    }

    @Override
    public void draw(@NotNull Color color, float x, float y) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            Draw.color(color);
            Draw.rect(curTR, x, y);
            Draw.color();
        }
    }

    @Override
    public void draw(@NotNull IHowToRender howToRender) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            howToRender.render(curTR);
        }
    }

    public void draw(@NotNull IFrameIndexer indexer, @NotNull IHowToRender howToRender) {
        TextureRegion curTR = getCurTR(indexer);
        if (curTR != null) {
            howToRender.render(curTR);
        }
    }
}
