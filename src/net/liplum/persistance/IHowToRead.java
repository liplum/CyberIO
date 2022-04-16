package net.liplum.persistance;

import arc.util.io.Reads;
import org.jetbrains.annotations.NotNull;

public interface IHowToRead<T> {
    T read(@NotNull Reads reads);
}
