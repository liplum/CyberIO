package net.liplum.blocks.prism;

import arc.graphics.g2d.TextureRegion;
import arc.util.io.Reads;
import arc.util.io.Writes;
import net.liplum.ClientOnly;
import net.liplum.math.PolarPos;
import net.liplum.persistance.RWU;
import net.liplum.utils.ByteU;

public class Crystal {
    public PolarPos revolution;
    public PolarPos rotation;
    @ClientOnly
    public TextureRegion img;
    private static final int ClockwisePos = 0;
    private static final int RemovedPos = 1;
    private static final int AwaitAddingPos = 2;
    private static final int Pos4 = 3;
    private static final int Pos5 = 4;
    private static final int Pos6 = 5;
    private static final int Pos7 = 6;
    private static final int Pos8 = 7;

    public int orbitPos = 0;
    public int data = 0;

    public boolean isClockwise() {
        return ByteU.isOn(data, ClockwisePos);
    }

    public void setClockwise(boolean clockwise) {
        if (clockwise)
            data = ByteU.on(data, ClockwisePos);
        else
            data = ByteU.off(data, ClockwisePos);
    }

    public boolean isRemoved() {
        return ByteU.isOn(data, RemovedPos);
    }

    public void setRemoved(boolean removed) {
        if (removed) {
            data = ByteU.on(data, RemovedPos);
            setAwaitAdding(false);
        } else
            data = ByteU.off(data, RemovedPos);
    }

    public boolean isAwaitAdding() {
        return ByteU.isOn(data, AwaitAddingPos);
    }

    public void setAwaitAdding(boolean awaitAdding) {
        if (awaitAdding) {
            data = ByteU.on(data, AwaitAddingPos);
            setRemoved(false);
        } else
            data = ByteU.off(data, AwaitAddingPos);
    }

    public static void write(Writes writes, Crystal crystal) {
        writes.b(crystal.orbitPos);
        RWU.writePolarPos(writes, crystal.revolution);
        RWU.writePolarPos(writes, crystal.rotation);
        writes.b(crystal.data);
    }

    public static Crystal read(Reads reads) {
        Crystal crystal = new Crystal();
        crystal.orbitPos = reads.b();
        crystal.revolution = RWU.readPolarPos(reads);
        crystal.rotation = RWU.readPolarPos(reads);
        crystal.data = reads.b();
        return crystal;
    }
}
