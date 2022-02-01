package net.liplum.blocks.virus;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Tile;
import net.liplum.blocks.AnimedBlock;
import net.liplum.utils.VirusUtil;

public class Virus extends AnimedBlock {
    public int spreadingSpeed;

    public Virus(String name) {
        super(name);
        solid = true;
        update = true;
        spreadingSpeed = 1000;
        canOverdrive = true;
    }

    @Override
    public int minimapColor(Tile tile) {
        return Color.purple.rgba();
    }

    public class VirusBuild extends Building {
        @Override
        public void updateTile() {
            int speed = spreadingSpeed;
            if (canOverdrive) {
                speed = (int) (speed / timeScale);
            }
            int luckyNumber = Mathf.random(speed);
            if (luckyNumber == 0) {
                int randomDX = Mathf.random(-1, 1);
                int randomDY = Mathf.random(-1, 1);
                int selfX = tile.x;
                int selfY = tile.y;
                Tile infected = Vars.world.tile(selfX + randomDX, selfY + randomDY);
                if (VirusUtil.canInfect(infected)) {
                    infected.setBlock(Virus.this, team);
                }
            }
        }
    }
}
