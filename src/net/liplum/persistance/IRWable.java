package net.liplum.persistance;

import arc.util.io.Reads;
import arc.util.io.Writes;
import org.jetbrains.annotations.NotNull;

public interface IRWable {
    void read(@NotNull Reads reader);

    void write(@NotNull Writes writer);
}
