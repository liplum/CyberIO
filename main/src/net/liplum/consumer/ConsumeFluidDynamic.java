package net.liplum.consumer;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import net.liplum.render.FluidImage;
import net.liplum.mixin.InventoryHKt;

public class ConsumeFluidDynamic extends Consume {
    public final Func<Building, LiquidStack[]> fluids;

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeFluidDynamic(Func<T, LiquidStack[]> fluids) {
        this.fluids = (Func<Building, LiquidStack[]>) fluids;
    }

    @Override
    public void apply(Block block) {
        block.hasLiquids = true;
    }

    @Override
    public void update(Building build) {
        LiquidStack[] fluids = this.fluids.get(build);
        InventoryHKt.remove(build.liquids, fluids, build.edelta());
    }

    @Override
    public void build(Building build, Table table) {
        final LiquidStack[][] current = {fluids.get(build)};

        table.table(cont -> {
            table.update(() -> {
                LiquidStack[] newFluids = fluids.get(build);
                if (current[0] != newFluids) {
                    rebuild(build, cont);
                    current[0] = newFluids;
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building tile, Table table) {
        table.clear();
        int i = 0;

        LiquidStack[] fluids = this.fluids.get(tile);
        for (LiquidStack stack : fluids) {
            table.add(new ReqImage(new FluidImage(stack.liquid.uiIcon),
                () -> tile.liquids != null && tile.liquids.get(stack.liquid) >= stack.amount)).padRight(8).left();
            if (++i % 4 == 0) table.row();
        }
    }

    @Override
    public float efficiency(Building build) {
        LiquidStack[] fluids = this.fluids.get(build);
        return build.consumeTriggerValid() || InventoryHKt.has(build.liquids, fluids) ? 1f : 0f;
    }
}
