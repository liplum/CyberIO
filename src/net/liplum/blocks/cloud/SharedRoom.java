package net.liplum.blocks.cloud;

import arc.struct.OrderedSet;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.gen.Building;
import mindustry.world.modules.ItemModule;

public class SharedRoom {
    public transient OrderedSet<Building> users;
    public ItemModule sharedItemModule;
    public transient Runnable whenOfflineLastOne = null;

    public SharedRoom() {
        users = new OrderedSet<>();
    }

    public void online(IShared sharedBuild) {
        if (sharedItemModule == null) {
            sharedItemModule = sharedBuild.getSharedItems();
        } else {
            sharedBuild.setSharedItems(sharedItemModule);
        }
        users.add(sharedBuild.getBuilding());
    }

    public void offline(IShared sharedBuild) {
        users.remove(sharedBuild.getBuilding());
        if (users.isEmpty() && whenOfflineLastOne != null) {
            whenOfflineLastOne.run();
        }
    }
}
