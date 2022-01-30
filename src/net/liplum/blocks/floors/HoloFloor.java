package net.liplum.blocks.floors;

import mindustry.gen.Building;
import mindustry.world.blocks.environment.Floor;

public class HoloFloor extends Floor {
    public HoloFloor(String name) {
        super(name);
    }

    public class HoloFloorBuilding extends Building {
        @Override
        public void updateTile() {

        }
    }
}
