package net.liplum.brains

import arc.Core
import arc.math.Mathf
import arc.scene.ui.layout.Table
import arc.util.Strings
import mindustry.Vars.tilesize
import mindustry.content.StatusEffects
import mindustry.ctype.UnlockableContent
import mindustry.entities.bullet.BulletType
import mindustry.gen.Tex
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import mindustry.world.meta.StatValue
import mindustry.world.meta.Stats
import net.liplum.api.brain.IComponentBlock
import net.liplum.api.brain.UpgradeType
import net.liplum.utils.bundle
import net.liplum.utils.format
import net.liplum.utils.percent
import net.liplum.utils.value

val UpgradeType.localizedName: String
    get() = "heimdall.${UpgradeType.Names[type]}.name".bundle

fun <T> T.addUpgradeComponentStats() where T : Block, T : IComponentBlock {
    stats.add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, upgrade) in upgrades) {
            stat.add(type.localizedName).left()
            stat.table {
                it.add(
                    if (upgrade.isDelta)
                        upgrade.value.value()
                    else
                        upgrade.value.percent(0)
                )
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}

fun Stats.addHeimdallProperties(props: Map<UpgradeType, Float>) {
    add(Stat.boostEffect) { stat ->
        stat.row()
        for ((type, basic) in props) {
            stat.add(type.localizedName).left()
            stat.table {
                it.add(basic.format(2))
            }.get().background(Tex.underline)
            stat.row()
        }
    }
}

fun <T : UnlockableContent> eyeAmmo(vararg map: Pair<T, BulletType>, indent: Int = 0): StatValue {
    return StatValue { table: Table ->
        table.row()
        for ((t, type) in map) {
            val compact = t is UnitType || indent > 0
            //no point in displaying unit icon twice
            if (!compact && t !is PowerTurret) {
                table.image(t.uiIcon).size((3 * 8).toFloat()).padRight(4f).right().top()
                table.add(t.localizedName).padRight(10f).left().top()
            }
            table.table { bt: Table ->
                bt.left().defaults().padRight(3f).left()
                if (type.damage > 0 && (type.collides || type.splashDamage <= 0)) {
                    if (type.continuousDamage() > 0) {
                        bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized())
                    } else {
                        bt.add(Core.bundle.format("bullet.damage", type.damage))
                    }
                }
                if (type.buildingDamageMultiplier != 1f) {
                    sep(bt, Core.bundle.format("bullet.buildingdamage", (type.buildingDamageMultiplier * 100).toInt()))
                }
                if (type.splashDamage > 0) {
                    sep(
                        bt,
                        Core.bundle.format(
                            "bullet.splashdamage",
                            type.splashDamage.toInt(),
                            Strings.fixed(type.splashDamageRadius / tilesize, 1)
                        )
                    )
                }
                if (!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier) {
                    sep(bt, Core.bundle.format("bullet.multiplier", type.ammoMultiplier.toInt()))
                }
                if (!compact && !Mathf.equal(type.reloadMultiplier, 1f)) {
                    sep(bt, Core.bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)))
                }
                if (type.knockback > 0) {
                    sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)))
                }
                if (type.healPercent > 0f) {
                    sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)))
                }
                if (type.pierce || type.pierceCap != -1) {
                    sep(bt, if (type.pierceCap == -1) "@bullet.infinitepierce" else Core.bundle.format("bullet.pierce", type.pierceCap))
                }
                if (type.incendAmount > 0) {
                    sep(bt, "@bullet.incendiary")
                }
                if (type.homingPower > 0.01f) {
                    sep(bt, "@bullet.homing")
                }
                if (type.lightning > 0) {
                    sep(
                        bt,
                        Core.bundle.format(
                            "bullet.lightning",
                            type.lightning,
                            if (type.lightningDamage < 0) type.damage else type.lightningDamage
                        )
                    )
                }
                if (type.status !== StatusEffects.none) {
                    sep(bt, (if (type.minfo.mod == null) type.status.emoji() else "") + "[stat]" + type.status.localizedName)
                }
                if (type.fragBullet != null) {
                    sep(bt, Core.bundle.format("bullet.frags", type.fragBullets))
                    bt.row()
                    eyeAmmo(Pair(t, type.fragBullet), indent = indent + 1).display(bt)
                }
            }.padTop(if (compact) 0f else -9f).padLeft((indent * 8).toFloat()).left().get()
                .background(if (compact) null else Tex.underline)
            table.row()
        }
    }
}
//for AmmoListValue
private fun sep(table: Table, text: String) {
    table.row()
    table.add(text)
}
