package net.liplum.blocks.prism;

import arc.util.io.Reads;
import arc.util.io.Writes;
import net.liplum.math.PolarPos;
import net.liplum.persistance.RWU;

public class Prisel {
    public PolarPos revolution;
    public PolarPos rotation;
    public boolean isClockwise;

    public static void write(Writes writes, Prisel prisel) {
        RWU.writePolarPos(writes, prisel.revolution);
        RWU.writePolarPos(writes, prisel.rotation);
        writes.bool(prisel.isClockwise);
    }

    public static Prisel read(Reads reads) {
        Prisel prisel = new Prisel();
        prisel.revolution = RWU.readPolarPos(reads);
        prisel.rotation = RWU.readPolarPos(reads);
        prisel.isClockwise = reads.bool();
        return prisel;
    }
}
