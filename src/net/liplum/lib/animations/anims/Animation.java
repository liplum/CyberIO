package net.liplum.lib.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import net.liplum.lib.animations.anis.DrawT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Animation implements IAnimated {
    @NotNull
    public TextureRegion[] allFrames;
    @Nullable
    public IFrameIndexer indexer;

    public boolean reversed = false;

    public float duration;

    /**
     * @param duration  how long can this animation be played
     * @param allFrames every frame which has the same duration
     */
    public Animation(float duration, @NotNull TextureRegion... allFrames) {
        this.duration = Math.max(1f, duration);
        this.allFrames = allFrames;
    }

    @NotNull
    public TextureRegion get(int index) {
        return allFrames[index];
    }

    @NotNull
    public Animation indexer(@Nullable IFrameIndexer indexer) {
        this.indexer = indexer;
        return this;
    }


    /**
     * Gets the index which represents current frame.It will use internal indexer as default.
     *
     * @param length of all frames
     * @return index. If it shouldn't display any image, return -1.
     */
    public int getCurIndex(int length) {
        if (this.indexer != null) {
            return this.indexer.getCurIndex(length);
        }
        return -1;
    }

    @NotNull
    public AnimationObj gen() {
        return new AnimationObj(this);
    }

    /**
     * Gets current texture using {@link AnimationObj#getIndex(int)}.<br/>
     *
     * @return texture to be rendered
     */
    @Nullable
    public TextureRegion getCurTRByObj(@NotNull AnimationObj obj) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index = obj.getIndex(length);
        if (index < 0 || index >= length) {
            return null;
        }
        if (reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    /**
     * Gets current texture.<br/>
     * Indexer using order:
     * 1.internal {@link Animation#indexer} ->
     * 2.parameter {@code indexer} ->
     * 3.subclass {@link Animation#getCurIndex(int)}
     *
     * @param indexer if it's null, use internal indexer. Otherwise, use this.
     * @return texture to be rendered
     */
    @Nullable
    public TextureRegion getCurTR(@Nullable IFrameIndexer indexer) {
        int length = allFrames.length;
        if (length == 0) {
            return null;
        }
        int index;
        if (indexer != null) {
            index = indexer.getCurIndex(length);
        } else if (this.indexer != null) {
            index = this.indexer.getCurIndex(length);
        } else {
            index = this.getCurIndex(length);
        }
        if (index < 0 || index >= length) {
            return null;
        }
        if (reversed) {
            index = length - 1 - index;
        }
        return allFrames[index];
    }

    /**
     * Gets current texture by internal indexer.
     *
     * @return texture to be rendered
     */
    @Nullable
    public TextureRegion getCurTR() {
        return getCurTR(null);
    }


    @Override
    public void draw(float x, float y, float rotation) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            DrawT.Draw(curTR, x, y, rotation);
            DrawT.Reset();
        }
    }

    @Override
    public void draw(@NotNull Color color, float x, float y, float rotation) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            DrawT.SetColor(color);
            DrawT.Draw(curTR, x, y, rotation);
            DrawT.Reset();
        }
    }

    @Override
    public void draw(@NotNull IHowToRender howToRender) {
        TextureRegion curTR = getCurTR();
        if (curTR != null) {
            howToRender.render(curTR);
            DrawT.Reset();
        }
    }

    @Override
    public void draw(@NotNull IFrameIndexer indexer, @NotNull IHowToRender howToRender) {
        TextureRegion curTR = getCurTR(indexer);
        if (curTR != null) {
            howToRender.render(curTR);
            DrawT.Reset();
        }
    }

    public void draw(@NotNull AnimationObj obj, float x, float y, float rotation) {
        TextureRegion curTR = getCurTRByObj(obj);
        if (curTR != null) {
            DrawT.Draw(curTR, x, y, rotation);
            DrawT.Reset();
        }
    }

    public void draw(@NotNull AnimationObj obj, @NotNull Color color, float x, float y, float rotation) {
        TextureRegion curTR = getCurTRByObj(obj);
        if (curTR != null) {
            DrawT.SetColor(color);
            DrawT.Draw(curTR, x, y, rotation);
            DrawT.Reset();
        }
    }

    public void draw(@NotNull AnimationObj obj, @NotNull IHowToRender howToRender) {
        if (obj.meta == this) {
            TextureRegion curTR = getCurTRByObj(obj);
            if (curTR != null) {
                howToRender.render(curTR);
                DrawT.Reset();
            }
        }
    }
}
