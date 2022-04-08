package net.liplum;

import java.io.InputStream;

public class Res {
    public static InputStream loadInJar(Class<?> clz, String name) {
        return clz.getResourceAsStream(name);
    }

    public static InputStream loadInThisJar(String name) {
        return Res.class.getResourceAsStream(name);
    }
}
