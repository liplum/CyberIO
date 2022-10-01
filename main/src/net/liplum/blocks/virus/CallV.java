package net.liplum.blocks.virus;

import arc.graphics.Color;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;

public class CallV {
    public static void infect(
        Tile tile, Virus virusType, Team team,
        int curGeneration, @Nullable Color raceColor
    ) {
        if (Vars.net.server() || !Vars.net.active()) {
            Tile.setTile(tile, virusType, team, 0);
            Building build = tile.build;
            if (build instanceof Virus.VirusBuild) {
                Virus.VirusBuild vb = (Virus.VirusBuild) build;
                vb.setCurGeneration(curGeneration);
                vb.setRaceColor(raceColor);
            }
        }

        if (Vars.net.server()) {
            VInfectMsg packet = new VInfectMsg();
            packet.tile = tile;
            packet.block = virusType;
            packet.team = team;
            packet.curGeneration = curGeneration;
            packet.raceColor = raceColor;
            Vars.net.send(packet, true);
        }

    }
}
