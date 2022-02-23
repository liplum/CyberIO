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
        public static final String TeamName = "team";
        public static final String Team = Gen(TeamName);
        public static final String SpiralRotationSpeedName = "spiral-rotate-speed";
        public static final String SpiralRotationSpeed = Gen(SpiralRotationSpeedName);
        public static final String AlphaName = "alpha";
        public static final String Alpha = Gen(AlphaName);
        public static final String RangeName = "range";
        public static final String Range = Gen(RangeName);
        public static final String CoolDownName = "cool-down";
        public static final String CoolDown = Gen(CoolDownName);
        public static final String IsAliveName = "is-alive";
        public static final String IsAlive = Gen(IsAliveName);
        public static final String AniStateName = "ani-state";
        public static final String AniState = Gen(AniStateName);
        public static final String IsProjectingName = "is-projecting";
        public static final String IsProjecting = Gen(IsProjectingName);
        public static final String RestRestoreName = "rest-restore";
        public static final String RestRestore = Gen(RestRestoreName);
        public static final String ChargeName = "charge";
        public static final String Charge = Gen(ChargeName);
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
