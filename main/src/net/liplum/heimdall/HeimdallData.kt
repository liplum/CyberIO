package net.liplum.heimdall

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.persistence.IRWable
import net.liplum.common.TimeH
import plumy.core.math.Progress

internal typealias ResourceID = Int

class HeimdallData {
    var hud = HeimdallHUD()
    var inventory = Inventory()
    var allMiningTask = HashMap<ResourceID, MiningTask>()
}

class Inventory {
    var maxCount = 1000
    /**
     * Index: [ResourceMeta.id]
     */
    var items = IntArray(0)
    fun progress(index: ResourceID) =
        (items[index] / maxCount.toFloat()).coerceIn(0f, 1f)
}

class HeimdallHUD {
    var health = 1000
    var maxHealth = 1000
    val progress: Progress
        get() = (health / maxHealth.toFloat()).coerceIn(0f, 1f)
}

class HeimdallOp(
    val data: HeimdallData,
) {
    fun addMining(
        mineralID: ResourceID,
        timeReq: Int,
    ) {
        data.allMiningTask[mineralID] = MiningTask().apply {
            this.mineralId = mineralID
            this.totalTime = timeReq
            this.startTimeStamp = TimeH.curTimeSec()
        }
    }

    fun getMiningTaskByID(id: ResourceID): MiningTask? =
        data.allMiningTask[id]
}

class MiningTask : IRWable {
    var mineralId: ResourceID = 0
    var totalTime = 0
    /** Unit:sec */
    var startTimeStamp = 0L
    val isFinished: Boolean
        get() = startTimeStamp + totalTime <= TimeH.curTimeSec()
    val restTime: Int
        get() = ((startTimeStamp + totalTime) - TimeH.curTimeSec()).toInt()
    val progress: Progress
        get() = ((TimeH.curTimeSec() - startTimeStamp).toFloat() / totalTime)
            .coerceIn(0f, 1f)

    override fun read(reader: Reads) {
        mineralId = reader.i()
        totalTime = reader.i()
        startTimeStamp = reader.l()
    }

    override fun write(writer: Writes) {
        writer.i(mineralId)
        writer.i(totalTime)
        writer.l(startTimeStamp)
    }
}