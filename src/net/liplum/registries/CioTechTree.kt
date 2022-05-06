package net.liplum.registries

import mindustry.content.Blocks.coreShard
import mindustry.content.TechTree
import net.liplum.lib.withContext
import net.liplum.registries.CioBlocks.TMTRAINER
import net.liplum.registries.CioBlocks.ear
import net.liplum.registries.CioBlocks.eye
import net.liplum.registries.CioBlocks.heimdall
import net.liplum.registries.CioBlocks.receiver
import net.liplum.registries.CioBlocks.sender
import net.liplum.registries.CioBlocks.smartDistributor
import net.liplum.registries.CioBlocks.smartUnloader
import net.liplum.registries.CioBlocks.streamClient
import net.liplum.registries.CioBlocks.streamHost
import net.liplum.registries.CioBlocks.streamServer

object CioTechTree {
    fun loadSerpulo() {
        TechTree.all.withContext {
            at(coreShard).sub(sender) {
                sub(receiver) {
                    sub(smartDistributor)
                    sub(smartUnloader) {
                        sub(streamClient) {
                            sub(streamHost)
                            sub(streamServer)
                        }
                    }
                }
                sub(TMTRAINER) {
                    sub(eye)
                    sub(ear)
                    sub(heimdall)
                }
            }
        }
    }
}