package net.liplum.registries;

import mindustry.ctype.ContentList;

public interface ContentTable extends ContentList {
    void firstLoad();

    @Override
    void load();

    void lastLoad();
}
