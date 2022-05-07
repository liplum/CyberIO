package net.liplum.registries

import mindustry.content.Blocks.*
import mindustry.content.Items.silicon
import mindustry.content.Liquids.cryofluid
import mindustry.content.TechTree
import net.liplum.lib.withContext
import net.liplum.registries.CioBlocks.TMTRAINER
import net.liplum.registries.CioBlocks.cyberionMixer
import net.liplum.registries.CioBlocks.deleter
import net.liplum.registries.CioBlocks.ear
import net.liplum.registries.CioBlocks.eye
import net.liplum.registries.CioBlocks.heimdall
import net.liplum.registries.CioBlocks.holoProjector
import net.liplum.registries.CioBlocks.holoWall
import net.liplum.registries.CioBlocks.holoWallLarge
import net.liplum.registries.CioBlocks.icMachine
import net.liplum.registries.CioBlocks.icMachineSmall
import net.liplum.registries.CioBlocks.jammer
import net.liplum.registries.CioBlocks.prism
import net.liplum.registries.CioBlocks.prismObelisk
import net.liplum.registries.CioBlocks.receiver
import net.liplum.registries.CioBlocks.sender
import net.liplum.registries.CioBlocks.smartDistributor
import net.liplum.registries.CioBlocks.smartUnloader
import net.liplum.registries.CioBlocks.stealth
import net.liplum.registries.CioBlocks.streamClient
import net.liplum.registries.CioBlocks.streamHost
import net.liplum.registries.CioBlocks.streamServer
import net.liplum.registries.CioBlocks.underdriveProjector
import net.liplum.registries.CioBlocks.wirelessTower
import net.liplum.registries.CioItems.ic
import net.liplum.registries.CioLiquids.cyberion
import net.liplum.registries.CioUnitTypes.holoArchitect
import net.liplum.registries.CioUnitTypes.holoFighter
import net.liplum.registries.CioUnitTypes.holoGuardian
import net.liplum.registries.CioUnitTypes.holoMiner
import net.liplum.registries.CioUnitTypes.holoSupporter

object CioTechTree {
    fun loadSerpulo() {
        TechTree.all.withContext {
            at(silicon).sub(ic)
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
                sub(receiver) {
                    sub(smartUnloader, icMachine)
                    sub(smartDistributor, icMachine)
                }
                sub(streamClient) {
                    sub(streamHost, icMachine) {
                        sub(streamServer)
                    }
                }
            }
            at(powerNodeLarge).sub(wirelessTower, icMachine)
            at(differentialGenerator).sub(underdriveProjector)
            at(coreShard).sub(cyberionMixer, icMachine) {
                sub(stealth, streamHost)
                sub(holoProjector) {
                    sub(holoMiner) {
                        sub(holoSupporter) {
                            sub(holoArchitect)
                        }
                    }
                    sub(holoGuardian) {
                        sub(holoFighter)
                    }
                }
            }
        }
    }
}