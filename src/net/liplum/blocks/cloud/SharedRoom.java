package net.liplum.blocks.cloud;

import arc.struct.OrderedSet;
import arc.util.Reflect;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.modules.ItemModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SharedRoom implements Json.JsonSerializable {
    private final transient OrderedSet<Building> users = new OrderedSet<>();
    @Nullable
    public ItemModule sharedItemModule;
    private transient boolean onlyOnCloud = false;
    @NotNull
    public transient CloudInfo sharedInfo = new CloudInfo();
    public transient Runnable whenOfflineLastOne = null;

    private void update() {
        onlyOnCloud = users.size == 0;
    }

    public void online(IShared sharedBuild) {
        if (sharedItemModule == null) {
            sharedItemModule = sharedBuild.getSharedItems();
        } else {
            sharedBuild.setSharedItems(sharedItemModule);
        }
        sharedBuild.setSharedInfo(sharedInfo);
        users.add(sharedBuild.getBuilding());
        update();
    }

    public void offline(IShared sharedBuild) {
        users.remove(sharedBuild.getBuilding());
        update();
        if (users.isEmpty() && whenOfflineLastOne != null) {
            whenOfflineLastOne.run();
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
        if (onlyOnCloudJV != null && onlyOnCloudJV.asBoolean()) {
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
