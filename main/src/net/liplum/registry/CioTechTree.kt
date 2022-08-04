package net.liplum.registry

import mindustry.content.Items.oxide
import mindustry.content.Items.thorium
import mindustry.content.Liquids.cryofluid
import mindustry.content.Liquids.slag
import net.liplum.ErekirSpec
import net.liplum.Meta
import net.liplum.VanillaSpec
import net.liplum.mdt.CreateTechTree
import net.liplum.registry.CioBlocks.TMTRAINER
import net.liplum.registry.CioBlocks.cyberionMixer
import net.liplum.registry.CioBlocks.deleter
import net.liplum.registry.CioBlocks.ear
import net.liplum.registry.CioBlocks.eye
import net.liplum.registry.CioBlocks.heimdall
import net.liplum.registry.CioBlocks.holoProjector
import net.liplum.registry.CioBlocks.holoWall
import net.liplum.registry.CioBlocks.holoWallLarge
import net.liplum.registry.CioBlocks.icAssembler
import net.liplum.registry.CioBlocks.jammer
import net.liplum.registry.CioBlocks.p2pNode
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
import net.liplum.registry.CioBlocks.zipBomb
import net.liplum.registry.CioItems.ic
import net.liplum.registry.CioUnitTypes.holoArchitect
import net.liplum.registry.CioUnitTypes.holoFighter
import net.liplum.registry.CioUnitTypes.holoGuardian
import net.liplum.registry.CioUnitTypes.holoMiner
import net.liplum.registry.CioUnitTypes.holoSupporter

object CioTechTree {
    fun loadAll() {
        VanillaSpec {
            loadSerpulo()
        }
        ErekirSpec {
            loadErekir()
        }
    }

    fun loadSerpulo() {
        CreateTechTree(ic, Meta.Name) {
            node(icAssembler) {
                node(cyberionMixer, cryofluid, thorium) {
                    node(stealth) {
                        node(jammer)
                    }
                    node(holoProjector) {
                        node(holoMiner) {
                            node(holoSupporter)
                            node(holoArchitect)
                        }
                        node(holoGuardian) {
                            node(holoFighter)
                        }
                    }
                }
            }
            node(zipBomb)
            node(heimdall) {
                node(ear)
                node(eye)
            }
            node(TMTRAINER) {
                node(deleter)
                node(prism) {
                    node(prismObelisk)
                }
            }
            node(holoWall) {
                node(holoWallLarge)
            }
            node(sender) {
                node(receiver) {
                    node(smartUnloader)
                    node(smartDistributor)
                }
            }
            node(p2pNode) {
                node(streamClient)
                node(streamHost, streamClient)
                node(streamServer, streamClient)
            }
            node(wirelessTower) {
                node(underdriveProjector)
            }
        }
    }

    fun loadErekir() {
        CreateTechTree(ic, Meta.Name) {
            node(icAssembler) {
                node(cyberionMixer, slag, oxide) {
                    node(stealth) {
                        node(jammer)
                    }
                    node(holoProjector) {
                        node(holoFighter)
                        node(holoSupporter)
                        node(holoArchitect)
                        node(holoMiner)
                        node(holoGuardian)
                    }
                }
            }
            node(heimdall) {
                node(ear)
                node(eye)
            }
            node(zipBomb) {
                node(TMTRAINER)
                node(deleter)
                node(prism) {
                    node(prismObelisk)
                }
            }
            node(holoWall) {
                node(holoWallLarge)
            }
            node(sender) {
                node(receiver)
                node(smartDistributor) {
                    node(smartUnloader)
                }
            }
            node(p2pNode) {
                node(streamHost)
                node(streamClient)
                node(streamServer, streamClient, streamHost)
            }
            node(underdriveProjector) {
                node(wirelessTower)
            }
        }
    }
}