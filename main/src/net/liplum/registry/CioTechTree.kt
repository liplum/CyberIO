package net.liplum.registry

import mindustry.content.Items.oxide
import mindustry.content.Items.thorium
import mindustry.content.Liquids.cryofluid
import mindustry.content.Liquids.slag
import mindustry.content.Planets
import net.liplum.ErekirSpec
import net.liplum.Meta
import net.liplum.VanillaSpec
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
import plumy.dsl.CreateTechTree

object CioTechTree {
    fun loadAll() {
        VanillaSpec {
            loadSerpulo()
        }
        ErekirSpec {
            loadErekir()
        }
    }

    private fun loadSerpulo() {
        CreateTechTree(name = Meta.Name, origin = ic, planet = Planets.serpulo) {
            icAssembler {
                cyberionMixer(require = listOf(cryofluid, thorium)) {
                    stealth {
                        jammer {}
                    }
                    holoProjector {
                        holoMiner {
                            holoSupporter {}
                            holoArchitect {}
                        }
                        holoGuardian {
                            holoFighter {}
                        }
                    }
                }
            }
            zipBomb {}
            eye {
                ear {
                    heimdall {}
                }
            }
            TMTRAINER {
                deleter {}
                prism {
                    prismObelisk {}
                }
            }
            holoWall {
                holoWallLarge {}
            }
            sender {
                // bound with sender
                receiver(overwriteReq = true) {
                    smartUnloader {}
                    smartDistributor {}
                }
            }
            p2pNode {
                streamClient {}
                streamHost(require = listOf(streamClient))
                streamServer(require = listOf(streamClient))
            }
            wirelessTower {
                underdriveProjector {}
            }
        }
    }

    private fun loadErekir() {
        CreateTechTree(name = Meta.Name, origin = ic, planet = Planets.erekir) {
            icAssembler {
                cyberionMixer(require = listOf(slag, oxide)) {
                    stealth {
                        jammer {}
                    }
                    holoProjector {
                        holoFighter {}
                        holoSupporter {}
                        holoArchitect {}
                        holoMiner {}
                        holoGuardian {}
                    }
                }
            }
            eye {
                ear {
                    heimdall {}
                }
            }
            zipBomb {
                TMTRAINER {}
                deleter {}
                prism {
                    prismObelisk {}
                }
            }
            holoWall {
                holoWallLarge {}
            }
            sender {
                // bound with sender
                receiver(overwriteReq = true) {}
                smartDistributor {
                    smartUnloader {}
                }
            }
            p2pNode {
                streamHost {}
                streamClient {}
                streamServer(require = listOf(streamClient, streamHost))
            }
            underdriveProjector {
                wirelessTower {}
            }
        }
    }
}