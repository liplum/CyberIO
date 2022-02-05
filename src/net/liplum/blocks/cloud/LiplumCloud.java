package net.liplum.blocks.cloud;

import arc.struct.OrderedMap;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.io.JsonIO;

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
        String data = JsonIO.json.toJson(CurGameCloudRoom);
        Log.info(data);
        // Vars.state.rules.tags.put("cyber-io-LiplumCloud","");
    }

    public static void read() {
        // String data = Vars.state.rules.tags.get("cyber-io-LiplumCloud", "");
        // JsonIO.json.fromJson(ItemModule.class,data);
    }
}
