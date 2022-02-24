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

        public static final String GenerationN = "generation";
        public static final String Generation = Gen(GenerationN);
        public static final String SlowDownN = "slow-down";
        public static final String SlowDown = Gen(SlowDownN);
        public static final String EfficiencyAbsorptionN = "efficiency-absorption";
        public static final String EfficiencyAbsorption = Gen(EfficiencyAbsorptionN);
        public static final String TeamN = "team";
        public static final String Team = Gen(TeamN);
        public static final String SpiralRotationSpeedN = "spiral-rotate-speed";
        public static final String SpiralRotationSpeed = Gen(SpiralRotationSpeedN);
        public static final String AlphaN = "alpha";
        public static final String Alpha = Gen(AlphaN);
        public static final String RangeN = "range";
        public static final String Range = Gen(RangeN);
        public static final String CoolDownN = "cool-down";
        public static final String CoolDown = Gen(CoolDownN);
        public static final String IsAliveN = "is-alive";
        public static final String IsAlive = Gen(IsAliveN);
        public static final String IsAsleepN = "is-asleep";
        public static final String IsAsleep = Gen(IsAsleepN);
        public static final String AniStateN = "ani-state";
        public static final String AniState = Gen(AniStateN);
        public static final String IsProjectingN = "is-projecting";
        public static final String IsProjecting = Gen(IsProjectingN);
        public static final String RestRestoreN = "rest-restore";
        public static final String RestRestore = Gen(RestRestoreN);
        public static final String ChargeN = "charge";
        public static final String Charge = Gen(ChargeN);
        public static final String NeighborStateN = "neighbor-state";
    }

    public static class C {
        public static final Color LightBlue = Color.valueOf("#add8e6");
        public static final Color Holo = Color.valueOf("#42a5f5");
        public static final Color VirusBK = Color.valueOf("#cd44ff");
        public static final Color IsAive = Color.green;
        public static final Color CoolDown = Color.gray;
        public static final Color Sender = Color.red;
        public static final Color Receiver = Pal.place;
        public static final Color Cloud = Color.white;
        public static final Color IcLight = Color.valueOf("#047b04");
        public static final Color IcDark = Color.valueOf("#024202");
        public static final Color shadowColor = new Color(0, 0, 0, 0.71f);
        public static final Color blendShadowColor = Color.white.cpy().lerp(Color.black, shadowColor.a);

    }

    public static class S {
        public static String Gen(String name) {
            return "shaders/" + name + ".frag";
        }

        public static final String Test = Gen("test");
    }
}
