package net.liplum.blocks.cloud;

import arc.struct.OrderedMap;
import mindustry.game.Team;

public class LiplumCloud {
    private static OrderedMap<Team, SharedRoom> CurGameCloudRoom;

    public static void reset() {
        CurGameCloudRoom = new OrderedMap<>();
    }

    public static SharedRoom getCloud(Team team) {
        SharedRoom r;
        if (CurGameCloudRoom.containsKey(team)) {
            r = new SharedRoom();
            CurGameCloudRoom.put(team, r);
        } else {
            r = CurGameCloudRoom.get(team);
        }
        return r;
    }

    public static void save() {

    }

    public static void read() {

    }
}
