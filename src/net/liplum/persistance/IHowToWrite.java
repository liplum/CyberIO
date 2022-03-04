package net.liplum.persistance;

import arc.util.io.Writes;

public interface IHowToWrite<T> {
    void write(Writes write,T data);
}
