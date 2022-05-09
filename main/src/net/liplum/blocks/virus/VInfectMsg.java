package net.liplum.blocks.virus;

import arc.graphics.Color;
import arc.util.Nullable;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.SetTileCallPacket;
import mindustry.world.Tile;

public class VInfectMsg extends SetTileCallPacket {
    public int curGeneration;
    @Nullable
    public Color raceColor;

    @Override
    public void write(Writes WRITE) {
        super.write(WRITE);
        WRITE.b(curGeneration);
        WRITE.i(raceColor != null ? raceColor.rgba8888() : -1);
    }

    @Override
    public void handled() {
        super.handled();
        this.curGeneration = READ.b();
        int raceColorInt = READ.i();
        if (raceColorInt < 0) {
            raceColor = null;
        } else {
            raceColor = new Color(raceColorInt);
        }
    }

    @Override
    public void handleClient() {
        Tile.setTile(this.tile, this.block, this.team, this.rotation);
        Building build = tile.build;
        if (build instanceof Virus.VirusBuild) {
            Virus.VirusBuild vb = (Virus.VirusBuild) build;
            vb.setCurGeneration(curGeneration);
            vb.setRaceColor(raceColor);
        }
    }
}
