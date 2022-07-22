package net.liplum.data;

import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.io.JsonIO;
import net.liplum.CLog;
import net.liplum.Meta;
import net.liplum.annotations.Only;
import net.liplum.annotations.Subscribe;

@SuppressWarnings("unchecked")
public class LiplumCloud {
    private static OrderedMap<Integer, SharedRoom> CurGameCloudRoom;
    private static boolean Saved = false;
    private static boolean Read = false;

    public static SharedRoom getCloud(Team team) {
        return getCloud(team.id);
    }

    public static SharedRoom getCloud(int teamID) {
        if (CurGameCloudRoom == null) {
            CurGameCloudRoom = new OrderedMap<>();
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
        if (Meta.EnableDebug) {
            CLog.info("Flowing is save:");
            CLog.info(data);
        }
        Vars.state.rules.tags.put("cyber-io-LiplumCloud", data);
    }

    public static void read() {
        if (Read) {
            return;
        }
        String data = Vars.state.rules.tags.get("cyber-io-LiplumCloud", "{}");
        if (Meta.EnableDebug) {
            CLog.info("Flowing is read:");
            CLog.info(data);
        }
        OrderedMap<String, SharedRoom> strIdToRoom = JsonIO.json.fromJson(OrderedMap.class, data);
        CurGameCloudRoom = new OrderedMap<>();
        for (ObjectMap.Entry<String, SharedRoom> entry : strIdToRoom) {
            SharedRoom shardRoom = entry.value;
            shardRoom.whenOfflineLastOne = LiplumCloud::save;
            CurGameCloudRoom.put(Integer.valueOf(entry.key), shardRoom);
        }
        Read = true;
    }

    @Subscribe(triggerType = EventType.Trigger.update, only = Only.debug)
    public static void update() {
        GameState state = Vars.state;
        if (state.isGame() && !state.isPaused()) {
            if (CurGameCloudRoom != null) {
                for (SharedRoom room : CurGameCloudRoom.values()) {
                    room.update();
                }
            }
        }
    }

    public static void reset() {
        Saved = false;
        Read = false;
    }
}
