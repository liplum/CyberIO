package net.liplum.lib.persistence;

import arc.util.io.Reads;
import arc.util.io.Writes;
import org.jetbrains.annotations.NotNull;

public interface IRWable {
    void read(@NotNull Reads reader);
    void read(@NotNull CacheReaderSpec reader);
    void write(@NotNull Writes writer);
    void write(@NotNull CacheWriter writer);
}
