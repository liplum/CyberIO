package net.liplum.registry

import arc.graphics.Texture
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.entities.bullet.LaserBulletType
import mindustry.entities.bullet.LightningBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.api.brain.UT
import net.liplum.api.brain.Upgrade
import net.liplum.brain.*
import net.liplum.bullet.BBulletType
import plumy.dsl.plus

object CioHeimdall {
    @JvmStatic lateinit var heimdall: Heimdall
    @JvmStatic lateinit var eye: Eye
    @JvmStatic lateinit var ear: Ear
    @JvmStatic lateinit var heart: Heart
    @DependOn("CioItem.ic")
    fun heimdall() {
        heimdall = Heimdall("heimdall").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 55,
                    Items.sporePod + 100,
                    Items.metaglass + 50,
                    Items.silicon + 120,
                    Items.plastanium + 50,
                )
                scaledHealth = 400f
                range = 175f
                powerUse = 2.5f
                damage = 8f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 50,
                    Items.oxide + 80,
                    Items.carbide + 40,
                    Items.thorium + 150,
                    Items.graphite + 150,
                )
                scaledHealth = 350f
                range = 145f
                powerUse = 2.4f
                waveSpeed = 1.8f
                damage = 10f
                reloadTime = 75f
            }
            loopSound = Sounds.minebeam
            size = 4
            connectedSound = CioSound.connected
            addFormationPatterns(
                FaceFE, FunnyFaceFE, ForceFieldFE
            )
        }
    }
    @DependOn("CioItem.ic")
    fun eye() {
        eye = Eye("heimdall-eye").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.pyratite + 15,
                    Items.plastanium + 10,
                    Items.metaglass + 40,
                    Items.silicon + 30,
                )
                range = 165f
                scaledHealth = 300f
                consumePower(3f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.carbide + 25,
                    Items.tungsten + 40,
                    Items.silicon + 60,
                )
                range = 175f
                scaledHealth = 250f
                consumePower(2.4f)
            }
            shoot.firstShotDelay = 60f
            size = 2
            moveWhileCharging = false
            shootEffect = BrainFx.eyeShoot
            smokeEffect = Fx.none
            addUpgrade(
                Upgrade(UT.Damage, false, 0.05f),
                Upgrade(UT.ReloadTime, true, -4.5f),
                Upgrade(UT.ControlLine, true, 0.01f),
                Upgrade(UT.ForceFieldRegen, false, 0.3f),
                Upgrade(UT.Range, false, -0.05f),
                Upgrade(UT.ForceFieldRadius, true, -3f),
                Upgrade(UT.WaveWidth, true, -0.1f),
                Upgrade(UT.PowerUse, false, 0.55f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.2f),
            )
            normalSounds = CioSound.laserWeak
            improvedSounds = CioSound.laser
            soundVolume = 0.2f
            normalBullet = LightningBulletType().apply {
                VanillaSpec {
                    damage = 90f
                }
                ErekirSpec {
                    damage = 240f
                }
                lightningLength = 25
                collidesAir = false
                ammoMultiplier = 1f
                recoil = 3f
                shootCone = 3f
                accurateDelay = true
                lightningColor = R.C.RedAlert
            }
            improvedBullet = LaserBulletType().apply {
                VanillaSpec {
                    damage = 250f
                }
                ErekirSpec {
                    damage = 550f
                }
                colors = arrayOf(R.C.RedAlert.cpy().a(0.4f), R.C.RedAlert, R.C.RedAlertDark)
                lightningColor = R.C.RedAlertDark
                chargeEffect = MultiEffect(BrainFx.eyeCharge, BrainFx.eyeChargeBegin)
                hitEffect = Fx.hitLancer
                hitSize = 4f
                lifetime = 16f
                recoil = 4f
                drawSize = 200f
                shootCone = 3f
                length = 173f
                accurateDelay = true
                ammoMultiplier = 1f
            }
        }
    }
    @DependOn("CioItem.ic")
    fun ear() {
        ear = Ear("heimdall-ear").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                consumePower(2f)
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.sporePod + 20,
                    Items.silicon + 50,
                    Items.plastanium + 10,
                )
                range = 145f
                scaledHealth = 300f
                damage = 4f
            }
            ErekirSpec {
                consumePower(2f)
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.graphite + 50,
                    Items.beryllium + 60,
                    Items.silicon + 50,
                    Items.phaseFabric + 20,
                )
                range = 165f
                scaledHealth = 250f
                damage = 8f
                waveSpeed = 2.2f
            }
            loopSound = Sounds.wind
            addUpgrade(
                Upgrade(UT.Damage, false, -0.02f),
                Upgrade(UT.Range, false, 0.1f),
                Upgrade(UT.WaveSpeed, true, 0.08f),
                Upgrade(UT.WaveWidth, true, 0.4f),
                Upgrade(UT.ForceFieldRadius, true, 5f),
                Upgrade(UT.ForceFieldMax, false, 0.2f),
                Upgrade(UT.PowerUse, false, 0.35f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.15f),
            )
            size = 2
        }
    }
    @DependOn("CioItem.ic")
    fun heart() {
        DebugOnly {
            heart = Heart("heimdall-heart").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                VanillaSpec {
                    requirements = arrayOf(
                        CioItem.ic + 60,
                        Items.graphite + 200,
                        Items.metaglass + 500,
                        Items.silicon + 50,
                        Items.blastCompound + 200,
                    )
                    scaledHealth = 125f
                    convertSpeed = 6f
                }
                ErekirSpec {
                    requirements = arrayOf(
                        CioItem.ic + 60,
                        Items.oxide + 100,
                        Items.tungsten + 500,
                        Items.silicon + 200,
                    )
                    convertSpeed = 7f
                    scaledHealth = 105f
                }
                size = 4
                blood = Blood()
                heartbeat.apply {
                    shake.config {
                        base = 1.5f
                        upRange = 4.8f - base
                        downRange = 0.9f
                    }
                    reloadTime.config {
                        // Decrease
                        base = 120f
                        upRange = 50f
                        downRange = 80f
                    }
                    powerUse.config {
                        base = 2f
                        upRange = 5f - base
                        downRange = 0f
                    }
                    damage.config {
                        base = 60f
                        upRange = 120f - base
                        downRange = 20f
                    }
                    range.config {
                        base = 165f
                        upRange = 240f - base
                        downRange = 0f
                    }
                    shootNumber.config {
                        base = 22
                        upRange = 34 - 22
                        downRange = 22 - 12
                    }
                    bloodCost.config {
                        base = 50f
                        upRange = 150f - 50f
                        downRange = 0f
                    }
                    systole.config {
                        base = 0.175f
                        upRange = 0.192f - 0.175f
                        downRange = 0.175f - 0.17f
                    }
                    diastole.config {
                        // Decrease
                        base = 3.3f
                        upRange = 3.5f - 3.3f
                        downRange = 3.3f - 3.15f
                    }
                    bulletLifeTime.config {
                        base = 200f
                        upRange = 300f - 200f
                        downRange = 50f
                    }
                    soundGetter = {
                        when (it) {
                            in Float.MIN_VALUE..0.1f -> CioSound.heartbeat
                            in 0.1f..Float.MAX_VALUE -> CioSound.heartbeatFaster
                            else -> CioSound.heartbeat
                        }
                    }
                    offset = 20f // +5f when improved
                }
                bulletType = BBulletType("blood-bullet".cio).apply {
                    damage = 0f
                    lifetime = 0f
                    hitEffect = Fx.none
                    shootEffect = Fx.none
                    smokeEffect = Fx.none
                    layer = Layer.bullet - 0.1f
                    despawnEffect = BrainFx.bloodBulletHit
                    hitEffect = BrainFx.bloodBulletHit
                    collidesTiles = false
                    filter = Texture.TextureFilter.nearest
                    scale = { 2.4f + it.damage / 80f }
                    hitSize = 20f
                }
            }
        }
    }
}