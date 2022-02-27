package net.liplum;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class R {
    public static final class I {
        public static final String IC = "ic";
    }

    public static final class B {

    }

    public static final class Bar {
        public static String Gen(String content) {
            return "bar." + Meta.ModID + "." + content;
        }

        public static final String GenerationN = "generation",
                Generation = Gen(GenerationN);
        public static final String SlowDownN = "slow-down",
                SlowDown = Gen(SlowDownN);
        public static final String EfficiencyAbsorptionN = "efficiency-absorption",
                EfficiencyAbsorption = Gen(EfficiencyAbsorptionN);
        public static final String TeamN = "team",
                Team = Gen(TeamN);
        public static final String SpiralRotationSpeedN = "spiral-rotate-speed",
                SpiralRotationSpeed = Gen(SpiralRotationSpeedN);
        public static final String AlphaN = "alpha",
                Alpha = Gen(AlphaN);
        public static final String RangeN = "range",
                Range = Gen(RangeN);
        public static final String CoolDownN = "cool-down",
                CoolDown = Gen(CoolDownN);
        public static final String IsAliveN = "is-alive",
                IsAlive = Gen(IsAliveN);
        public static final String IsAsleepN = "is-asleep",
                IsAsleep = Gen(IsAsleepN);
        public static final String AniStateN = "ani-state",
                AniState = Gen(AniStateN);
        public static final String IsProjectingN = "is-projecting",
                IsProjecting = Gen(IsProjectingN);
        public static final String RestRestoreN = "rest-restore",
                RestRestore = Gen(RestRestoreN);
        public static final String ChargeN = "charge",
                Charge = Gen(ChargeN);
        public static final String NeighborStateN = "neighbor-state";
        public static final String LastDamagedN = "last-damaged",
                LastDamaged = Gen(LastDamagedN);


    }
    public static final class Ctrl {
        public static String Gen(String content) {
            return "control." + Meta.ModID + "." + content;
        }

        public static final String
                Yes = Gen("yes"),
                No = Gen("no"),
                OK = Gen("ok");
    }

    public static final class C {
        public static final Color LightBlue = Color.valueOf("#add8e6");
        public static final Color Holo = Color.valueOf("#42a5f5");
        public static final Color HoloDark = Color.valueOf("#1e88e5");
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

    public static final class S {
        public static String Gen(String name) {
            return "shaders/" + name + ".frag";
        }

        public static final String Test = Gen("test");
    }
}
