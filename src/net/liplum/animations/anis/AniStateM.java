package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.api.ITrigger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AniStateM<TBlock extends Block, TBuild extends Building> {
    private final TBlock block;
    private final TBuild build;
    private final AniConfig<TBlock, TBuild> config;
    @NotNull
    private AniState<TBlock, TBuild> curState;
    @Nullable
    private ISwitchAniStateListener<TBlock, TBuild> switchAniStateListener;
    @Nullable
    private Runnable onUpdate;

    public AniStateM(AniConfig<TBlock, TBuild> config, TBlock block, TBuild build) {
        this.config = config;
        this.curState = Objects.requireNonNull(config.getDefaultState());
        this.block = block;
        this.build = build;
    }

    public ISwitchAniStateListener<TBlock, TBuild> getSwitchAniStateListener() {
        return switchAniStateListener;
    }

    public void setSwitchAniStateListener(ISwitchAniStateListener<TBlock, TBuild> switchAniStateListener) {
        this.switchAniStateListener = switchAniStateListener;
    }

    public Runnable getOnUpdate() {
        return onUpdate;
    }

    public AniStateM<TBlock, TBuild> onUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    @NotNull
    public AniState<TBlock, TBuild> getCurState() {
        return curState;
    }

    public void setCurState(@NotNull AniState<TBlock, TBuild> curState) {
        this.curState = curState;
    }

    public void drawBuilding() {
        curState.drawBuilding(this.build);
    }

    public boolean curOverwriteBlock() {
        return curState.isOverwriteBlock();
    }

    public void update() {
        if (onUpdate != null) {
            onUpdate.run();
        }
        for (AniState<TBlock, TBuild> to : config.getAllEntrances(curState)) {
            ITrigger<TBuild> canEnter = config.getCanEnter(curState, to);
            if (canEnter != null && canEnter.canTrigger(this.build)) {
                if (switchAniStateListener != null) {
                    switchAniStateListener.onSwitch(this.block, this.build, curState, to);
                }
                setCurState(to);
                return;
            }
        }
    }
}
