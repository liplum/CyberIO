package net.liplum.registry

import mindustry.content.Blocks.*
import mindustry.content.Items.silicon
import mindustry.content.Liquids.cryofluid
import mindustry.content.TechTree
import net.liplum.mdt.withContext
import net.liplum.registry.CioBlocks.TMTRAINER
import net.liplum.registry.CioBlocks.cyberionMixer
import net.liplum.registry.CioBlocks.deleter
import net.liplum.registry.CioBlocks.ear
import net.liplum.registry.CioBlocks.eye
import net.liplum.registry.CioBlocks.heimdall
import net.liplum.registry.CioBlocks.holoProjector
import net.liplum.registry.CioBlocks.holoWall
import net.liplum.registry.CioBlocks.holoWallLarge
import net.liplum.registry.CioBlocks.icMachine
import net.liplum.registry.CioBlocks.icMachineSmall
import net.liplum.registry.CioBlocks.jammer
import net.liplum.registry.CioBlocks.prism
import net.liplum.registry.CioBlocks.prismObelisk
import net.liplum.registry.CioBlocks.receiver
import net.liplum.registry.CioBlocks.sender
import net.liplum.registry.CioBlocks.smartDistributor
import net.liplum.registry.CioBlocks.smartUnloader
import net.liplum.registry.CioBlocks.stealth
import net.liplum.registry.CioBlocks.streamClient
import net.liplum.registry.CioBlocks.streamHost
import net.liplum.registry.CioBlocks.streamServer
import net.liplum.registry.CioBlocks.underdriveProjector
import net.liplum.registry.CioBlocks.wirelessTower
import net.liplum.registry.CioItems.ic
import net.liplum.registry.CioFluids.cyberion
import net.liplum.registry.CioUnitTypes.holoArchitect
import net.liplum.registry.CioUnitTypes.holoFighter
import net.liplum.registry.CioUnitTypes.holoGuardian
import net.liplum.registry.CioUnitTypes.holoMiner
import net.liplum.registry.CioUnitTypes.holoSupporter

object CioTechTree {
    fun loadAll() {
        loadSerpulo()
    }

    fun loadSerpulo() {
        TechTree.all.withContext {
            at(silicon).sub(ic, icMachineSmall)
            at(cryofluid).sub(cyberion)
            at(salvo).sub(TMTRAINER, icMachine) {
                sub(ear)
                sub(heimdall, eye, ear)
                sub(eye)
            }
            at(parallax).sub(deleter, icMachine) {
                sub(jammer)
            }
            at(coreShard).sub(icMachineSmall) {
                sub(icMachine) {
                    sub(prism) {
                        sub(prismObelisk)
                    }
                }
                sub(holoWall) {
                    sub(holoWallLarge)
                }
            }
            at(coreShard).sub(sender, icMachineSmall) {
                sub(receiver, sender, overwriteReq = true) {
                    sub(smartUnloader, icMachine)
                    sub(smartDistributor, icMachine)
                }
                sub(streamClient) {
                    sub(streamHost, icMachine, streamClient, overwriteReq = true) {
                        sub(streamServer)
                    }
                }
            }
            at(powerNodeLarge).sub(wirelessTower, icMachine)
            at(differentialGenerator).sub(underdriveProjector)
            at(coreShard).sub(cyberionMixer, icMachine) {
                sub(stealth, streamHost)
                sub(holoProjector, cyberion) {
                    sub(holoMiner, holoProjector, overwriteReq = true) {
                        sub(holoSupporter) {
                            sub(holoArchitect)
                        }
                    }
                    sub(holoGuardian, holoProjector, overwriteReq = true) {
                        sub(holoFighter)
                    }
                }
            }
        }
    }
}