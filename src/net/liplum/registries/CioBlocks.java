package net.liplum.registries;

import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import net.liplum.blocks.Receiver;
import net.liplum.blocks.Sender;

public class CioBlocks implements ContentList {
    public Block icMachine;
    public Block dataCenter;
    public Block receiver;
    public Block sender;

    @Override
    public void load() {
        icMachine = new GenericCrafter("ic-machine") {{
            requirements(Category.crafting,
                    ItemStack.with(Items.copper, 200,
                            Items.silicon, 100,
                            Items.titanium, 200)
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
        }};

        receiver = new Receiver("receiver") {{
            requirements(Category.distribution, new ItemStack[]{
                    new ItemStack(CioItems.ic, 1),
                    new ItemStack(Items.copper, 10),
                    new ItemStack(Items.silicon, 5),
            });
            consumes.power(0.5f);
        }};

        sender = new Sender("sender") {{
            requirements(Category.distribution, new ItemStack[]{
                    new ItemStack(CioItems.ic, 1),
                    new ItemStack(Items.copper, 10),
                    new ItemStack(Items.silicon, 5),
            });
            consumes.power(0.5f);
        }};
    }
}
