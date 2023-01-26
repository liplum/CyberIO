package net.liplum.registry

import mindustry.Vars
import mindustry.content.Items
import mindustry.type.Category
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.ErekirSpec
import net.liplum.VanillaSpec
import net.liplum.annotations.DependOn
import net.liplum.data.P2pNode
import net.liplum.data.StreamClient
import net.liplum.data.StreamHost
import net.liplum.data.StreamServer
import net.liplum.data.*
import plumy.dsl.plus

object CioCyber {
    @JvmStatic lateinit var receiver: Receiver
    @JvmStatic lateinit var sender: Sender
    @JvmStatic lateinit var serializer: Serializer
    @JvmStatic lateinit var p2pNode: P2pNode
    @JvmStatic lateinit var dataCDN: DataCDN
    @JvmStatic lateinit var smartDistributor: SmartDistributor
    @JvmStatic lateinit var smartUnloader: SmartUnloader
    @JvmStatic lateinit var streamClient: StreamClient
    @JvmStatic lateinit var streamHost: StreamHost
    @JvmStatic lateinit var streamServer: StreamServer
    @DependOn("CioItem.ic")
    fun receiver() {
        receiver = Receiver("receiver").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 45,
                    Items.lead + 80,
                    Items.metaglass + 60,
                    Items.silicon + 80,
                )
                scaledHealth = 140f
                maxConnection = 5
                consumePower(1.5f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 40,
                    Items.graphite + 80,
                    Items.tungsten + 30,
                    Items.silicon + 30,
                )
                scaledHealth = 250f
                maxConnection = 3
                consumePower(0.8f)
            }
            replaceable = false
        }
    }
    @DependOn("CioItem.ic")
    fun sender() {
        sender = Sender("sender").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 45,
                    Items.lead + 80,
                    Items.metaglass + 60,
                    Items.silicon + 80,
                )
                scaledHealth = 140f
                consumePower(1.5f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 40,
                    Items.graphite + 120,
                    Items.tungsten + 50,
                    Items.silicon + 50,
                )
                scaledHealth = 250f
                maxRange = 800f
                consumePower(1f)
            }
            replaceable = false
        }
    }
    @DependOn("CioItem.ic")
    fun smartDistributor() {
        smartDistributor = SmartDistributor("smart-distributor").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 125,
                    Items.copper + 550,
                    Items.silicon + 210,
                    Items.plastanium + 80,
                    Items.thorium + 140,
                    Items.surgeAlloy + 50,
                )
                scaledHealth = 800f
                maxConnection = -1
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 75,
                    Items.tungsten + 200,
                    Items.carbide + 120,
                    Items.surgeAlloy + 50,
                )
                scaledHealth = 500f
                maxConnection = -1
            }
            size = 2
            ArrowsFrames = 4
            ArrowsDuration = 12f
        }
    }
    @DependOn("CioItem.ic")
    fun smartUnloader() {
        smartUnloader = SmartUnloader("smart-unloader").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 120,
                    Items.lead + 350,
                    Items.silicon + 210,
                    Items.graphite + 150,
                    Items.titanium + 50,
                )
                unloadSpeed = 5f
                scaledHealth = 300f
                maxConnection = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 200,
                    Items.tungsten + 350,
                    Items.carbide + 120,
                    Items.phaseFabric + 120,
                    Items.surgeAlloy + 50,
                )
                unloadSpeed = 6f
                scaledHealth = 1000f
                maxRange = 1500f
                maxConnection = 6
            }
            size = 2
            ShrinkingFrames = 7
            ShrinkingDuration = 25f
        }
    }
    @DependOn("CioItem.ic")
    fun streamClient() {
        streamClient = StreamClient("stream-client").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 38,
                    Items.metaglass + 40,
                    Items.silicon + 20,
                    Items.graphite + 40,
                    Items.titanium + 10,
                )
                scaledHealth = 180f
                maxConnection = 5
                consumePower(1.8f)
                liquidCapacity = 200f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 35,
                    Items.beryllium + 40,
                    Items.silicon + 20,
                    Items.tungsten + 40,
                )
                scaledHealth = 200f
                maxConnection = 3
                consumePower(1.5f)
                liquidCapacity = 80f
            }
            replaceable = false
            size = 1
        }
    }
    @DependOn("CioItem.ic")
    fun streamHost() {
        streamHost = StreamHost("stream-host").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 55,
                    Items.lead + 100,
                    Items.metaglass + 600,
                    Items.silicon + 80,
                    Items.graphite + 60,
                    Items.titanium + 40,
                    Items.plastanium + 20,
                )
                scaledHealth = 500f
                powerUseBase = 2f
                powerUsePerConnection = 1f
                networkSpeed = 3.5f
                liquidCapacity = 800f
                maxConnection = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 54,
                    Items.tungsten + 250,
                    Items.carbide + 100,
                    Items.beryllium + 400,
                )
                scaledHealth = 350f
                powerUseBase = 1.4f
                powerUsePerConnection = 2.5f
                networkSpeed = 5f
                liquidCapacity = 1200f
                maxConnection = 3
                maxRange = 1800f
            }
            size = 2
            replaceable = false
        }
    }
    @DependOn("CioItem.ic")
    fun streamServer() {
        streamServer = StreamServer("stream-server").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 180,
                    Items.copper + 1200,
                    Items.lead + 400,
                    Items.metaglass + 1200,
                    Items.silicon + 320,
                    Items.thorium + 40,
                    Items.phaseFabric + 120,
                )
                scaledHealth = 500f
                networkSpeed = 15f
                researchCostMultiplier = 0.7f
                powerUseBase = 3.5f
                maxConnection = 5
                powerUsePerConnection = 2f
                liquidCapacity = 2000f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 200,
                    Items.tungsten + 300,
                    Items.carbide + 150,
                    Items.beryllium + 500,
                    Items.phaseFabric + 120,
                )
                scaledHealth = 380f
                networkSpeed = 15f
                liquidCapacity = 3000f
                researchCostMultiplier = 0.6f
                powerUseBase = 2.5f
                maxConnection = 8
                maxRange = 2800f
                powerUsePerConnection = 3.8f
            }
            fireproof = true
            squareSprite = false
            size = 3
            replaceable = false
        }
    }
    @DependOn("CioItem.ic")
    fun p2pNode() {
        p2pNode = P2pNode("p2p-node").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 40,
                    Items.graphite + 50,
                    Items.metaglass + 150,
                    Items.silicon + 50,
                )
                scaledHealth = 120f
                liquidCapacity = 800f
                balancingSpeed = 0.5f
                maxRange = -1f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 35,
                    Items.tungsten + 40,
                    Items.beryllium + 240,
                    Items.silicon + 180,
                )
                scaledHealth = 100f
                liquidCapacity = 600f
                balancingSpeed = 0.3f
                maxRange = 1450f
            }
            liquidPadding = 2f
            size = 2
            squareSprite = false
            consumePower(1.5f)
        }
    }
    @DependOn("CioItem.ic")
    fun serializer() {
        DebugOnly {
            serializer = Serializer("serializer").apply {
                category = Category.units
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                size = 5
                linkRange = 30f * Vars.tilesize
            }
        }
    }
    @DependOn("CioItem.ic")
    fun dataCDN() {
        DebugOnly {
            dataCDN = DataCDN("data-cdn").apply {
                category = Category.units
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                size = 3
            }
        }
    }
}