package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.api.ITrigger;

public class AniStateM<TBlock extends Block, TBuild extends Building> {
    private final TBlock block;
    private final TBuild build;
    private final AniConfig<TBlock, TBuild> config;
    private AniState<TBlock, TBuild> curState;
    @Nullable
    private ISwitchAniStateListener<TBlock, TBuild> switchAniStateListener;
    @Nullable
    private Runnable onUpdate;

    public AniStateM(AniConfig<TBlock, TBuild> config, TBlock block, TBuild build) {
        this.config = config;
        this.curState = config.getDefaultState();
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

    public AniState<TBlock, TBuild> getCurState() {
        return curState;
    }

    public void setCurState(AniState<TBlock, TBuild> curState) {
        this.curState = curState;
    }

    public void drawBuilding() {
        curState.drawBuilding(this.block, this.build);
    }

    public boolean curOverwriteBlock() {
        return curState != null && curState.isOverwriteBlock();
    }

    public void update() {
        if (onUpdate != null) {
            onUpdate.run();
        }
        for (AniState<TBlock, TBuild> to : config.getAllEntrances(curState)) {
            ITrigger<TBlock, TBuild> canEnter = config.getCanEnter(curState, to);
            if (canEnter != null && canEnter.canTrigger(this.block, this.build)) {
                if (switchAniStateListener != null) {
                    switchAniStateListener.onSwitch(this.block, this.build, curState, to);
                }
                setCurState(to);
                return;
            }
        }
    }
}
