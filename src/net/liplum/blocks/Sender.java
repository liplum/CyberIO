package net.liplum.blocks;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import net.liplum.api.IDataReceiver;
import net.liplum.api.IDataSender;

public class Sender extends Block {
    public Sender(String name) {
        super(name);
        solid = true;
        update = true;
        acceptsItems = true;
        configurable = true;
        group = BlockGroup.transportation;

        config(Integer.class, SenderBuild::setReceiverPackedPos);
    }

    public class SenderBuild extends Building implements IDataSender {
        private int receiverPackedPos = -1;

        public int getReceiverPackedPos() {
            return receiverPackedPos;
        }

        public void setReceiverPackedPos(int receiverPackedPos) {
            this.receiverPackedPos = receiverPackedPos;
        }

        @Override
        public void sendData(IDataReceiver receiver, Item item) {
            receiver.receiveData(this, item);
        }

        @Override
        public void handleItem(Building source, Item item) {
            Receiver.ReceiverBuild reb = this.receiverBuilding();
            if (reb != null) {
                this.sendData(reb, item);
            }
        }

        @Nullable
        public Receiver.ReceiverBuild receiverBuilding() {
            if (receiverPackedPos != -1) {
                Building rBuild = Vars.world.build(receiverPackedPos);
                if (rBuild instanceof Receiver.ReceiverBuild) {
                    return (Receiver.ReceiverBuild) rBuild;
                }
            }
            return null;
        }

        @Override
        public void drawConfigure() {
            float sin = Mathf.absin(Time.time, 6f, 1f);
            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(tile.drawx(), tile.drawy(), (tile.block().size / 2f + 1) * Vars.tilesize + sin - 2f, Pal.accent);
            Receiver.ReceiverBuild reb = this.receiverBuilding();
            if (reb != null) {
                Tile ret = reb.tile();
                Drawf.dashCircle(ret.drawx(), ret.drawy(),
                        (ret.block().size / 2f + 1) * Vars.tilesize + sin - 2f, Pal.accent);
            }
        }

        public void clearReceiver() {
            configure(-1);
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other) {
                this.clearReceiver();
                return false;
            }

            if (this.receiverPackedPos == other.pos()) {
                this.clearReceiver();
                return false;
            }
            if (other.block instanceof Receiver && other instanceof Receiver.ReceiverBuild) {
                this.setReceiver((Receiver) other.block, (Receiver.ReceiverBuild) other);
                return false;
            }
            return true;
        }

        public void setReceiver(Receiver block, Receiver.ReceiverBuild build) {
            this.receiverPackedPos = build.pos();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            Receiver.ReceiverBuild reb = this.receiverBuilding();
            if (reb != null) {
                return reb.acceptData(this, item);
            }
            return false;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(this.receiverPackedPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            this.receiverPackedPos = read.i();
        }
    }
}