package net.liplum.data;

import arc.struct.OrderedSet;
import arc.util.Reflect;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.modules.ItemModule;
import org.jetbrains.annotations.NotNull;

public class SharedRoom implements Json.JsonSerializable {
    public final transient OrderedSet<Building> users = new OrderedSet<>();
    /**
     * Lateinit
     */
    public ItemModule sharedItemModule;
    @NotNull
    public transient CloudInfo sharedInfo = new CloudInfo(this);
    public transient Runnable whenOfflineLastOne = null;
    private transient boolean onlyOnCloud = false;

    public void online(IShared sharedBuild) {
        if (sharedItemModule == null) {
            sharedItemModule = sharedBuild.getSharedItems();
        } else {
            sharedBuild.setSharedItems(sharedItemModule);
        }
        sharedBuild.setSharedInfo(sharedInfo);
        users.add(sharedBuild.getBuilding());
    }

    public void offline(IShared sharedBuild) {
        Building b = sharedBuild.getBuilding();
        if (users.contains(b)) {
            if (users.size == 1 && whenOfflineLastOne != null) {
                onlyOnCloud = true;
                whenOfflineLastOne.run();
            }
            users.remove(b);
        }
    }

    public void update() {
        if (!users.isEmpty() && sharedItemModule != null) {
            sharedInfo.update();
        }
    }

    @Override
    public void write(Json json) {
        if (sharedItemModule != null) {
            int[] itemArray = Reflect.get(sharedItemModule, "items");
            json.writeValue("Items", itemArray);
            json.writeValue("Total", sharedItemModule.total());
        }
        json.writeValue("OnlyOnCloud", onlyOnCloud);
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        JsonValue onlyOnCloudJV = jsonValue.get("OnlyOnCloud");
        if (onlyOnCloudJV != null) {
            onlyOnCloud = onlyOnCloudJV.asBoolean();
            if (onlyOnCloud) {
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
    }
}
