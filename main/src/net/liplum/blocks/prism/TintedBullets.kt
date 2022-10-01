package net.liplum.blocks.prism

import arc.graphics.Color
import arc.math.Mathf
import arc.util.Time
import mindustry.entities.bullet.*
import mindustry.gen.Building
import mindustry.graphics.Pal
import net.liplum.R
import net.liplum.api.prism.PrismRegistry.getRegistered
import net.liplum.bullet.BBulletType
import net.liplum.common.util.ArrayList
import net.liplum.common.util.copyFieldsFrom

val BulletType.isIgnoreTinted: Boolean
    get() = this in IgnoredBullets || this::class.java in IgnoredClass

fun tintedRGB(b: BulletType): List<BulletType> {
    val t = getRegistered(b)
    if (t != null) {
        return t
    }
    return when (b) {
        is BasicBulletType -> b.tinted
        is ShrapnelBulletType -> b.tinted
        is LaserBulletType -> b.tinted
        is ContinuousFlameBulletType -> b.tinted
        is ContinuousLaserBulletType -> b.tinted
        is PointLaserBulletType -> b.tinted
        is FireBulletType -> b.tinted
        is LiquidBulletType -> b.tinted
        is MassDriverBolt -> b.tinted
        is BBulletType -> b.tinted
        else -> b.tintGeneral
    }
}

val AutoRGB = {
    val rgb = R.C.PrismRgbFG
    val len = rgb.size
    val total = len * 60f
    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
}
val AutoRGBx: (Building) -> Color = {
    val rgb = R.C.PrismRgbFG
    val len = rgb.size
    val total = len * 60f
    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
}

fun Color.Lerp(target: Color, progress: Float): Color =
    this.lerp(target, progress)

inline fun <T> HashMap<T, List<T>>.rgb(
    bullet: T,
    gen: (Int) -> T,
): List<T> = this.getOrPut(bullet) {
    RgbList(gen)
}

fun BulletType.commonTint(i: Int, lerp: Float = 0.3f) {
    trailColor = FG(i).Lerp(trailColor, lerp)
    healColor = FG(i).Lerp(healColor, lerp)
    lightColor = FG(i).Lerp(lightColor, lerp)
    lightningColor = BK(i).Lerp(lightningColor, lerp)
    hitColor = BK(i).Lerp(hitColor, lerp)
}

internal fun FG(i: Int): Color =
    R.C.PrismRgbFG[i].cpy()

internal fun BK(i: Int): Color =
    R.C.PrismRgbBK[i].cpy()

val BasicBullets: HashMap<BasicBulletType, List<BasicBulletType>> = HashMap()
val BulletType.BasicTintLerp: Float
    get() = 0.4f + Mathf.randomSeed(id.toLong(), -0.1f, 0.1f)
val BasicBulletType.tinted: List<BasicBulletType>
    get() = BasicBullets.rgb(this) {
        (this.copy() as BasicBulletType).apply {
            frontColor = FG(it).Lerp(
                frontColor, BasicTintLerp
            )
            backColor = BK(it).Lerp(
                backColor, BasicTintLerp
            )
            commonTint(it, BasicTintLerp)
        }
    }
val BBullets: HashMap<BBulletType, List<BBulletType>> = HashMap()
val BBulletType.BasicTintLerp: Float
    get() = 0.4f + Mathf.randomSeed(id.toLong(), -0.1f, 0.1f)
val BBulletType.tinted: List<BBulletType>
    get() = BBullets.rgb(this) {
        (this.copy() as BBulletType).apply {
            color = FG(it).Lerp(
                color, BasicTintLerp
            )
            commonTint(it, BasicTintLerp)
        }
    }
val ShrapnelBullets: HashMap<ShrapnelBulletType, List<ShrapnelBulletType>> = HashMap()
val ShrapnelBulletType.tinted: List<ShrapnelBulletType>
    get() = ShrapnelBullets.rgb(this) {
        (this.copy() as ShrapnelBulletType).apply {
            fromColor = FG(it)
            toColor = BK(it)
            commonTint(it, BasicTintLerp)
        }
    }
val LightningBullets: HashMap<LightningBulletType, List<LightningBulletType>> = HashMap()
val LightningBulletType.tinted: List<LightningBulletType>
    get() = LightningBullets.rgb(this) {
        (this.copy() as LightningBulletType).apply {
            lightningColor = BK(it)
            commonTint(it, BasicTintLerp)
        }
    }
val RedSapBullets: HashMap<SapBulletType, List<SapBulletType>> = HashMap()
val SapBulletType.tinted: List<SapBulletType>
    get() = RedSapBullets.rgb(this) {
        (this.copy() as SapBulletType).apply {
            color = BK(it)
            commonTint(it, BasicTintLerp)
        }
    }
