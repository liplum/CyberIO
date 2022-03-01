package net.liplum.blocks;

import mindustry.world.blocks.production.GenericCrafter;
import net.liplum.CioMod;
import net.liplum.GameH;
import net.liplum.animations.anis.AniConfig;
import net.liplum.animations.anis.AniState;
import net.liplum.animations.anis.AniStateM;
import net.liplum.animations.anis.IAniSMed;

import java.util.Collection;
import java.util.HashMap;

public abstract class AniedCrafter extends GenericCrafter implements IAniSMed<AniedCrafter, AniedCrafter.AniedCrafterBuild> {
    public AniConfig<AniedCrafter, AniedCrafterBuild> aniConfig;
    public HashMap<String, AniState<AniedCrafter, AniedCrafterBuild>> allAniStates = new HashMap<>();

    public AniedCrafter(String name) {
        super(name);
        if (CioMod.IsClient) {
            this.genAniState();
            this.genAniConfig();
        }
    }

    @Override
    public AniConfig<AniedCrafter, AniedCrafterBuild> getAniConfig() {
        return aniConfig;
    }

    @Override
    public void setAniConfig(AniConfig<AniedCrafter, AniedCrafterBuild> config) {
        aniConfig = config;
    }

    @Override
    public AniState<AniedCrafter, AniedCrafterBuild> getAniStateByName(String name) {
        return allAniStates.get(name);
    }

    @Override
    public Collection<AniState<AniedCrafter, AniedCrafterBuild>> getAllAniStates() {
        return allAniStates.values();
    }

    public AniState<AniedCrafter, AniedCrafterBuild> addAniState(AniState<AniedCrafter, AniedCrafterBuild> aniState) {
        allAniStates.put(aniState.getStateName(), aniState);
        return aniState;
    }


    public class AniedCrafterBuild extends GenericCrafterBuild {
        private AniStateM<AniedCrafter, AniedCrafterBuild> aniStateM;

        @Override
        public void created() {
            if (CioMod.IsClient) {
                AniedCrafter out = AniedCrafter.this;
                this.aniStateM = out.getAniConfig().gen(out, this);
            }
        }

        @Override
        public void draw() {
            if (GameH.CanRefresh()) {
                aniStateM.update();
            }
            if (!aniStateM.curOverwriteBlock()) {
                super.draw();
            }
            aniStateM.drawBuilding();
        }
    }
}
