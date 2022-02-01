package net.liplum.utils;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;

public class GraphicUtl {
    public static void drawDashLineBetweenTwoBlocks(Tile startTile, Tile endTile) {
        drawDashLineBetweenTwoBlocks(startTile.block(), startTile.x, startTile.y, endTile.block(), endTile.x, endTile.y);
    }

    public static void drawDashLineBetweenTwoBlocks(Block startBlock, float startBlockX, float startBlockY,
                                                    Block endBlock, float endBlockX, float endBlockY) {
        float startDrawX = WorldUtil.toDrawXY(startBlock, startBlockX);
        float startDrawY = WorldUtil.toDrawXY(startBlock, startBlockY);
        float endDrawX = WorldUtil.toDrawXY(endBlock, endBlockX);
        float endDrawY = WorldUtil.toDrawXY(endBlock, endBlockY);

        float sin = Mathf.absin(Time.time, 6f, 1f);

        float segsf = Util2D.distance(startDrawX, startDrawY, endBlockX * tilesize, endBlockY * tilesize) / tilesize;
        Tmp.v1.set(endDrawX, endDrawY)
                .sub(startDrawX, startDrawY)
                .limit((endBlock.size / 2f + 1) * tilesize + sin + 0.5f);
        float x2 = endBlockX * tilesize - Tmp.v1.x;
        float y2 = endBlockY * tilesize - Tmp.v1.y;
        float x1 = startDrawX + Tmp.v1.x;
        float y1 = startDrawY + Tmp.v1.y;
        int segs = (int) segsf;

        Lines.stroke(4f, Pal.gray);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Lines.stroke(2f, Pal.placing);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Draw.reset();
    }
}
