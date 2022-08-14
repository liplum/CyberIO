package net.liplum;

import arc.graphics.Color;
import mindustry.graphics.Pal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class R {
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
        public static final String Blood = "blood";
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
        public static final String Unlimited = Gen("unlimited");
        public static final String
            CostSecond = Gen("cost.second"),
            CostMinute = Gen("cost.minute");
        public static final String
            FullSender = Gen("full-sender"),
            FullReceiver = Gen("full-receiver"),
            FullHost = Gen("full-host"),
            FullClient = Gen("full-client"),
            OverRange = Gen("over-range");
        public static final String
            UnsavedChange = Gen("unsaved-change");
        public static final String
            RandomName = Gen("random-name");

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return Meta.ModID + "." + content;
        }
    }

    public static final class Bar {
        public static final String NeighborStateN = "neighbor-state";

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return "bar." + Meta.ModID + "." + content;
        }

        public static final class Vanilla {
            public static final String UnitsN = "units",
                UnitCapacity = "bar.unitcap";
        }        public static final String GenerationN = "generation",
            Generation = Gen(GenerationN);



        public static final String SlowDownN = "slow-down",
            SlowDown = Gen(SlowDownN);
        public static final String EfficiencyAbsorptionN = "efficiency-absorption",
            EfficiencyAbsorption = Gen(EfficiencyAbsorptionN);
        public static final String RangeN = "range",
            Range = Gen(RangeN);
        public static final String CoolDownN = "cool-down",
            CoolDown = Gen(CoolDownN);
        public static final String IsAliveN = "is-alive",
            IsAlive = Gen(IsAliveN);
        public static final String NullN = "null",
            Null = Gen(NullN);

        public static final String
            PrismN = "prism",
            Prism = Gen(PrismN);

        public static final String
            LinkedN = "linked",
            Linked = Gen(LinkedN),
            Unlinked = Gen("unlinked");
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
        public static final String BloodN = "blood",
            Blood = Gen(BloodN);
        public static final String TemperatureN = "temperature",
            Temperature = Gen(TemperatureN);
    }

    public static final class Ctrl {
        public static final String
            Enable = Gen("enable");

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return "control." + Meta.ModID + "." + content;
        }
    }

    public static final class Advanced {
        public static final String
            MapCleaner = Gen("map-cleaner"),
            GithubMirror = Gen("github-mirror");

        @NotNull
        @Contract(pure = true)
        public static String Gen(String content) {
            return "advanced." + Meta.ModID + "." + content;
        }
    }

    public static final class C {
        @NotNull
        public static final Color
            RedAlert = Color.valueOf("#e53935"),
            RedAlertDark = Color.valueOf("#d50000"),
            GreenSafe = Color.valueOf("#00e676");
        @NotNull
        public static final Color RedDark = Color.valueOf("#7d0000");
        @NotNull
        public static final Color LightBlue = Color.valueOf("#add8e6");
        @NotNull
        public static final Color
            Holo = Color.valueOf("#42a5f5"),//(0.2588,0.6471,0.9608)
            HoloOrange = Color.valueOf("#ff8f00"),//(1.0,0.5608,0)
            HoloDark = Color.valueOf("#1e88e5"),//(0.1176,0.5333,0.898)
            HoloDarkOrange = Color.valueOf("#ff6f00");//(1,0.0.4353,0.898)
        @NotNull
        public static final Color VirusBK = Color.valueOf("#cd44ff");
        @NotNull
        public static final Color
            BrainWave = Color.valueOf("#be61ab"),
            TissueFluid = Color.valueOf("#cc8ebf"),
            Blood = Color.valueOf("#8a0303"),
            ColdTemperature = Color.valueOf("#242f9b"),
            HotTemperature = Color.valueOf("#e02401");
        public static final Color Providence = Color.valueOf("#fff799");
        public static final Color Black = new Color(Color.black);
        public static final Color IsAive = GreenSafe;
        public static final Color CoolDown = Color.gray;
        @NotNull
        public static final Color
            Sender = RedAlert,
            Receiver = Pal.place,
            Host = RedAlert,
            Client = Pal.place,
            P2P = LightBlue;
        @NotNull
        public static final
        Color IcLight = Color.valueOf("#047b04"),
            IcDark = Color.valueOf("#024202");
        public static final Color Shadow = new Color(0, 0, 0, 0.71f);
        public static final Color BlendShadow = Color.white.cpy().lerp(Color.black, Shadow.a);
        public static final Color Power = Pal.power;
        @NotNull
        public static final Color SonicWave = Color.valueOf("#b0bec5");
        @NotNull
        public static final Color
            FutureBlue = Color.valueOf("#84fff1"),
            CuttexCyan = Color.valueOf("#c0ecff");

        @NotNull
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
        public static final Color Stop = RedAlert;/* Color.valueOf("#dd2c00");*/
        public static final Color Unconnected = Color.white;
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
        public static final String WelcomeVariants = "WelcomeVariants";
        public static final String IconPath = "IconPath";

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
    }

    public static final class Sound {
        public static final String ogg = "ogg",
            wav = "wav",
            mp3 = "mp3";
        public static final String[] extensions = {
            wav, mp3, ogg
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
    }

    public static final class Setting {
        public static final String
            LinkOpacity = Gen("connection-opacity"),
            LinkArrowDensity = Gen("connection-density"),
            LinkAnimationSpeed = Gen("connection-animation-speed"),
            AlwaysShowLink = Gen("always-show-link"),
            LinkBloom = Gen("connection-bloom"),
            ShowLinkCircle = Gen("show-link-circle"),
            ShowWirelessTowerCircle = Gen("show-wireless-tower-circle"),
            LinkSize = Gen("connection-size"),// Deprecated
            ShowWelcome = Gen("show-welcome"),
            ShowUpdate = Gen("show-update"),
            LastSkippedUpdate = Gen("last-skipped-update"),
            Version = Gen("version"),
            ClickWelcomeTimes = Gen("welcome-click-times"),
            LastWelcome = Gen("last-welcome"),
            LastWelcomeID = Gen("last-welcome-id"),
            FirstInstallationTime = Gen("first-install-time"),
            CyberIOLoadedTimes = Gen("cio-loaded-times"),
            ClassLoadedTimes = Gen("clz-loaded-times"),
            LastPlayTime = Gen("last-play-time"),
            GitHubMirrorUrl = Gen("github-mirror"),// Deprecated
            ShaderRootPath = Gen("shader-root-path"),
            ContentSpecific = Gen("content-specific"),
            CheckUpdate = Gen("check-update");
    }

    public static final class CMD {
        public static final String
            ResetConfig = "cio-reset-config",
            ReloadConfig = "cio-reload-config",
            CheckUpdate = "cio-update",
            ClearContent = "cio-clean-map",
            SwitchSpec = "cio-spec";
    }
}
