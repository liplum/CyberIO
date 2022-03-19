package net.liplum.blocks;

import arc.util.Nullable;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.CioMod;
import net.liplum.ClientOnly;
import net.liplum.GameH;
import net.liplum.animations.anis.*;
import net.liplum.api.ITrigger;
import net.liplum.utils.DebugH;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * A block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
@SuppressWarnings("unchecked")
public abstract class AniedBlock<TBlock extends AniedBlock<?, ?>, TBuild extends AniedBlock<?, ?>.AniedBuild> extends Block implements IAniSMed<TBlock, TBuild> {
    @ClientOnly
    protected final HashMap<String, AniState<TBlock, TBuild>> allAniStates = new HashMap<>();
    @ClientOnly
    protected AniConfig<TBlock, TBuild> aniConfig;
    @ClientOnly
    public boolean callDefaultBlockDraw = true;

    public AniedBlock(String name) {
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
            DebugH.addAniStateInfo(bars);
        }
    }

    @Override
    @Nullable
    public AniState<TBlock, TBuild> getAniStateByName(String name) {
        return allAniStates.get(name);
    }

    @Override
    public Collection<AniState<TBlock, TBuild>> getAllAniStates() {
        return allAniStates.values();
    }

    @Override
    public AniConfig<TBlock, TBuild> getAniConfig() {
        return aniConfig;
    }

    @Override
    public void setAniConfig(AniConfig<TBlock, TBuild> config) {
        aniConfig = config;
    }

    public AniState<TBlock, TBuild> addAniState(AniState<TBlock, TBuild> aniState) {
        allAniStates.put(aniState.getStateName(), aniState);
        return aniState;
    }

    @Override
    public AniConfig<TBlock, TBuild> createAniConfig() {
        aniConfig = new AniConfig<>();
        return aniConfig;
    }

    @NotNull
    @Override
    public AniStateM<TBlock, TBuild> getAniStateM(TBuild build) {
        return (AniStateM<TBlock, TBuild>) build.getAniStateM();
    }

    /**
     * Add an entry of configuration.
     *
     * @param from     the current State
     * @param to       the next State
     * @param canEnter When the current State can enter next
     * @return the configuration self
     */
    public AniConfig<TBlock, TBuild> entry(String from, String to, ITrigger<TBuild> canEnter) {
        return aniConfig.entry(getAniStateByName(from), getAniStateByName(to), canEnter);
    }

    /**
     * You have to make the {@link Building} of your subclass extend this
     */
    public abstract class AniedBuild extends Building implements IAniSMedBuild<TBlock, TBuild> {
        protected AniStateM<TBlock, TBuild> aniStateM;

        @NotNull
        @Override
        public AniStateM<TBlock, TBuild> getAniStateM() {
            return aniStateM;
        }

        @Override
        public Building create(Block block, Team team) {
            super.create(block, team);
            if (CioMod.IsClient) {
                AniedBlock<TBlock, TBuild> outer = AniedBlock.this;
                this.aniStateM = outer.getAniConfig().gen((TBlock) outer, (TBuild) this);
            }
            return this;
        }

        /**
         * Overwrite this please
         */
        public void fixedDraw() {

        }

        /**
         * Overwrite this please
         */
        public void beforeDraw() {

        }

        /**
         * Don't overwrite it unless you want a custom function
         */
        @Override
        public void draw() {
            aniStateM.spend(delta());
            beforeDraw();
            if (GameH.CanRefresh()) {
                aniStateM.update();
            }
            if (callDefaultBlockDraw && !aniStateM.curOverwriteBlock()) {
                super.draw();
            }
            fixedDraw();
            aniStateM.drawBuilding();
        }
    }
}