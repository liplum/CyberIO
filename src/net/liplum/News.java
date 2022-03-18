package net.liplum;

import arc.Core;

public class News {
    public static String getTitle() {
        return Core.bundle.format(R.Welcome.Gen(Meta.Version + "." + "title"), Meta.Version);
    }

    public static String getWelcome() {
        return Core.bundle.format(R.Welcome.Gen("welcome"), Meta.Version);
    }

    public static String getNews() {
        String news = getNewsContent();
        return Core.bundle.format(R.Welcome.Gen("news"), news);
    }

    protected static String getNewsContent() {
        return Core.bundle.format(R.Welcome.Gen(Meta.Version));
    }

    public static String getRead() {
        return Core.bundle.format(R.Welcome.Gen(Meta.Version + "." + "read"));
    }
}
