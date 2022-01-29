package net.liplum.registries;

import arc.graphics.Color;
import mindustry.ctype.ContentList;
import mindustry.type.Item;

public class CioItems implements ContentList {
    public static Item ic;

    @Override
    public void load() {
        ic = new Item("ic", Color.valueOf("272727")) {

        };
    }
}
