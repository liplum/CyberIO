package net.liplum.blocks;

import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import net.liplum.CioMod;
import net.liplum.animations.AniConfig;
import net.liplum.animations.AniState;
import net.liplum.animations.AniStateM;
import net.liplum.animations.IAniSMed;

import java.util.Collection;
import java.util.HashMap;

public abstract class AniedCrafter extends GenericCrafter implements IAniSMed<AniedCrafter, AniedCrafter.AniedCrafterBuild> {
    public AniConfig<AniedCrafter, AniedCrafterBuild> aniConfig;
    public HashMap<String, AniState<AniedCrafter, AniedCrafterBuild>> allAniStates = new HashMap<>();

    public AniedCrafter(String name) {
        super(name);
        if (CioMod.AniStateCanLoad) {
            this.genAnimState();
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

    protected AniState<AniedCrafter, AniedCrafterBuild> addAniState(AniState<AniedCrafter, AniedCrafterBuild> aniState) {
        allAniStates.put(aniState.getStateName(), aniState);
        return aniState;
    }


    public class AniedCrafterBuild extends GenericCrafterBuild {
        private AniStateM<AniedCrafter, AniedCrafterBuild> aniStateM;

        @Override
        public Building create(Block block, Team team) {
            super.create(block, team);
            if (CioMod.AniStateCanLoad) {
                AniedCrafter out = AniedCrafter.this;
                this.aniStateM = out.getAniConfig().gen(out, this);
            }
            return this;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (CioMod.AniStateCanLoad) {
                aniStateM.update();
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (CioMod.AniStateCanLoad) {
                aniStateM.drawBuilding();
            }
        }
    }
}
