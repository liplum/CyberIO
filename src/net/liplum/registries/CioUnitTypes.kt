package net.liplum.registries

import arc.func.Prov
import mindustry.ai.types.MinerAI
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.content.StatusEffects
import mindustry.entities.bullet.BulletType
import mindustry.gen.Sounds
import mindustry.type.Weapon
import mindustry.type.ammo.ItemAmmoType
import mindustry.type.ammo.PowerAmmoType
import mindustry.world.meta.BlockFlag
import net.liplum.Cio
import net.liplum.R
import net.liplum.holo.HoloAbility
import net.liplum.holo.HoloUnitType
import net.liplum.utils.NewUnitType

object CioUnitTypes : ContentTable {
    @JvmStatic lateinit var holoMiner: HoloUnitType
    @JvmStatic lateinit var holoFighter: HoloUnitType
    override fun firstLoad() {
        holoMiner = NewUnitType(R.Unit.HoloMiner, ::HoloUnitType).apply {
            health = 3000f
            abilities.add(HoloAbility())
            defaultController = Prov { MinerAI() }
            lowAltitude = true
            flying = true
            mineSpeed = 6.5f
            mineTier = 3
            armor = 2f
            drag = 0.06f
            accel = 0.12f
            speed = 1.5f
            engineSize = 1.8f
            engineOffset = 5.7f
            range = 50f
            isCounted = false

            ammoType = PowerAmmoType(500f)

        }

        holoFighter = NewUnitType(R.Unit.HoloFighter, ::HoloUnitType).apply {
            health = 2500f
            abilities.add(HoloAbility())
            speed = 1.65f
            accel = 0.08f
            drag = 0.016f
            flying = true
            hitSize = 9f
            targetAir = true
            engineOffset = 7.8f
            range = 150f
            faceTarget = false
            armor = 3f
            playerTargetFlags = arrayOf(null)
            targetFlags = arrayOf(BlockFlag.factory, null)
            commandLimit = 5
            circleTarget = true
            ammoType = ItemAmmoType(Items.plastanium)

            weapons.add(object : Weapon("holo-fighter-gun".Cio) {
                init {
                    top = false
                    shootSound = Sounds.flame
                    shootY = 2f
                    reload = 11f
                    recoil = 1f
                    ejectEffect = Fx.none
                    bullet = object : BulletType(4.1f, 35f) {
                        init {
                            ammoMultiplier = 3f
                            hitSize = 7f
                            lifetime = 13f
                            pierce = true
                            statusDuration = 60f * 4
                            shootEffect = Fx.shootSmallFlame
                            hitEffect = Fx.hitFlameSmall
                            despawnEffect = Fx.none
                            status = StatusEffects.burning
                            keepVelocity = false
                            hittable = false
                        }
                    }
                }
            })
        }
    }

    override fun load() {
    }

    override fun lastLoad() {
    }
}