package net.liplum.blocks;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;

public class Virus extends Block {
    public int spreadingSpeed;

    public Virus(String name) {
        super(name);
        solid = true;
        update = true;
        spreadingSpeed = 1000;
    }

    public class VirusBuild extends Building {
        @Override
        public void updateTile() {
            int luckyNumber = Mathf.random(spreadingSpeed);
            if (luckyNumber == 0) {
                int randomDX = Mathf.random(-1, 1);
                int randomDY = Mathf.random(-1, 1);
                int selfX = tile.x;
                int selfY = tile.y;
                Tile infected = Vars.world.tile(selfX + randomDX, selfY + randomDY);
                if (infected != null) {
                    if (!(infected.build instanceof VirusBuild) && !(infected.block() instanceof CoreBlock)) {
                        infected.setBlock(Virus.this);
                        infected.setFloor((Floor) Blocks.air);
                    }
                }
            }
        }
    }
}
