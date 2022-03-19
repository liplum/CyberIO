package net.liplum;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import net.liplum.animations.ganim.GlobalAnimation;
import net.liplum.api.data.DataCenter;
import net.liplum.api.stream.StreamCenter;
import net.liplum.blocks.cloud.LiplumCloud;
import net.liplum.blocks.cloud.SharedRoom;
import net.liplum.registries.CioCLs;
import net.liplum.registries.CioShaders;
import net.liplum.registries.ContentRegistry;
import net.liplum.utils.AtlasU;
import net.liplum.utils.G;

import static mindustry.game.EventType.*;
import static net.liplum.registries.TintedBulletsRegistryKt.tintedBulletsRegistryLoad;

public class CioMod extends Mod {
    public static final boolean IsClient = !Vars.headless;
    public static boolean DebugMode = true;
    public static boolean ExperimentalMode = false;
    public static boolean CanGlobalAnimationPlay = false;
    public static float UpdateFrequency = 5f;

    public CioMod() {
        Log.info("Cyber IO mod loaded.");
        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog(News.getTitle());
                // mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(AtlasU.inCio("icon")).maxSize(200f).pad(20f).row();
                dialog.cont.add(News.getWelcome()).wrapLabel(true).row();
                dialog.cont.add(News.getNews()).wrapLabel(true).row();
                dialog.cont.button(News.getRead(), dialog::hide).size(100f, 50f);
                dialog.show();
            });
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
        CanGlobalAnimationPlay = true;
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
    }

    @Override
    public void loadContent() {
        CioCLs.load();
        ContentRegistry.loadContent();
        Log.info("Cyber IO mod's contents loaded.");
    }
}