val FireBullets: HashMap<FireBulletType, List<FireBulletType>> = HashMap()
val FireBulletType.tinted: List<FireBulletType>
    get() = FireBullets.rgb(this) {
        (this.copy() as FireBulletType).apply {
            colorFrom = FG(it)
            colorMid = FG(it)
            colorTo = BK(it)
            commonTint(it, BasicTintLerp)
        }
    }
val LaserBullets: HashMap<LaserBulletType, List<LaserBulletType>> = HashMap()
val LaserTintLerp: Float
    get() = 0.3f + Mathf.random(-0.15f, 0.15f)
val LaserBulletType.tinted: List<LaserBulletType>
    get() = LaserBullets.rgb(this) {
        (this.copy() as LaserBulletType).apply {
            colors = Array(colors.size) { i ->
                FG(it).lerp(colors[i], LaserTintLerp)
            }
            commonTint(it, LaserTintLerp)
        }
    }
val ContinuousLaserBullets: HashMap<ContinuousLaserBulletType, List<ContinuousLaserBulletType>> = HashMap()
val ContinuousLaserBulletType.tinted: List<ContinuousLaserBulletType>
    get() = ContinuousLaserBullets.rgb(this) {
        (this.copy() as ContinuousLaserBulletType).apply {
            colors = Array(colors.size) { i ->
                FG(it).lerp(colors[i], LaserTintLerp)
            }
            commonTint(it, LaserTintLerp)
        }
    }
val ContinuousFlameBullets: HashMap<ContinuousFlameBulletType, List<ContinuousFlameBulletType>> = HashMap()
val ContinuousFlameBulletType.tinted: List<ContinuousFlameBulletType>
    get() = ContinuousFlameBullets.rgb(this) {
        (this.copy() as ContinuousFlameBulletType).apply {
            colors = Array(colors.size) { i ->
                FG(it).lerp(colors[i], LaserTintLerp)
            }
            flareColor = FG(it).Lerp(flareColor, LaserTintLerp)
            commonTint(it, LaserTintLerp)
        }
    }
val PointLaserBulletTypeBullets: HashMap<PointLaserBulletType, List<PointLaserBulletType>> = HashMap()
val PointLaserBulletType.tinted: List<PointLaserBulletType>
    get() = PointLaserBulletTypeBullets.rgb(this) {
        (this.copy() as PointLaserBulletType).apply {
            color = BK(it).Lerp(
                color, LaserTintLerp
            )
            commonTint(it, LaserTintLerp)
        }
    }
val LiquidBullets: HashMap<LiquidBulletType, List<LiquidBulletType>> = HashMap()
val BulletType.LiquidTintLerp: Float
    get() = 0.4f + Mathf.randomSeed(id.toLong(), -0.08f, 0.08f)
val LiquidBulletType.tinted: List<LiquidBulletType>
    get() = LiquidBullets.rgb(this) {
        try {
            TintLiquidBulletT(this.liquid).apply {
                copyFieldsFrom(this)
                tintColor = BK(it).cpy().Lerp(
                    this@tinted.liquid.color, LiquidTintLerp
                )
                commonTint(it, LiquidTintLerp)
            }
        } catch (e: Exception) {
            this
        }
    }
val MassDriverBolts: HashMap<MassDriverBolt, List<MassDriverBolt>> = HashMap()
val BulletType.MassDriverLerp: Float
    get() = 0.4f + Mathf.randomSeed(id.toLong(), -1.2f, 1.2f)
val MassDriverBolt.tinted: List<MassDriverBolt>
    get() = MassDriverBolts.rgb(this) {
        try {
            MassDriverBoltT().apply {
                copyFieldsFrom(this)
                tintColor = FG(it).Lerp(
                    Pal.bulletYellow, MassDriverLerp
                )
                tintBKColor = BK(it).Lerp(
                    Pal.bulletYellowBack, MassDriverLerp
                )
                hitEffect = HitBulletBigRgbFx[it]
                despawnEffect = HitBulletBigRgbFx[it]
                commonTint(it, LiquidTintLerp)
            }
        } catch (e: Exception) {
            this
        }
    }
val GeneralBullets: HashMap<BulletType, List<BulletType>> = HashMap()
val BulletType.tintGeneral: List<BulletType>
    get() = GeneralBullets.rgb(this) {
        (this.copy() as BulletType).apply {
            if (lightningType != null) {
                lightningType = lightningType.copy().apply {
                    commonTint(it, LiquidTintLerp)
                }
            }
            commonTint(it, BasicTintLerp)
        }
    }
val IgnoredBullets: HashSet<BulletType> = HashSet()
val IgnoredClass: HashSet<Class<out BulletType>> = HashSet()
fun BulletType.ignoreRGB() =
    IgnoredBullets.add(this)

fun Class<out BulletType>.ignoreRGB() =
    IgnoredClass.add(this)

inline fun <T> RgbList(gen: (Int) -> T) =
    ArrayList(3) {
        gen(it)
    }