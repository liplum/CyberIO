package net.liplum.blocks.prism

import arc.graphics.Color
import arc.math.Mathf
import arc.util.Time
import mindustry.entities.bullet.*
import mindustry.gen.Building
import mindustry.graphics.Pal
import net.liplum.R
import net.liplum.lib.copyFrom
import net.liplum.utils.ArrayList

val BulletType.isTintIgnored: Boolean
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
        is ContinuousLaserBulletType -> b.tinted
        is FireBulletType -> b.tinted
        is LiquidBulletType -> b.tinted
        is MassDriverBolt -> b.tinted
        else -> {
            if (b.lightningType != null)
                b.tintedLighting
            else
                b.tintGeneral
        }
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

inline fun <T> HashMap<T, List<T>>.rgb(
    bullet: T,
    gen: (Int) -> T,
): List<T> = this.getOrPut(bullet) {
    RgbList(gen)
}

fun BulletType.commonTint(i: Int, lerp: Float = 0.3f) {
    trailColor = FG(i).lerp(trailColor, lerp)
}

internal fun FG(i: Int): Color =
    R.C.PrismRgbFG[i].cpy()

internal fun BK(i: Int): Color =
    R.C.PrismRgbBK[i].cpy()

val BasicBullets: HashMap<BasicBulletType, List<BasicBulletType>> = HashMap()
val BasicTintLerp: Float
    get() = 0.4f + Mathf.random(-0.1f, 0.1f)
val BasicBulletType.tinted: List<BasicBulletType>
    get() = BasicBullets.rgb(this) {
        (this.copy() as BasicBulletType).apply {
            frontColor = FG(it).lerp(
                frontColor, BasicTintLerp
            )
            backColor = BK(it).lerp(
                backColor, BasicTintLerp
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
            hitEffect = HitMeltRgbFx[it]
            shootEffect = HitMeltRgbFx[it]
            smokeEffect = HitMeltRgbFx[it]
            despawnEffect = HitMeltRgbFx[it]
            lightColor = FG(it)
            commonTint(it, LaserTintLerp)
        }
    }
val LiquidBullets: HashMap<LiquidBulletType, List<LiquidBulletType>> = HashMap()
val LiquidTintLerp: Float
    get() = 0.4f + Mathf.random(-0.08f, 0.08f)
val LiquidBulletType.tinted: List<LiquidBulletType>
    get() = LiquidBullets.rgb(this) {
        try {
            TintLiquidBulletT(this.liquid).apply {
                copyFrom(this)
                tintColor = BK(it).cpy().lerp(
                    this@tinted.liquid.color, LiquidTintLerp
                )
                commonTint(it, LiquidTintLerp)
            }
        } catch (e: Exception) {
            this
        }
    }
val MassDriverBolts: HashMap<MassDriverBolt, List<MassDriverBolt>> = HashMap()
val MassDriverLerp: Float
    get() = 0.4f + Mathf.random(-1.2f, 1.2f)
val MassDriverBolt.tinted: List<MassDriverBolt>
    get() = MassDriverBolts.rgb(this) {
        try {
            MassDriverBoltT().apply {
                copyFrom(this)
                tintColor = FG(it).lerp(
                    Pal.bulletYellow, MassDriverLerp
                )
                tintBKColor = BK(it).lerp(
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
val LightingBullets: HashMap<BulletType, List<BulletType>> = HashMap()
val BulletType.tintedLighting: List<BulletType>
    get() = LightingBullets.rgb(this) {
        this.copy().apply {
            lightningType = this.lightningType.copy().apply {
                lightningColor = BK(it)
                commonTint(it, LiquidTintLerp)
            }
        }
    }
val Registry: HashMap<BulletType, List<BulletType>> = HashMap()
fun <T : BulletType> T.registerRGB(register: T.() -> Triple<T, T, T>) {
    val (r, g, b) = register()
    Registry[this] = arrayListOf(r, g, b)
}

fun <T : BulletType> T.registerRGBs(register: T.() -> List<T>) {
    Registry[this] = this.register()
}

fun <T : BulletType> T.registerRGBIndex(register: T.(Int) -> T) {
    Registry[this] = RgbList { register(it) }
}
/**
 * @param registerGen a generator which accepts 0(red), 1(green) and 2(blue) and returns the corresponding [BulletType] object.
 */
fun BulletType.registerRGBGen(registerGen: BulletType.() -> ((Int) -> BulletType)) {
    val gen = registerGen()
    Registry[this] = RgbList(gen)
}

fun getRegistered(b: BulletType): List<BulletType>? =
    Registry[b]

val GeneralBullets: HashMap<BulletType, List<BulletType>> = HashMap()
val BulletType.tintGeneral: List<BulletType>
    get() = GeneralBullets.rgb(this) {
        (this.copy() as BulletType).apply {
            shootEffect = ShootRgbFx[it]
            hitEffect = SmallRgbFx[it]
            despawnEffect = SmallRgbFx[it]
            smokeEffect = SmallRgbFx[it]
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