package net.liplum.blocks.holo;

import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import net.liplum.V7;

@V7
public class HoloFloor extends Floor {
    public HoloFloor(String name) {
        super(name);
    }

    @Override
    public void drawBase(Tile tile) {
        super.drawBase(tile);
    }
    /*
    public class HoloFloorBuilding extends Building {
        @Override
        public void updateTile() {

        }
    }*/
}
