package net.liplum;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import net.liplum.blocks.cloud.LiplumCloud;
import net.liplum.blocks.cloud.SharedRoom;
import net.liplum.registries.ContentRegistry;
import net.liplum.registries.ShaderRegistry;
import net.liplum.utils.AtlasU;

import static mindustry.game.EventType.*;

public class CioMod extends Mod {
    public static final boolean IsClient = !Vars.headless;
    public static boolean CanGlobalAnimationPlay = false;
    public static final boolean DebugMode = true;
    public static float UpdateFrequency = 5f;

    public CioMod() {
        Log.info("Cyber IO mod loaded.");
        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("Welcome");
                dialog.cont.add("Welcome to play Cyber IO mod").row();
                // mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(AtlasU.cio("icon")).maxSize(200f).pad(20f).row();
                dialog.cont.button("Thanks", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
        Events.on(FileTreeInitEvent.class,
                e -> Core.app.post(ShaderRegistry::load)
        );

        Events.on(DisposeEvent.class,
                e -> ShaderRegistry.dispose()
        );
    }

    @Override
    public void init() {
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
        //Core.settings.put("",1);
    }

    @Override
    public void loadContent() {
        ContentRegistry.INSTANCE.loadContent();
        Log.info("Cyber IO mod's contents loaded.");
    }
}
