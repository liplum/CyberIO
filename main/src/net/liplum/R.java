package net.liplum;

import arc.graphics.Color;
import mindustry.graphics.Pal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class R {
    @NotNull
    @Contract(pure = true)
    public static String Gen(String name) {
        return Meta.ModID + "-" + name;
    }

    public static final class I {
        public static final String IC = "ic";
    }

    public static final class Liquid {
        public static final String Cyberion = "cyberion";
        public static final String TissueFluid = "tissue-fluid";
    }

    public static final class B {

    }

    public static final class Unit {
        public static final String
                HoloMiner = "holo-miner",
                HoloFighter = "holo-fighter",
                HoloGuardian = "holo-guardian",
                HoloArchitect = "holo-architect",
                HoloSupporter = "holo-supporter",
                Brain = "brain";
    }

    public static final class Bundle {
        public static final String UnitSeconds = "unit.seconds";

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return Meta.ModID + "." + content;
        }

        public static final String CyberionReq = Gen("cyberion-req");
        public static final String
                CostSecond = Gen("cost.second"),
                CostMinute = Gen("cost.minute");
        public static final String
                PercentPlus = Gen("percent.plus"),
                PercentMinus = Gen("percent.minus"),
                ValuePlus = Gen("value.plus"),
                ValueMinus = Gen("value.minus");
    }

    public static final class Bar {
        public static final String NeighborStateN = "neighbor-state";
        public static final String PowerN = "power";

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return "bar." + Meta.ModID + "." + content;
        }

        public static final String GenerationN = "generation",
                Generation = Gen(GenerationN);

        public static final class Vanilla {
            public static final String BuildProgressN = "progress",
                    BuildProgress = "bar.progress";
            public static final String UnitsN = "units",
                    UnitCapacity = "bar.unitcap";
        }

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
        public static final String AniStateLastN = "ani-state-last",
                AniStateLast = Gen(AniStateLastN);
        public static final String NullN = "null",
                Null = Gen(NullN);
        public static final String IsProjectingN = "is-projecting",
                IsProjecting = Gen(IsProjectingN);
        public static final String RestRestoreN = "rest-restore",
                RestRestore = Gen(RestRestoreN);
        public static final String ChargeN = "charge",
                Charge = Gen(ChargeN);

        public static final String LastDamagedN = "last-damaged",
                LastDamaged = Gen(LastDamagedN);
        public static final String ProgressN = "progress",
                Progress = Gen(ProgressN);
        public static final String
                PrismN = "prism",
                Prism = Gen(PrismN),
                PrismPl = Gen(PrismN + ".pl");
        public static final String
                LinkedN = "linked",
                Linked = Gen(LinkedN),
                NoLink = Gen(LinkedN + ".no");
        public static final String StatusN = "status",
                Status = Gen(StatusN);
        public static final String ReceiverN = "receiver",
                Receiver = Gen(ReceiverN);
        public static final String SenderN = "sender",
                Sender = Gen(SenderN);
        public static final String ClientN = "client",
                Client = Gen(ClientN);
        public static final String HostN = "host",
                Host = Gen(HostN);
        public static final String RestLifeN = "rest-life",
                RestLife = Gen(RestLifeN),
                RestLifeFigure = Gen(RestLifeN + ".figure");
        public static final String RestResurrectN = "rest-resurrect",
                RestResurrect = Gen(RestResurrectN);
        public static final String FormationN = "formation";
    }

    public static final class Ctrl {
        public static final String
                Yes = Gen("yes"),
                No = Gen("no"),
                OK = Gen("ok"),
                Validate = Gen("validate"),
                UpdateModSuccess = Gen("update-mod.success"),
                UpdateModFailed = Gen("update-mod.failed");

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return "control." + Meta.ModID + "." + content;
        }
    }

    public static final class C {
        public static final Color RedAlert = Color.valueOf("#e53935");
        public static final Color RedAlertDark = Color.valueOf("#d50000");
        public static final Color RedDark = Color.valueOf("#7d0000");
        public static final Color GreenSafe = Color.valueOf("#00e676");
        public static final Color LightBlue = Color.valueOf("#add8e6");
        public static final Color Holo = Color.valueOf("#42a5f5");//(0.2588,0.6471,0.9608)
        public static final Color HoloDark = Color.valueOf("#1e88e5");//(0.1176,0.5333,0.898)
        public static final Color HoloDark2 = Color.valueOf("#0c80df");
        public static final Color VirusBK = Color.valueOf("#cd44ff");
        public static final Color IsAive = GreenSafe;
        public static final Color CoolDown = Color.gray;
        public static final Color Sender = RedAlert,
                Receiver = Pal.place;
        public static final Color Cloud = Color.white;
        public static final Color IcLight = Color.valueOf("#047b04");
        public static final Color IcDark = Color.valueOf("#024202");
        public static final Color Shadow = new Color(0, 0, 0, 0.71f);
        public static final Color BlendShadow = Color.white.cpy().lerp(Color.black, Shadow.a);
        public static final Color Power = Pal.power;
        public static final Color BrainWave = Color.valueOf("#be61ab");
        public static final Color TissueFluid = Color.valueOf("#cc8ebf");
        public static final Color SonicWave = Color.valueOf("#b0bec5");

        public static final Color
                PrismRedFG = Color.valueOf("#EF5350"), PrismRedBK = Color.valueOf("#E53935"),
                PrismGreenFG = Color.valueOf("#76FF03"), PrismGreenBK = Color.valueOf("#00C853"),
                PrismBlueFG = Color.valueOf("#039BE5"), PrismBlueBK = Color.valueOf("#1565C0");
        @NotNull
        public static final Color[] PrismRgbFG = {
                PrismRedFG, PrismGreenFG, PrismBlueFG
        };
        @NotNull
        public static final Color[] PrismRgbBK = {
                PrismRedBK, PrismGreenBK, PrismBlueBK
        };
        public static final Color
                prismClockwise = Pal.power,
                prismAntiClockwise = Pal.lancerLaser;
        public static final Color Stop = RedAlert;/* Color.valueOf("#dd2c00");*/
        public static final Color Unconnected = Color.white;
        public static final Color Host = RedAlert,
                Client = Pal.place;
        public static Color[] LiquidColors;
        public static Color[] HostLiquidColors;
        public static Color[] ClientLiquidColors;
    }

    public static final class SD {
        public static final String Test = GenFrag("test");

        @NotNull
        @Contract(pure = true)
        public static String GenFrag(String name) {
            return "shaders/" + name + ".frag";
        }

        @NotNull
        @Contract(pure = true)
        public static String GenTR(String name) {
            return "sprites/shaders/" + name + ".png";
        }
    }

    public static final class Welcome {
        @NotNull
        @Contract(pure = true)
        public static String Gen(String name) {
            return "welcome." + Meta.ModID + "." + name;
        }

        @NotNull
        @Contract(pure = true)
        public static String Atlas(String name) {
            return Meta.ModID + "-welcome-" + name;
        }

        public static final String WelcomeVariants = "WelcomeVariants";
        public static final String IconPath = "IconPath";
    }

    public static final class Sound {
        public static final String ogg = "ogg",
                wav = "wav",
                mp3 = "mp3";
        public static final String[] extensions = {
                wav, ogg, mp3
        };

        @NotNull
        @Contract(pure = true)
        public static String OGG(String name) {
            return Gen(name, ogg);
        }

        @NotNull
        @Contract(pure = true)
        public static String WAV(String name) {
            return Gen(name, wav);
        }

        @NotNull
        @Contract(pure = true)
        public static String MP3(String name) {
            return Gen(name, mp3);
        }

        @NotNull
        @Contract(pure = true)
        public static String Gen(String name) {
            return "sounds/" + name + ".";
        }

        @NotNull
        @Contract(pure = true)
        public static String Gen(String name, String extension) {
            return Gen(name) + extension;
        }
    }

    public static final class UI {
        public static String Gen(String name) {
            return "ui." + Meta.ModID + "." + name;
        }

        public static final String
                MousePositionTile = Gen("mouse-position.tile"),
                MousePositionBuild = Gen("mouse-position.build"),
                MousePositionWorld = Gen("mouse-position.world");
    }

    public static final class Setting {
        public static final String
                LinkOpacity = Gen("connection-opacity"),
                LinkArrowDensity = Gen("connection-density"),
                AlwaysShowLink = Gen("always-show-link"),
                ShowLinkCircle = Gen("show-link-circle"),
                LinkSize = Gen("connection-size"),
                ShowWelcome = Gen("show-welcome"),
                ShowUpdate = Gen("show-update"),
                Version = Gen("version"),
                ClickWelcomeTimes = Gen("welcome-click-times"),
                LastWelcome = Gen("last-welcome"),
                LastWelcomeID = Gen("last-welcome-id"),
                FirstInstallationTime = Gen("first-install-time"),
                LastPlayTime = Gen("last-play-time"),
                GitHubMirrorUrl = Gen("github-mirror"),
                CheckUpdate = Gen("check-update");
    }

    public static final class CMD {
        public static final String
                ResetConfig = "cio-reset-config",
                ReloadConfig = "cio-reload-config",
                CheckUpdate = "cio-check-update";
    }
}
