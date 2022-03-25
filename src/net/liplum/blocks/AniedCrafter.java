package net.liplum.blocks;

import mindustry.world.blocks.production.GenericCrafter;
import net.liplum.CioMod;
import net.liplum.ClientOnly;
import net.liplum.GameH;
import net.liplum.animations.anis.*;
import net.liplum.utils.DebugH;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public abstract class AniedCrafter<TBlock extends AniedCrafter<?, ?>, TBuild extends AniedCrafter<?, ?>.AniedCrafterBuild>
        extends GenericCrafter
        implements IAniSMed<TBlock, TBuild> {
    @ClientOnly
    public AniConfig<TBlock, TBuild> aniConfig;
    @ClientOnly
    public HashMap<String, AniState<TBlock, TBuild>> allAniStates = new HashMap<>();
    @ClientOnly
    public boolean callDefaultBlockDraw = true;

    public AniedCrafter(String name) {
        super(name);
        if (CioMod.IsClient) {
            this.genAniState();
            this.genAniConfig();
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        if (CioMod.DebugMode) {
            DebugH.addProgressInfo(bars);
            DebugH.addAniStateInfo(bars);
        }
    }

    @Override
    public AniConfig<TBlock, TBuild> getAniConfig() {
        return aniConfig;
    }

    @Override
    public void setAniConfig(AniConfig<TBlock, TBuild> config) {
        aniConfig = config;
    }

    @Override
    public AniConfig<TBlock, TBuild> createAniConfig() {
        aniConfig = new AniConfig<>();
        return aniConfig;
    }

    @Override
    public AniState<TBlock, TBuild> getAniStateByName(String name) {
        return allAniStates.get(name);
    }

    @Override
    public Collection<AniState<TBlock, TBuild>> getAllAniStates() {
        return allAniStates.values();
    }

    public AniState<TBlock, TBuild> addAniState(AniState<TBlock, TBuild> aniState) {
        allAniStates.put(aniState.getStateName(), aniState);
        return aniState;
    }

    @NotNull
    @Override
    public AniStateM<TBlock, TBuild> getAniStateM(TBuild build) {
        return (AniStateM<TBlock, TBuild>) build.getAniStateM();
    }

    public class AniedCrafterBuild extends GenericCrafterBuild implements IAniSMedBuild<TBlock, TBuild> {
        private AniStateM<TBlock, TBuild> aniStateM;

        public AniedCrafterBuild() {
            if (CioMod.IsClient) {
                AniedCrafter<TBlock, TBuild> out = AniedCrafter.this;
                this.aniStateM = out.getAniConfig().gen((TBlock) out, (TBuild) this);
                this.aniStateM.onUpdate(this::onAniStateMUpdate);
            }
        }

        @NotNull
        @Override
        public AniStateM<TBlock, TBuild> getAniStateM() {
            return aniStateM;
        }

        @ClientOnly
        public void onAniStateMUpdate() {

        }

        /**
         * Overwrite this please
         */
        @ClientOnly
        public void beforeDraw() {

        }

        @Override
        public void draw() {
            aniStateM.spend(delta());
            beforeDraw();
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
