package net.liplum.registries;

import mindustry.ctype.ContentList;

import java.util.LinkedList;
import java.util.List;

public class ContentRegistry {
    public static List<ContentList> ContentLists = new LinkedList<>();
    public static ContentList ItemList = with(new CioItems());
    public static ContentList BlockList = with(new CioBlocks());

    public static ContentList with(ContentList cl) {
        ContentLists.add(cl);
        return cl;
    }

    public static void loadContent() {
        for (ContentList cl : ContentLists) {
            cl.load();
        }
    }
}
