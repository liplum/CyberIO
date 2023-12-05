package net.liplum.holo

import arc.Core
import arc.graphics.Color
import arc.graphics.Texture
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.math.Scaled
import arc.scene.ui.Image
import arc.scene.ui.layout.Table
import arc.util.Scaling
import arc.util.Tmp
import mindustry.Vars
import mindustry.ai.types.LogicAI
import mindustry.content.Blocks
import mindustry.content.Fx
import mindustry.entities.part.DrawPart
import mindustry.gen.Iconc
import mindustry.gen.Payloadc
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.type.ItemStack
import mindustry.type.UnitType
import mindustry.ui.Bar
import mindustry.world.meta.Env
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.common.shader.use
import plumy.dsl.bundle
import net.liplum.common.util.toFloat
import net.liplum.holo.HoloProjector.HoloProjectorBuild
import plumy.core.math.FUNC
import plumy.core.ClientOnly
import plumy.core.Else
import plumy.core.MUnit
import net.liplum.utils.healthPct
import net.liplum.registry.SD
import net.liplum.utils.time
import plumy.core.arc.toSecond
import plumy.core.assets.TR
import plumy.texture.*
import plumy.dsl.castBuild
import kotlin.math.min

/**
 * Only support flying unit for now.
 */
open class HoloUnitType(name: String) : UnitType(name) {
    @JvmField
    @ClientOnly
    var ColorOpacity = -1f
    @JvmField
    @ClientOnly
    var HoloOpacity = -1f
    @JvmField
    @ClientOnly
    var minAlpha = 0.15f
    @JvmField
    var lose = 0.3f
    @JvmField
    var loseMultiplierWhereMissing = 12f
    @JvmField
    var lifespan = 120 * 60f
    @JvmField
    var overageDmgFactor = 0.5f
    @ClientOnly
    @JvmField
    var ruvikShootingTipTime = 30f
    @ClientOnly
    @JvmField
    var ruvikTipRange = 100f
    @ClientOnly
    @JvmField
    var enableRuvikTip = false
    @JvmField
    var sacrificeCyberionAmount = 1f
    /**
     * Cyber amount -> Lifetime (unit:tick)
     */
    @JvmField
    var sacrificeLifeFunc: FUNC = { it * 15f }
    @JvmField
    var researchReq: Array<ItemStack> = emptyArray()

    init {
        //outlineColor = Var.HologramDark
        allowedInPayloads = false
        lightColor = Var.Hologram
        engineColor = Var.Hologram
        healColor = Var.Hologram
        engineColorInner = Var.HologramDark
        mechLegColor = Var.HologramDark
        wreckRegions = emptyArray()
        fallEffect = Fx.none
        fallEngineEffect = Fx.none
        deathExplosionEffect = Fx.none
        canDrown = false
        immunities.addAll(Vars.content.statusEffects())
        envDisabled = Env.none
        isEnemy = false
        payloadCapacity = 0f
        useUnitCap = false
    }

