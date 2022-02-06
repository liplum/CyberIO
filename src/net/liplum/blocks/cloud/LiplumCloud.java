package net.liplum.blocks.cloud;

import arc.struct.OrderedMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.io.JsonIO;

public class LiplumCloud {
    private static OrderedMap<Integer, SharedRoom> CurGameCloudRoom;
    public static String CurGameCloudRoomDataBackup = "";
    public static void reset() {
        CurGameCloudRoom = new OrderedMap<>();
    }

    public static SharedRoom getCloud(Team team) {
        return getCloud(team.id);
    }

    public static SharedRoom getCloud(int teamID) {
        if (CurGameCloudRoom == null) {
            reset();
        }
        SharedRoom r;
        if (CurGameCloudRoom.containsKey(teamID)) {
            r = CurGameCloudRoom.get(teamID);
        } else {
            r = new SharedRoom();
            r.whenOfflineLastOne = LiplumCloud::save;
            CurGameCloudRoom.put(teamID, r);
        }
        return r;
    }

    public static void save() {
        String data = JsonIO.json.toJson(CurGameCloudRoom);
        Log.info("Flowing is save:");
        Log.info(data);
        Vars.state.rules.tags.put("cyber-io-LiplumCloud", data);
        CurGameCloudRoomDataBackup = data;
    }

    public static void read() {
        String data = Vars.state.rules.tags.get("cyber-io-LiplumCloud", "{}");
        Log.info("Flowing is read:");
        Log.info(data);
        CurGameCloudRoom = JsonIO.json.fromJson(OrderedMap.class, data);
    }
}
