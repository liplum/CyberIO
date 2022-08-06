package net.liplum.registry

import mindustry.content.Items.oxide
import mindustry.content.Items.thorium
import mindustry.content.Liquids.cryofluid
import mindustry.content.Liquids.slag
import net.liplum.ErekirSpec
import net.liplum.Meta
import net.liplum.VanillaSpec
import net.liplum.mdt.CreateTechTree
import net.liplum.registry.CioBlock.TMTRAINER
import net.liplum.registry.CioBlock.cyberionMixer
import net.liplum.registry.CioBlock.deleter
import net.liplum.registry.CioBlock.holoWall
import net.liplum.registry.CioBlock.holoWallLarge
import net.liplum.registry.CioBlock.icAssembler
import net.liplum.registry.CioBlock.jammer
import net.liplum.registry.CioBlock.prism
import net.liplum.registry.CioBlock.prismObelisk
import net.liplum.registry.CioBlock.stealth
import net.liplum.registry.CioBlock.underdriveProjector
import net.liplum.registry.CioBlock.wirelessTower
import net.liplum.registry.CioBlock.zipBomb
import net.liplum.registry.CioCyber.p2pNode
import net.liplum.registry.CioCyber.receiver
import net.liplum.registry.CioCyber.sender
import net.liplum.registry.CioCyber.smartDistributor
import net.liplum.registry.CioCyber.smartUnloader
import net.liplum.registry.CioCyber.streamClient
import net.liplum.registry.CioCyber.streamHost
import net.liplum.registry.CioCyber.streamServer
import net.liplum.registry.CioHeimdall.ear
import net.liplum.registry.CioHeimdall.eye
import net.liplum.registry.CioHeimdall.heimdall
import net.liplum.registry.CioHoloUnit.holoArchitect
import net.liplum.registry.CioHoloUnit.holoFighter
import net.liplum.registry.CioHoloUnit.holoGuardian
import net.liplum.registry.CioHoloUnit.holoMiner
import net.liplum.registry.CioHoloUnit.holoProjector
import net.liplum.registry.CioHoloUnit.holoSupporter
import net.liplum.registry.CioItem.ic

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
            node(eye) {
                node(ear) {
                    node(heimdall)
                }
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
            node(eye) {
                node(ear) {
                    node(heimdall)
                }
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