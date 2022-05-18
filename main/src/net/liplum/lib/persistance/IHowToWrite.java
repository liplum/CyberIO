package net.liplum.lib.persistance;

import arc.util.io.Writes;
import org.jetbrains.annotations.NotNull;

public interface IHowToWrite<T> {
    void write(@NotNull Writes write, T value);
}
