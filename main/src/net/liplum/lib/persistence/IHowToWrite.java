package net.liplum.lib.persistence;

import arc.util.io.Writes;
import org.jetbrains.annotations.NotNull;

public interface IHowToWrite<T> {
    void write(@NotNull Writes write, T value);
}
