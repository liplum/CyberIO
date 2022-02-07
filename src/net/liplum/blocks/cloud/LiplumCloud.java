package net.liplum.blocks.cloud;

import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.io.JsonIO;
import net.liplum.CioMod;

public class LiplumCloud {
    private static OrderedMap<Integer, SharedRoom> CurGameCloudRoom;

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
        if (CioMod.DebugMode) {
            Log.info("Flowing is save:");
            Log.info(data);
        }
        Vars.state.rules.tags.put("cyber-io-LiplumCloud", data);
    }

    public static void read() {
        String data = Vars.state.rules.tags.get("cyber-io-LiplumCloud", "{}");
        if (CioMod.DebugMode) {
            Log.info("Flowing is read:");
            Log.info(data);
        }
        OrderedMap<String, SharedRoom> strIdToRoom = JsonIO.json.fromJson(OrderedMap.class, data);
        CurGameCloudRoom = new OrderedMap<>();
        for (ObjectMap.Entry<String, SharedRoom> entry : strIdToRoom) {
            CurGameCloudRoom.put(Integer.valueOf(entry.key), entry.value);
        }
    }
}
