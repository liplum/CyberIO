package net.liplum.persistance

fun < T : IRWable> T.toReader(): IHowToRead<T> =
    IHowToRead<T> { reads ->
        this.read(reads)
        this
    }

fun <T : IRWable> T.toWriter(): IHowToWrite<T> =
    IHowToWrite<T> { writes, _ ->
        this.write(writes)
    }

