package net.liplum.items;

import arc.graphics.Color;
import mindustry.type.Item;

public class UncraftableItem extends Item {
    public UncraftableItem(String name, Color color) {
        super(name, color);
    }

    public UncraftableItem(String name) {
        super(name);
    }

    @Override
    public boolean unlockedNow() {
        return false;
    }

    @Override
    public boolean unlockedNowHost() {
        return false;
    }
}
