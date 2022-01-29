package net.liplum.registries;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.graphics.Drawf;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import net.liplum.animations.AniConfig;
import net.liplum.animations.AniState;
import net.liplum.animations.IAnimated;
import net.liplum.blocks.AniedCrafter;
import net.liplum.blocks.Receiver;
import net.liplum.blocks.Sender;
import net.liplum.utils.AnimUtil;
import net.liplum.utils.AtlasUtil;

public class CioBlocks implements ContentList {
    public Block icMachine;
    public Block dataCenter;
    public Block receiver;
    public Block sender;

    @Override
    public void load() {
        icMachine = new AniedCrafter("ic-machine") {
            private AniState<AniedCrafter, AniedCrafterBuild> idleState;
            private AniState<AniedCrafter, AniedCrafterBuild> workingState;
            private IAnimated workingAnimation;
            private TextureRegion idleTR;

            {
                requirements(Category.crafting,
                        ItemStack.with(Items.copper, 1000,
                                Items.silicon, 200,
                                Items.graphite, 150,
                                Items.titanium, 250)
                );
                outputItem = new ItemStack(CioItems.ic, 1);
                craftTime = 1200f;
                size = 3;
                buildCostMultiplier = 10f;
                hasPower = true;
                hasItems = true;
                itemCapacity = 200;
                consumes.items(
                        //Total:200
                        new ItemStack(Items.silicon, 40),//20%
                        new ItemStack(Items.copper, 100),//50%
                        new ItemStack(Items.metaglass, 60)//30%
                );
                consumes.power(10f);
            }

            @Override
            public void genAnimState() {
                idleState = addAniState(new AniState<>("Idle", ((block, build) -> {
                    Draw.rect(idleTR, build.x, build.y);
                })));
                workingState = addAniState(new AniState<>("Working", ((block, build) -> {
                    workingAnimation.draw(build.x, build.y);
                    Drawf.light(build.team, build.x, build.y,
                            5f,
                            Color.white,
                            1f);
                })));
            }

            @Override
            public void genAniConfig() {
                aniConfig = new AniConfig<>();
                aniConfig.defaultState(idleState);
                aniConfig.enter(idleState, workingState, ((block, build) ->
                        !Mathf.zero(build.progress))
                );
                aniConfig.enter(workingState, idleState, ((block, build) ->
                        Mathf.zero(build.progress) || Mathf.zero(build.power.status))
                );
                aniConfig.build();
            }

            @Override
            public void load() {
                super.load();
                workingAnimation = AnimUtil.auto(this, "indicator-light", 7, 60f);
                idleTR = AtlasUtil.sub(this, "light-off");
            }
        };

        receiver = new Receiver("receiver") {{
            requirements(Category.distribution, new ItemStack[]{
                    new ItemStack(CioItems.ic, 1),
                    new ItemStack(Items.copper, 50),
                    new ItemStack(Items.graphite, 20),
                    new ItemStack(Items.metaglass, 20),
                    new ItemStack(Items.silicon, 10),
            });
            consumes.power(0.5f);
        }};

        sender = new Sender("sender") {{
            requirements(Category.distribution, new ItemStack[]{
                    new ItemStack(CioItems.ic, 1),
                    new ItemStack(Items.copper, 50),
                    new ItemStack(Items.graphite, 20),
                    new ItemStack(Items.metaglass, 20),
                    new ItemStack(Items.silicon, 10),
            });
            consumes.power(0.5f);
        }};
    }
}
