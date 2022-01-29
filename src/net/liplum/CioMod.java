package net.liplum;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;
import net.liplum.registries.ContentRegistry;

public class CioMod extends Mod {
    public static final boolean AniStateCanLoad = true;

    public CioMod() {
        Log.info("Cyber IO mod loaded.");
        //listen for game load event
        Events.on(EventType.ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("Welcome");
                dialog.cont.add("Welcome to play Cyber IO mod").row();
                // mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                // dialog.cont.image(Core.atlas.find("cyber-io-frog")).pad(20f).row();
                dialog.cont.button("Thanks", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void init() {

    }

    @Override
    public void loadContent() {
        ContentRegistry.loadContent();
        Log.info("Cyber IO mod's contents loaded.");
    }
}
