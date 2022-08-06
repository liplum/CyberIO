package net.liplum.registry

import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.Liquids
import mindustry.content.UnitTypes
import mindustry.world.blocks.payloads.*
import mindustry.world.blocks.sandbox.ItemSource
import mindustry.world.blocks.sandbox.LiquidSource
import mindustry.world.blocks.sandbox.PowerSource
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.ExperimentalOnly
import net.liplum.annotations.DependOn

object CioOverwrite {
    @DependOn
    fun _overwriteVanilla() {
        DebugOnly {
            (Blocks.powerSource as PowerSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.itemSource as ItemSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.liquidSource as LiquidSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.payloadSource as PayloadSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            Blocks.powerVoid.buildVisibility = BuildVisibility.shown
            Blocks.itemVoid.buildVisibility = BuildVisibility.shown
            Blocks.liquidVoid.buildVisibility = BuildVisibility.shown
            Blocks.payloadVoid.buildVisibility = BuildVisibility.shown
            val blockSize = 10f
            (Blocks.payloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.payloadLoader as PayloadLoader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadRouter as PayloadRouter).payloadLimit = blockSize
            (Blocks.payloadUnloader as PayloadUnloader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadPropulsionTower as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.payloadMassDriver as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.reinforcedPayloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.reinforcedPayloadRouter as PayloadConveyor).payloadLimit = blockSize
            UnitTypes.evoke.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.incite.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.emanate.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            /*val coreBlock = Blocks.coreShard as CoreBlock
            coreBlock.unitType = CioUnitTypes.holoFighter
            coreBlock.solid = false*/
            Liquids.neoplasm.hidden = false
        }
        ExperimentalOnly {
            Blocks.conveyor.sync = true
            Blocks.titaniumConveyor.sync = true
            Blocks.armoredConveyor.sync = true
            Blocks.plastaniumConveyor.sync = true
        }
    }
}