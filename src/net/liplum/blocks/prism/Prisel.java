package net.liplum.blocks.prism;

import arc.util.io.Reads;
import arc.util.io.Writes;
import net.liplum.math.PolarPos;
import net.liplum.persistance.RWU;
import net.liplum.utils.ByteU;

public class Prisel {
    public PolarPos revolution;
    public PolarPos rotation;
    private static final int ClockwisePos = 0;
    private static final int RemovedPos = 1;
    private static final int Pos3 = 2;
    private static final int Pos4 = 3;
    private static final int Pos5 = 4;
    private static final int Pos6 = 5;
    private static final int Pos7 = 6;
    private static final int Pos8 = 7;
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
        if (removed)
            data = ByteU.on(data, RemovedPos);
        else
            data = ByteU.off(data, RemovedPos);
    }

    public static void write(Writes writes, Prisel prisel) {
        RWU.writePolarPos(writes, prisel.revolution);
        RWU.writePolarPos(writes, prisel.rotation);
        writes.b(prisel.data);
    }

    public static Prisel read(Reads reads) {
        Prisel prisel = new Prisel();
        prisel.revolution = RWU.readPolarPos(reads);
        prisel.rotation = RWU.readPolarPos(reads);
        prisel.data = reads.b();
        return prisel;
    }
}
