package net.liplum.animations;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

public class AniStateM<TBlock extends Block, TBuild extends Building> {
    private final TBlock block;
    private final TBuild build;
    private final AniConfig<TBlock, TBuild> config;
    private AniState<TBlock, TBuild> curState;
    @Nullable
    private ISwitchAniStateListener<TBlock, TBuild> switchAniStateListener;

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

    public AniState<TBlock, TBuild> getCurState() {
        return curState;
    }

    public void setCurState(AniState<TBlock, TBuild> curState) {
        this.curState = curState;
    }

    public void drawBuilding() {
        curState.drawBuilding(this.block, this.build);
    }

    public void update() {
        for (AniState<TBlock, TBuild> to : config.getAllEntrances(curState)) {
            ITrigger<TBlock, TBuild> canEnter = config.getCanEnter(curState, to);
            if (canEnter != null && canEnter.canTrigger(this.block, this.build)) {
                if (switchAniStateListener != null) {
                    switchAniStateListener.OnSwitch(this.block, this.build, curState, to);
                }
                setCurState(to);
                return;
            }
        }
    }
}
