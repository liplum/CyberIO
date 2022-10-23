package net.liplum.mixin

import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Player
import mindustry.gen.Syncc
import java.nio.FloatBuffer

interface SimpleSyncMixin : Syncc {
    var lastUpdate: Long
    var updateSpacing: Long
    override fun lastUpdated(): Long =
        lastUpdate

    override fun lastUpdated(timestamp: Long) {
        lastUpdate = timestamp
    }

    override fun updateSpacing(): Long =
        updateSpacing

    override fun updateSpacing(spacing: Long) {
        updateSpacing
    }

    override fun interpolate() {
    }

    override fun readSyncManual(buffer: FloatBuffer) {
    }

    override fun isSyncHidden(player: Player): Boolean =
        false

    override fun afterSync() {
    }

    override fun handleSyncHidden() {
    }

    override fun readSync(reader: Reads) {
    }

    override fun snapInterpolation() {
    }

    override fun snapSync() {
    }

    override fun writeSync(writer: Writes) {
    }

    override fun writeSyncManual(buffer: FloatBuffer) {
    }
}