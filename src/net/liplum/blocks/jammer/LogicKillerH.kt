package net.liplum.blocks.jammer

import mindustry.gen.Building
import mindustry.world.blocks.logic.LogicBlock
import mindustry.world.blocks.logic.MemoryBlock
import mindustry.world.blocks.logic.MessageBlock

fun Building.destroyLogic() {
    when (this) {
        is LogicBlock.LogicBuild -> updateCode("")
        is MessageBlock.MessageBuild -> configure("")
        is MemoryBlock.MemoryBuild -> {
            for (i in memory.indices)
                memory[i] = 0.0
        }
        //Destroying a LogicDisplay isn't fun.
    }
}