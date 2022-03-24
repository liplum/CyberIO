package net.liplum.holo

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import arc.util.Scaling
import arc.util.Tmp
import mindustry.Vars
import mindustry.ai.types.LogicAI
import mindustry.content.Blocks
import mindustry.gen.Iconc
import mindustry.gen.Payloadc
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.UnitType
import mindustry.ui.Bar
import net.liplum.*
import net.liplum.shaders.SD
import net.liplum.shaders.use
import net.liplum.utils.bundle
import net.liplum.utils.healthPct
import kotlin.math.min

open class HoloUnitType(name: String) : UnitType(name) {
    @JvmField @ClientOnly var ColorOpacity = -1f
    @JvmField @ClientOnly var HoloOpacity = -1f
    @JvmField @ClientOnly var minAlpha = 0.15f
    @JvmField var lose = 0.3f
    @JvmField var loseMultiplierWhereMissing = 12f
    @JvmField var lifespan = 120 * 60f
    @JvmField var overageDmgFactor = 0.5f

    init {
        //outlineColor = R.C.HoloDark
        lightColor = R.C.Holo
        engineColor = R.C.Holo
        healColor = R.C.Holo
        engineColorInner = R.C.HoloDark
        mechLegColor = R.C.HoloDark

        immunities.addAll(Vars.content.statusEffects())
    }

    open fun AutoLife(lose: Float) {
        this.lose = lose
        this.lifespan = this.health / lose
    }

    open fun AutoLife(maxHealth: Float, lose: Float) {
        this.health = maxHealth
        this.lose = lose
        this.lifespan = this.health / lose
    }

    open val Unit.holoAlpha: Float
        get() {
            var alpha = this.healthPct
            if (this is HoloUnit) {
                alpha = min(alpha, this.restLifePercent)
            }
            return alpha.coerceAtLeast(minAlpha)
        }

    override fun draw(unit: Unit) {
        val z =
            if (unit.elevation > 0.5f) if (lowAltitude) Layer.flyingUnitLow else Layer.flyingUnit else groundLayer + Mathf.clamp(
                hitSize / 4000f,
                0f,
                0.01f
            )

        if (unit.controller().isBeingControlled(Vars.player.unit())) {
            drawControl(unit)
        }

        if (unit.isFlying || visualElevation > 0) {
            Draw.z(min(Layer.darkness, z - 1f))
            drawShadow(unit)
        }

        Draw.z(z - 0.02f)

        Draw.z(min(z - 0.01f, Layer.bullet - 1f))

        if (unit is Payloadc) {
            drawPayload(unit)
        }
        val healthPct = unit.healthPct
        val alpha = unit.holoAlpha
        drawSoftShadow(unit, alpha)

        Draw.z(z)
        SD.Hologram2.use {
            it.alpha = alpha
            it.opacityNoise *= 2f - healthPct
            it.flickering = it.DefaultFlickering + (1f - healthPct)
            if (ColorOpacity > 0f)
                it.blendFormerColorOpacity = ColorOpacity
            if (HoloOpacity > 0f) {
                it.blendHoloColorOpacity = HoloOpacity
            }
            if (drawBody) {
                drawOutline(unit)
            }
            drawWeaponOutlines(unit)
            if (engineSize > 0) {
                drawEngine(unit)
            }
            if (drawBody) {
                drawBody(unit)
            }
            if (drawCell) {
                drawCell(unit)
            }
            drawWeapons(unit)
            drawLight(unit)
        }

        if (unit.shieldAlpha > 0 && drawShields) {
            drawShield(unit)
        }
        if (drawItems) {
            drawItems(unit)
        }

        if (decals.size > 0) {
            val base = unit.rotation - 90
            for (d in decals) {
                Draw.z(d.layer)
                Draw.scl(d.xScale, d.yScale)
                Draw.color(d.color)
                Draw.rect(
                    d.region,
                    unit.x + Angles.trnsx(base, d.x, d.y),
                    unit.y + Angles.trnsy(base, d.x, d.y),
                    base + d.rotation
                )
            }
            Draw.reset()
            Draw.z(z)
        }

        if (unit.abilities.size > 0) {
            for (a in unit.abilities) {
                Draw.reset()
                a.draw(unit)
            }
        }

        Draw.reset()
    }

    open fun setBars(unit: Unit, bars: Table) {
        if (unit is HoloUnit) {
            DebugOnly {
                bars.add(Bar(
                    { R.Bar.RestLifeFigure.bundle(unit.restLife.seconds) },
                    { R.C.Holo },
                    { unit.restLifePercent }
                ))
            }.Else {
                bars.add(Bar(
                    { R.Bar.RestLife.bundle },
                    { R.C.Holo },
                    { unit.restLifePercent }
                ))
            }
            bars.row()
        }
    }

    override fun display(unit: Unit, table: Table) {
        table.table { t: Table ->
            t.left()
            t.add(Image(uiIcon)).size(Vars.iconMed).scaling(Scaling.fit)
            t.labelWrap(localizedName).left().width(190f).padLeft(5f)
        }.growX().left()
        table.row()

        table.table { bars: Table ->
            bars.defaults().growX().height(20f).pad(4f)
            bars.add(
                Bar("stat.health", Pal.health) { unit.healthf() }.blink(Color.white)
            )
            bars.row()
            if (Vars.state.rules.unitAmmo) {
                bars.add(
                    Bar(
                        ammoType.icon() + " " + Core.bundle["stat.ammo"], ammoType.barColor()
                    ) { unit.ammo / ammoCapacity }
                )
                bars.row()
            }
            setBars(unit, bars)
            for (ability in unit.abilities) {
                ability.displayBars(unit, bars)
            }
            if (unit is Payloadc) {
                bars.add(
                    Bar(
                        "stat.payloadcapacity",
                        Pal.items
                    ) { unit.payloadUsed() / unit.type().payloadCapacity }
                )
                bars.row()
                val count = floatArrayOf(-1f)
                bars.table().update { t: Table? ->
                    if (count[0] != unit.payloadUsed()) {
                        unit.contentInfo(t, (8 * 2).toFloat(), 270f)
                        count[0] = unit.payloadUsed()
                    }
                }.growX().left().height(0f).pad(0f)
            }
        }.growX()

        if (unit.controller() is LogicAI) {
            table.row()
            table.add(Blocks.microProcessor.emoji() + " " + Core.bundle["units.processorcontrol"]).growX().wrap().left()
            table.row()
            table.label { Iconc.settings.toString() + " " + unit.flag.toLong() + "" }.color(Color.lightGray).growX()
                .wrap().left()
        }

        table.row()
    }

    override fun drawShield(unit: Unit) {
        val alpha = unit.shieldAlpha()
        val radius = unit.hitSize() * 1.3f
        Fill.light(
            unit.x, unit.y, Lines.circleVertices(radius), radius,
            Color.clear,
            Tmp.c2.set(R.C.Holo)
                .lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f))
                .a(0.7f * alpha)
        )
    }
}