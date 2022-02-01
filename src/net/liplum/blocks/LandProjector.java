package net.liplum.blocks;

import arc.util.Time;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.liplum.blocks.floors.HoloFloor;
import net.liplum.registries.CioBlocks;

import static mindustry.Vars.world;

public class LandProjector extends Block {
    private int projectRadius = 10;

    public LandProjector(String name) {
        super(name);
        solid = true;
        update = true;
        size = 2;
    }

    public class LandProjectorBuild extends Building {

        @Override
        public void updateTile() {
            boolean transform = Time.time % 60f < 1;
            if (transform) {
                int selfBlockX = tile().x;
                int selfBlockY = tile().y;
                int xm = selfBlockX - projectRadius;//x minus
                int xp = selfBlockX + projectRadius;//x plus
                int ym = selfBlockY - projectRadius;//y minus
                int yp = selfBlockY + projectRadius;//y plus
                for (int i = xm; i < xp; i++) {
                    for (int j = ym; j < yp; j++) {
                        Tile tile = world.tile(i, j);
                        if (tile != null && !(tile.floor() instanceof HoloFloor)) {
                            tile.setFloor(CioBlocks.holoFloor);
                        }
                    }
                }
            }
        }
    }
}
