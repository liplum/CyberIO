package net.liplum;

import arc.graphics.Color;

public class R {
    public static class I {
        public static final String IC = "ic";
    }

    public static class B {

    }

    public static class Bar {
        public static String Gen(String content){
            return "bar." + Meta.ModID + "." + content;
        }
        public static final String Generation = Gen("generation");
    }

    public static class C {
        public static final Color LightBlue = Color.valueOf("#add8e6");
        public static final Color VirusBK = Color.valueOf("#cd44ff");
    }
}
