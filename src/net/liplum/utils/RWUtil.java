package net.liplum.utils;

import arc.struct.ObjectSet;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;

public class RWUtil {
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

    public static Seq<Integer> readIntSeq(Reads reads) {
        int length = reads.i();
        Seq<Integer> intSeq = new Seq<>(length);
        for (int i = 0; i < length; i++) {
            intSeq.add(reads.i());
        }
        return intSeq;
    }

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

    public static OrderedSet<Integer> readIntSet(Reads reads) {
        int length = reads.i();
        OrderedSet<Integer> intObjectSet = new OrderedSet<>(length);
        for (int i = 0; i < length; i++) {
            intObjectSet.add(reads.i());
        }
        return intObjectSet;
    }

    public static OrderedSet<Short> readShortSet(Reads reads) {
        int length = reads.i();
        OrderedSet<Short> byteObjectSet = new OrderedSet<>(length);
        for (int i = 0; i < length; i++) {
            byteObjectSet.add(reads.s());
        }
        return byteObjectSet;
    }
}
