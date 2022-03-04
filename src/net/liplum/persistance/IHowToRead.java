package net.liplum.persistance;

import arc.util.io.Reads;

public interface IHowToRead<T> {
    T read(Reads reads);
}
