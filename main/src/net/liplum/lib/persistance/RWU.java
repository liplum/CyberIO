package net.liplum.lib.persistance;

import arc.struct.IntMap;
import arc.struct.ObjectSet;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import net.liplum.lib.math.Polar;
import org.jetbrains.annotations.NotNull;

public class RWU {
    public static void writeIntSeq(Writes writes, Seq<Integer> intSeq) {
        writes.i(intSeq.size);
        for (int i : intSeq) {
            writes.i(i);
        }
    }

    public static <T extends Building> void writeBuildingSeq(Writes writes, Seq<T> buildingSeq) {
        writes.i(buildingSeq.size);
        for (Building b : buildingSeq) {
            writes.i(b.pos());
        }
    }

    public static void writeShortSeq(Writes writes, Seq<Byte> shortSeq) {
        writes.i(shortSeq.size);
        for (int b : shortSeq) {
            writes.s(b);
        }
    }

    @NotNull
    public static Seq<Integer> readIntSeq(Reads reads) {
        int length = reads.i();
        Seq<Integer> intSeq = new Seq<>(length);
        for (int i = 0; i < length; i++) {
            intSeq.add(reads.i());
        }
        return intSeq;
    }

    @NotNull
    public static Seq<Short> readShortSeq(Reads reads) {
        int length = reads.i();
        Seq<Short> byteSeq = new Seq<>(length);
        for (int i = 0; i < length; i++) {
            byteSeq.add(reads.s());
        }
        return byteSeq;
    }

    public static void writeIntSet(Writes writes, ObjectSet<Integer> intObjectSet) {
        writes.i(intObjectSet.size);
        for (int i : intObjectSet) {
            writes.i(i);
        }
    }

    public static void writeShortSet(Writes writes, ObjectSet<Byte> shortObjectSet) {
        writes.i(shortObjectSet.size);
        for (int b : shortObjectSet) {
            writes.s(b);
        }
    }

    @NotNull
    public static OrderedSet<Integer> readIntSet(Reads reads) {
        int length = reads.i();
        OrderedSet<Integer> intObjectSet = new OrderedSet<>(length);
        for (int i = 0; i < length; i++) {
            intObjectSet.add(reads.i());
        }
        return intObjectSet;
    }

    @NotNull
    public static OrderedSet<Short> readShortSet(Reads reads) {
        int length = reads.i();
        OrderedSet<Short> byteObjectSet = new OrderedSet<>(length);
        for (int i = 0; i < length; i++) {
            byteObjectSet.add(reads.s());
        }
        return byteObjectSet;
    }

    public static void writePolarPos(Writes writes, Polar pos) {
        writes.f(pos.r);
        writes.f(pos.a);
    }

    @NotNull
    public static Polar readPolarPos(Reads reads) {
        return new Polar(reads.f(), reads.f());
    }

    public static <T> void writeSeq(Writes writes, Seq<T> seq, IHowToWrite<T> howToWrite) {
        writes.i(seq.size);
        for (T data : seq) {
            howToWrite.write(writes, data);
        }
    }

    @NotNull
    public static <T> Seq<T> readSeq(Reads reads, IHowToRead<T> howToRead) {
        int length = reads.i();
        Seq<T> seq = new Seq<>(length);
        for (int i = 0; i < length; i++) {
            try {
                T read = howToRead.read(reads);
                seq.add(read);
            } catch (Exception ignored) {

            }
        }
        return seq;
    }

    public static <T> void writeIntMap(Writes writes, IntMap<T> map, IWriteIntMap<T> howToWrite) {
        writes.i(map.size);
        for (T data : map.values()) {
            writes.i(howToWrite.write(data));
        }
    }

    @NotNull
    public static <T> IntMap<T> readIntMap(Reads reads, IReadIntMap<T> howToRead) {
        int length = reads.i();
        IntMap<T> map = new IntMap<>(length);
        for (int i = 0; i < length; i++) {
            try {
                int key = reads.i();
                T value = howToRead.read(key);
                map.put(key, value);
            } catch (Exception ignored) {

            }
        }
        return map;
    }
}
