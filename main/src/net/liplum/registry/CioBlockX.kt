package net.liplum.registry

import mindustry.type.Category
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.annotations.DependOn
import net.liplum.api.bullets.MultiBulletAbility
import net.liplum.blocks.ddos.DDoS
import net.liplum.blocks.decentralizer.Decentralizer
import net.liplum.bullet.*

object CioBlockX {
    @JvmStatic lateinit var DDoS: DDoS
    @JvmStatic lateinit var decentralizer: Decentralizer
    fun decentralizer() {
        DebugOnly {
            decentralizer = Decentralizer("decentralizer").apply {
                requirements(
                    Category.crafting, BuildVisibility.shown, arrayOf()
                )
                size = 4
            }
        }
    }
    @DependOn("CioItem.ic")
    fun ddos() {
        DebugOnly {
            DDoS = DDoS("DDoS").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                maxDamage = 120f
                size = 4
                hitSizer = { damage / 80f * 4f }
                bulletType = AbilityItemBulletType().apply bullet@{
                    ability = MultiBulletAbility(
                        ProvidenceBA(),
                        SlowDownBA(),
                        InfiniteBA(),
                        //TeleportBA(),
                        BlackHoleBA(),
                        RestrictedAreaBA(),
                    ).apply {
                        bulletType = this@bullet
                    }
                    speed = 2f
                    damage = 0f
                    hitSize = 10f
                    pierce = true
                    pierceCap = 5
                    drawSizer = { damage / 80f }
                    trailLength = 8
                    lifetime = 180f
                    trailWidth = 4f
                }
                DebugOnly {
                    health = Int.MAX_VALUE
                }
            }
        }
    }
}