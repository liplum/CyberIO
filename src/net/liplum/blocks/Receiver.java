package net.liplum.blocks;

import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import net.liplum.api.IDataReceiver;
import net.liplum.api.IDataSender;

import static mindustry.Vars.content;

public class Receiver extends Block {

    public Receiver(String name) {
        super(name);
        hasItems = true;
        update = true;
        solid = true;
        itemCapacity = 10;
        group = BlockGroup.transportation;
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        acceptsItems = false;

        config(Item.class, ReceiverBuild::setOutputItem);
        configClear((ReceiverBuild tile) -> tile.setOutputItem(null));
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.remove("items");
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list) {
        drawRequestConfigCenter(req, req.config, "center", true);
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class ReceiverBuild extends Building implements IDataReceiver {
        private Item outputItem;

        @Nullable
        public Item getOutputItem() {
            return outputItem;
        }

        public void setOutputItem(@Nullable Item item) {
            this.outputItem = item;
        }

        @Override
        public void updateTile() {
            if (outputItem != null) {
                dump(outputItem);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(table, content.items(), this::getOutputItem, this::configure);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other) {
                deselect();
                configure(null);
                return false;
            }
            return true;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        public boolean acceptData(IDataSender source, Item item) {
            return items.get(item) < getMaximumAccepted(item) &&
                    getOutputItem() == item;
        }

        @Override
        public void receiveData(IDataSender sender, Item item) {
            this.items.add(item, 1);
        }

        @Override
        public Item config() {
            return outputItem;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(outputItem == null ? -1 : outputItem.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            outputItem = content.item(read.s());
        }
    }
}
