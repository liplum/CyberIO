package net.liplum.api.cyber;

import arc.struct.IntSeq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import net.liplum.data.DataNetwork;

public class NetworkModule extends BlockModule {
    public DataNetwork graph = new DataNetwork();
    public IntSeq links = new IntSeq();

    @Override
    public void write(Writes write) {

    }

    @Override
    public void read(Reads read) {

    }
}