    override fun loadIcon() {
        super.loadIcon()
        val width = fullIcon.width
        val height = fullIcon.height
        val maker = StackIconBakery(width, height)
        val rawIcon = fullIcon
        val layers = listOf(
            Layer(Core.atlas.getPixmap(rawIcon).toLayerBuffer()),
            Layer(Core.atlas.getPixmap(rawIcon).toLayerBuffer()) {
                +TintLerpLayerProcessor(Var.Hologram, Var.HoloUnitTintAlpha)
            }
        )
        val baked = maker.bake(layers).createPixmap()
        val icon = TR(Texture(baked))
        fullIcon = icon
        uiIcon = icon
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
        if (unit.inFogTo(Vars.player.team())) return
        val isPayload = !unit.isAdded
        val z = if (isPayload)
            Draw.z()
        else
            if (unit.elevation > 0.5f)
                if (lowAltitude)
                    Layer.flyingUnitLow
                else
                    Layer.flyingUnit
            else
                groundLayer + Mathf.clamp(hitSize / 4000f, 0f, 0.01f)

        if (unit.controller().isBeingControlled(Vars.player.unit())) {
            drawControl(unit)
        }

        if (!isPayload && (unit.isFlying || shadowElevation > 0)) {
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
        SD.Hologram.use {
            it.alpha = alpha
            it.opacityNoise *= 2f - healthPct
            it.flickering = it.DefaultFlickering + (1f - healthPct)
            if (ColorOpacity > 0f)
                it.blendFormerColorOpacity = ColorOpacity
            if (HoloOpacity > 0f) {
                it.blendHoloColorOpacity = HoloOpacity
            }
            Draw.z(z)
            if (drawBody) {
                drawOutline(unit)
            }
            drawWeaponOutlines(unit)
            if (engineLayer > 0)
                Draw.z(engineLayer)
            if (engineSize > 0)
                drawEngines(unit)
            if (drawBody)
                drawBody(unit)
            if (drawCell)
                drawCell(unit)
            drawWeapons(unit)
            drawLight(unit)
        }
        if (unit.shieldAlpha > 0 && drawShields) {
            drawShield(unit)
        }
        if (drawItems) {
            drawItems(unit)
        }
        for (part in parts) {
            val first = if (unit.mounts.size > part.weaponIndex)
                unit.mounts[part.weaponIndex]
            else null
            if (first != null) {
                DrawPart.params.set(
                    first.warmup,
                    first.reload / weapons.first().reload,
                    first.smoothReload,
                    first.heat,
                    first.recoil,
                    first.charge,
                    unit.x,
                    unit.y,
                    unit.rotation
                )
            } else {
                DrawPart.params.set(
                    0f, 0f, 0f, 0f, 0f, 0f,
                    unit.x, unit.y, unit.rotation
                )
            }
            if (unit is Scaled) {
                DrawPart.params.life = unit.fin()
            }
            part.draw(DrawPart.params)
        }

        if (!isPayload) {
            for (a in unit.abilities) {
                Draw.reset()
                a.draw(unit)
            }
        }
        Draw.reset()
    }

    override fun <T> drawPayload(unit: T) where T : Unit, T : Payloadc {
        if (unit.hasPayload()) {
            val pay = unit.payloads().first()
            pay.set(unit.x, unit.y, unit.rotation)
            pay.draw()
        }
    }

    override fun getRequirements(
        prevReturn: Array<out UnitType>?,
        timeReturn: FloatArray?,
    ): Array<ItemStack> {
        return researchReq
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.health) {
            it.add(lifespan.toSecond.time())
            it.row()
        }
    }

    open fun setBars(unit: Unit, bars: Table) {
        if (unit is HoloUnit) {
            DebugOnly {
                bars.add(Bar(
                    { R.Bar.RestLifeFigure.bundle(unit.restLife.toSecond) },
                    { Var.Hologram },
                    { unit.restLifePercent }
                ))
            }.Else {
                bars.add(Bar(
                    { R.Bar.RestLife.bundle },
                    { Var.Hologram },
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
            if (unit is Payloadc && canShowPayload(unit)) {
                bars.add(
                    Bar(
                        "stat.payloadcapacity",
                        Pal.items
                    ) { unit.payloadUsed() / unit.type().payloadCapacity }
                )
                bars.row()
            }
            if (unit is HoloUnit) {
                DebugOnly {
                    bars.add(
                        Bar({
                            val p = unit.projectorPos.castBuild<HoloProjectorBuild>()
                            if (p != null) "${p.tileX()},${p.tileY()}"
                            else "${Iconc.cancel}"
                        }, {
                            val p = unit.projectorPos.castBuild<HoloProjectorBuild>()
                            if (p != null) Var.Hologram
                            else Color.gray
                        }, {
                            (unit.projectorPos.castBuild<HoloProjectorBuild>() != null).toFloat()
                        })
                    )
                }.Else {
                    bars.add(
                        Bar({
                            val p = unit.projectorPos.castBuild<HoloProjectorBuild>()
                            if (p != null) "${Iconc.home}"
                            else "${Iconc.cancel}"
                        }, {
                            Var.Hologram
                        }, {
                            (unit.projectorPos.castBuild<HoloProjectorBuild>() != null).toFloat()
                        })
                    )
                }
                bars.row()
            }
            if (unit is Payloadc && canShowPayload(unit)) {
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

    fun <T> canShowPayload(unit: T): Boolean where T : MUnit, T : Payloadc =
        unit.type().payloadCapacity > 0f

    override fun drawShield(unit: Unit) {
        val alpha = unit.shieldAlpha() * 0.5f
        val radius = unit.hitSize() * 1.3f
        Fill.light(
            unit.x, unit.y, Lines.circleVertices(radius), radius,
            Color.clear,
            Tmp.c2.set(Var.Hologram)
                .lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f))
                .a(0.7f * alpha)
        )
    }
}

fun HoloUnitType.autoLife(hp: Float = health, lose: Float) {
    this.health = hp
    this.lose = lose
    this.lifespan = this.health / lose
}

fun HoloUnitType.limitLife(hp: Float, lifespan: Float) {
    this.health = hp
    this.lifespan = lifespan
    this.lose = hp / lifespan
}
