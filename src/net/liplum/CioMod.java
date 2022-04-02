package net.liplum;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;
import net.liplum.api.cyber.DataCenter;
import net.liplum.api.cyber.StreamCenter;
import net.liplum.blocks.cloud.LiplumCloud;
import net.liplum.blocks.cloud.SharedRoom;
import net.liplum.inputs.UnitTap;
import net.liplum.inputs.UnitTapEvent;
import net.liplum.lib.animations.ganim.GlobalAnimation;
import net.liplum.registries.*;
import net.liplum.render.LinkDrawer;
import net.liplum.ui.SettingsUI;
import net.liplum.utils.G;

import static mindustry.game.EventType.*;
import static net.liplum.registries.TintedBulletsRegistryKt.tintedBulletsRegistryLoad;

public class CioMod extends Mod {
    public static final boolean IsClient = !Vars.headless;
    public static boolean DebugMode = true;
    public static boolean TestGlCompatibility = false;
    public static boolean ExperimentalMode = false;
    public static boolean CanGlobalAnimationPlay = false;
    public static float UpdateFrequency = 5f;

    public CioMod() {
        Log.info("Cyber IO mod loaded.");
        GL.handleCompatible();
        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, Welcome::showWelcomeDialog);
        });
        Events.on(FileTreeInitEvent.class,
                e -> Core.app.post(CioShaders::init)
        );
        Events.on(DisposeEvent.class,
                e -> CioShaders.dispose()
        );
    }

    @Override
    public void init() {
        if (Vars.mobile || Vars.testMobile) {
            UpdateFrequency = 10f;
        } else {
            UpdateFrequency = 5f;
        }
        JsonIO.json.addClassTag(SharedRoom.class.getName(), SharedRoom.class);
        Events.on(WorldLoadEvent.class, e -> {
            LiplumCloud.reset();
            LiplumCloud.read();
        });
        Events.on(SaveWriteEvent.class, e -> {
            LiplumCloud.reset();
            LiplumCloud.save();
        });
        if (DebugMode) {
            Vars.enableConsole = true;
        }
        tintedBulletsRegistryLoad();
        Events.run(Trigger.update, () -> {
            GameState state = Vars.state;
            if (state.isGame() && !state.isPaused()) {
                LiplumCloud.update();
            }
        });
        DataCenter.initData();
        StreamCenter.initStream();
        StreamCenter.loadLiquidsColor();
        StreamCenter.initStreamColors();
        CioShaders.loadResource();
        GlobalAnimation.loadAllResources();
        Events.run(Trigger.preDraw, G::init);
        SettingsUI.addGraphicSettings();
        Settings.updateSettings();
        LinkDrawer.register();
        Core.input.addProcessor(UnitTap.INSTANCE);
        Log.info("Cyber IO initialized.");
    }

    @Override
    public void loadContent() {
        CioSounds.load();
        EntityRegistry.registerAll();
        CioCLs.load();
        ContentRegistry.loadContent();
        PrismBlackList.load();
        CanGlobalAnimationPlay = true;
        Log.info("Cyber IO mod's contents loaded.");
    }
}
