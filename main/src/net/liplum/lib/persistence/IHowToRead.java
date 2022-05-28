package net.liplum.lib.persistence;

import arc.util.io.Reads;
import org.jetbrains.annotations.NotNull;

public interface IHowToRead<T> {
    T read(@NotNull Reads reads);
}
