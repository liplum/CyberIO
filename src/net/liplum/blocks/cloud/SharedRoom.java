package net.liplum.blocks.cloud;

import arc.struct.OrderedSet;
import arc.util.Reflect;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.modules.ItemModule;

public class SharedRoom implements Json.JsonSerializable {
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

    @Override
    public void write(Json json) {
        int[] itemArray = Reflect.get(sharedItemModule, "items");
        json.writeValue("Items", itemArray);
        json.writeValue("Total", sharedItemModule.total());
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        sharedItemModule = new ItemModule();
        JsonValue itemsJV = jsonValue.get("Items");
        int[] items = new int[Vars.content.items().size];
        if (itemsJV != null) {
            int[] formerItems = itemsJV.asIntArray();
            System.arraycopy(formerItems, 0, items, 0, formerItems.length);
            Reflect.set(sharedItemModule, "items", items);
        }
        JsonValue totalJV = jsonValue.get("Total");
        if (totalJV != null) {
            Reflect.set(sharedItemModule, "total", totalJV.asInt());
        }
    }
}
