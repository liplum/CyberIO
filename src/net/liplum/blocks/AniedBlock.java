package net.liplum.blocks;

import arc.util.Nullable;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.CioMod;
import net.liplum.animations.anis.AniConfig;
import net.liplum.animations.anis.AniState;
import net.liplum.animations.anis.AniStateM;
import net.liplum.animations.anis.IAniSMed;
import net.liplum.api.ITrigger;
import net.liplum.utils.AniU;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * A block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
public abstract class AniedBlock<TBlock extends Block, TBuild extends Building> extends Block implements IAniSMed<TBlock, TBuild> {
    protected final HashMap<String, AniState<TBlock, TBuild>> allAniStates = new HashMap<>();
    protected AniConfig<TBlock, TBuild> aniConfig;

    public AniedBlock(String name) {
        super(name);
        if (CioMod.CanAniStateLoad) {
            this.genAniState();
            this.genAniConfig();
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

    /**
     * Add an entry of configuration.
     *
     * @param from     the current State
     * @param to       the next State
     * @param canEnter When the current State can enter next
     * @return the configuration self
     */
    public AniConfig<TBlock, TBuild> entry(String from, String to, ITrigger<TBlock, TBuild> canEnter) {
        return aniConfig.entry(getAniStateByName(from), getAniStateByName(to), canEnter);
    }

    /**
     * You have to make the {@link Building} of your subclass extend this
     */
    public abstract class AniedBuild extends Building {
        protected AniStateM<TBlock, TBuild> aniStateM;

        @NotNull
        public AniStateM<TBlock, TBuild> getAniStateM() {
            return aniStateM;
        }

        @Override
        public Building create(Block block, Team team) {
            super.create(block, team);
            if (CioMod.CanAniStateLoad) {
                AniedBlock<TBlock, TBuild> outer = AniedBlock.this;
                this.aniStateM = outer.getAniConfig().gen((TBlock) outer, (TBuild) this);
            }
            return this;
        }

        /**
         * Overwrite this please
         */
        public void fixedUpdateTile() {

        }

        /**
         * Overwrite this please
         */
        public void fixedDraw() {

        }

        /**
         * Don't overwrite it unless you want a custom function
         */
        @Override
        public void updateTile() {
            super.updateTile();
            fixedUpdateTile();
        }

        /**
         * Don't overwrite it unless you want a custom function
         */
        @Override
        public void draw() {
            if (CioMod.CanAniStateLoad) {
                if (AniU.needUpdateAniStateM()) {
                    aniStateM.update();
                }
                if (!aniStateM.curOverwriteBlock()) {
                    super.draw();
                }
                fixedDraw();
                aniStateM.drawBuilding();
            } else {
                super.draw();
                fixedDraw();
            }
        }
    }
}