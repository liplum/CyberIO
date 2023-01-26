package net.liplum.registry

import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.UnitTypes
import mindustry.world.blocks.heat.HeatProducer
import mindustry.world.blocks.payloads.*
import mindustry.world.blocks.sandbox.*
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.ExperimentalOnly
import net.liplum.annotations.DependOn

object CioOverwrite {
    @DependOn
    fun debugOnly() {
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
            (Blocks.heatSource as HeatProducer).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.powerVoid as PowerVoid).apply {
                buildVisibility = BuildVisibility.shown
            }
            (Blocks.itemVoid as ItemVoid).apply {
                buildVisibility = BuildVisibility.shown
            }
            (Blocks.liquidVoid as LiquidVoid).apply {
                buildVisibility = BuildVisibility.shown
            }
            (Blocks.payloadVoid as PayloadVoid).apply {
                buildVisibility = BuildVisibility.shown
            }
            val blockSize = 10f
            (Blocks.payloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.payloadLoader as PayloadLoader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadRouter as PayloadRouter).payloadLimit = blockSize
            (Blocks.payloadUnloader as PayloadUnloader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadMassDriver as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.reinforcedPayloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.reinforcedPayloadRouter as PayloadConveyor).payloadLimit = blockSize
            UnitTypes.evoke.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.incite.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.emanate.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            /*val coreBlock = Blocks.coreShard as CoreBlock
            coreBlock.unitType = CioUnitType.holoFighter
            coreBlock.solid = false*/
        }
        ExperimentalOnly {
            Blocks.conveyor.sync = true
            Blocks.titaniumConveyor.sync = true
            Blocks.armoredConveyor.sync = true
            Blocks.plastaniumConveyor.sync = true
            Blocks.duct.sync = true
            Blocks.armoredDuct.sync = true
        }
    }
}