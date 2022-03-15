package net.liplum.utils;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;

public class G {
    public static float sin = 0f;
    public static float tan = 0f;

    public static float Ax() {
        return Draw.scl * Draw.xscl;
    }

    public static float Ay() {
        return Draw.scl * Draw.yscl;
    }

    public static float Dw(TextureRegion tr) {
        return D(tr.width);
    }

    public static float Dh(TextureRegion tr) {
        return D(tr.height);
    }

    public static float D(float a) {
        return a * Draw.scl * Draw.xscl;
    }

    public static void init() {
        sin = Mathf.absin(Time.time, 6f, 1f);
        tan = Mathf.tan(Time.time, 6f, 1f);
    }

    public static void drawDashLineBetweenTwoBlocks(Tile startTile, Tile endTile) {
        drawDashLineBetweenTwoBlocks(startTile, endTile, Pal.placing);
    }

    public static void drawDashLineBetweenTwoBlocks(Tile startTile, Tile endTile,
                                                    Color lineColor) {
        drawDashLineBetweenTwoBlocks(startTile, endTile, lineColor, Pal.gray);
    }

    public static void drawDashLineBetweenTwoBlocks(Tile startTile, Tile endTile,
                                                    Color lineColor, Color outlineColor) {
        drawDashLineBetweenTwoBlocks(
                startTile.block(), startTile.x, startTile.y,
                endTile.block(), endTile.x, endTile.y,
                lineColor, outlineColor);
    }

    public static void drawDashLineBetweenTwoBlocks(Block startBlock, short startBlockX, short startBlockY,
                                                    Block endBlock, short endBlockX, short endBlockY) {
        drawDashLineBetweenTwoBlocks(
                startBlock, startBlockX, startBlockY,
                endBlock, endBlockX, endBlockY,
                Pal.placing);
    }

    public static void drawDashLineBetweenTwoBlocks(Block startBlock, short startBlockX, short startBlockY,
                                                    Block endBlock, short endBlockX, short endBlockY,
                                                    Color lineColor) {
        drawDashLineBetweenTwoBlocks(
                startBlock, startBlockX, startBlockY,
                endBlock, endBlockX, endBlockY,
                lineColor, Pal.gray);
    }

    public static void drawDashLineBetweenTwoBlocks(Block startBlock, short startBlockX, short startBlockY,
                                                    Block endBlock, short endBlockX, short endBlockY,
                                                    Color lineColor, Color outlineColor) {
        float startDrawX = WorldU.toDrawXY(startBlock, startBlockX);
        float startDrawY = WorldU.toDrawXY(startBlock, startBlockY);
        float endDrawX = WorldU.toDrawXY(endBlock, endBlockX);
        float endDrawY = WorldU.toDrawXY(endBlock, endBlockY);


        float segsf = Util2D.distance(startDrawX, startDrawY, endBlockX * tilesize, endBlockY * tilesize) / tilesize;
        Tmp.v1.set(endDrawX, endDrawY)
                .sub(startDrawX, startDrawY)
                .limit((endBlock.size / 2f + 1) * tilesize + sin + 0.5f);
        float x2 = endBlockX * tilesize - Tmp.v1.x;
        float y2 = endBlockY * tilesize - Tmp.v1.y;
        float x1 = startDrawX + Tmp.v1.x;
        float y1 = startDrawY + Tmp.v1.y;
        int segs = (int) segsf;

        Lines.stroke(4f, outlineColor);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Lines.stroke(2f, lineColor);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Draw.reset();
    }

    public static void drawArrowBetweenTwoBlocks(Tile startTile, Tile pointTile) {
        drawArrowBetweenTwoBlocks(
                startTile.block(), startTile.x, startTile.y, pointTile.block(), pointTile.x, pointTile.y
        );
    }

    public static void drawArrowBetweenTwoBlocks(Tile startTile, Tile pointedTile,
                                                 Color arrowColor) {
        drawArrowBetweenTwoBlocks(
                startTile.block(), startTile.x, startTile.y, pointedTile.block(), pointedTile.x, pointedTile.y,
                arrowColor
        );
    }

    public static void drawArrowBetweenTwoBlocks(Block startBlock, short startBlockX, short startBlockY,
                                                 Block pointedBlock, short pointedBlockX, short pointedBlockY) {
        drawArrowBetweenTwoBlocks(
                startBlock, startBlockX, startBlockY, pointedBlock, pointedBlockX, pointedBlockY,
                Pal.accent);
    }

    public static void drawArrowBetweenTwoBlocks(Block startBlock, short startBlockX, short startBlockY,
                                                 Block pointedBlock, short pointedBlockX, short pointedBlockY,
                                                 Color arrowColor) {
        float startDrawX = WorldU.toDrawXY(pointedBlock, pointedBlockX);
        float startDrawY = WorldU.toDrawXY(pointedBlock, pointedBlockY);
        float pointedDrawX = WorldU.toDrawXY(startBlock, startBlockX);
        float pointedDrawY = WorldU.toDrawXY(startBlock, startBlockY);

        Drawf.arrow(pointedDrawX, pointedDrawY,
                startDrawX, startDrawY,
                startBlock.size * tilesize + sin,
                4f + sin,
                arrowColor);

    }

    public static void drawSurroundingCircle(Tile t, Color circleColor) {
        Drawf.circles(t.drawx(), t.drawy(),
                (t.block().size / 2f + 1) * Vars.tilesize + sin - 2f,
                circleColor
        );
    }

    public static void drawSurroundingCircle(Block b, float drawX, float drawY, Color circleColor) {
        Drawf.circles(drawX, drawY,
                (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
                circleColor
        );
    }

    public static void drawSurroundingCircle(Block b, int worldX, int worldY, Color circleColor) {
        float drawX = WorldU.toDrawXY(b, worldX);
        float drawY = WorldU.toDrawXY(b, worldY);
        Drawf.circles(drawX, drawY,
                (b.size / 2f + 1) * Vars.tilesize + sin - 2f,
                circleColor
        );
    }

    public static void drawDashCircle(Building building,
                                      float range, Color color) {
        Drawf.dashCircle(building.x, building.y, range + sin - 2, color);
    }

    public static void drawDashCircle(float x, float y,
                                      float range, Color color) {
        Drawf.dashCircle(x, y, range + sin - 2, color);
    }

    public static void drawDashCircle(Block b, short blockX, short BlockY,
                                      float range, Color color) {
        float drawX = WorldU.toDrawXY(b, blockX);
        float drawY = WorldU.toDrawXY(b, BlockY);
        Drawf.dashCircle(drawX, drawY, range + sin - 2, color);
    }

    public static void drawSelected(Building other, Color color) {
        drawSelected(other, color, Tmp.c1);
    }

    public static void drawSelected(Building other, Color color, Color temp) {
        Drawf.selected(
                other,
                temp.set(color).a(Mathf.absin(4f, 1f))
        );
    }
}
