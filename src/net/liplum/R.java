package net.liplum;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class R {
    public static class I {
        public static final String IC = "ic";
    }

    public static class B {

    }

    public static class Bar {
        public static String Gen(String content) {
            return "bar." + Meta.ModID + "." + content;
        }

        public static final String GenerationName = "generation";
        public static final String Generation = Gen(GenerationName);
        public static final String SlowDownName = "slow-down";
        public static final String SlowDown = Gen(SlowDownName);
        public static final String EfficiencyAbsorptionName = "efficiency-absorption";
        public static final String EfficiencyAbsorption = Gen(EfficiencyAbsorptionName);
    }

    public static class C {
        public static final Color LightBlue = Color.valueOf("#add8e6");
        public static final Color VirusBK = Color.valueOf("#cd44ff");
        public static final Color Sender = Color.red;
        public static final Color Receiver = Pal.place;
    }
}
