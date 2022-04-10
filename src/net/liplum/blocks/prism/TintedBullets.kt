package net.liplum.blocks.prism

import arc.util.Time
import mindustry.entities.bullet.*
import mindustry.graphics.Pal
import net.liplum.R
import net.liplum.utils.ArrayList
import net.liplum.utils.copyFrom

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
        else -> b.tintRedGeneral
    }
}

val AutoRGB = {
    val rgb = R.C.PrismRgbFG
    val len = rgb.size
    val total = len * 60f
    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
}
val BasicBullets: HashMap<BasicBulletType, List<BasicBulletType>> = HashMap()
const val BasicTintLerp = 0.4f
val BasicBulletType.tinted: List<BasicBulletType>
    get() = BasicBullets.getOrPut(this) {
        RgbList {
            (this.copy() as BasicBulletType).apply {
                frontColor = R.C.PrismRgbFG[it].lerp(
                    this.frontColor, BasicTintLerp
                )
                backColor = R.C.PrismRgbBK[it].lerp(
                    this.backColor, BasicTintLerp
                )
            }
        }
    }
val ShrapnelBullets: HashMap<ShrapnelBulletType, List<ShrapnelBulletType>> = HashMap()
val ShrapnelBulletType.tinted: List<ShrapnelBulletType>
    get() = ShrapnelBullets.getOrPut(this) {
        RgbList {
            (this.copy() as ShrapnelBulletType).apply {
                fromColor = R.C.PrismRgbFG[it]
                toColor = R.C.PrismRgbBK[it]
            }
        }
    }
val LightningBullets: HashMap<LightningBulletType, List<LightningBulletType>> = HashMap()
val LightningBulletType.tinted: List<LightningBulletType>
    get() = LightningBullets.getOrPut(this) {
        RgbList {
            (this.copy() as LightningBulletType).apply {
                lightningColor = R.C.PrismRgbBK[it]
            }
        }
    }
val RedSapBullets: HashMap<SapBulletType, List<SapBulletType>> = HashMap()
val SapBulletType.tinted: List<SapBulletType>
    get() = RedSapBullets.getOrPut(this) {
        RgbList {
            (this.copy() as SapBulletType).apply {
                color = R.C.PrismRgbBK[it]
            }
        }
    }
val FireBullets: HashMap<FireBulletType, List<FireBulletType>> = HashMap()
val FireBulletType.tinted: List<FireBulletType>
    get() = FireBullets.getOrPut(this) {
        RgbList {
            (this.copy() as FireBulletType).apply {
                colorFrom = R.C.PrismRgbFG[it]
                colorMid = R.C.PrismRgbFG[it]
                colorTo = R.C.PrismRgbBK[it]
            }
        }
    }
val LaserBullets: HashMap<LaserBulletType, List<LaserBulletType>> = HashMap()
val LaserColorRgb = arrayOf(
    arrayOf(R.C.PrismRedFG, R.C.PrismRedBK, R.C.PrismRedBK),
    arrayOf(R.C.PrismGreenFG, R.C.PrismGreenBK, R.C.PrismRedBK),
    arrayOf(R.C.PrismBlueFG, R.C.PrismBlueBK, R.C.PrismRedBK)
)
val LaserBulletType.tinted: List<LaserBulletType>
    get() = LaserBullets.getOrPut(this) {
        RgbList {
            (this.copy() as LaserBulletType).apply {
                colors = LaserColorRgb[it]
            }
        }
    }
val ContinuousLaserBullets: HashMap<ContinuousLaserBulletType, List<ContinuousLaserBulletType>> = HashMap()
val ContinuousLaserColorRgb = arrayOf(
    arrayOf(R.C.PrismRedFG, R.C.PrismRedFG, R.C.PrismRedBK, R.C.PrismRedBK),
    arrayOf(R.C.PrismGreenFG, R.C.PrismGreenFG, R.C.PrismGreenBK, R.C.PrismRedBK),
    arrayOf(R.C.PrismBlueFG, R.C.PrismBlueFG, R.C.PrismBlueBK, R.C.PrismRedBK)
)
val ContinuousLaserBulletType.tinted: List<ContinuousLaserBulletType>
    get() = ContinuousLaserBullets.getOrPut(this) {
        RgbList {
            (this.copy() as ContinuousLaserBulletType).apply {
                colors = ContinuousLaserColorRgb[it]
                hitEffect = HitMeltRgbFx[it]
                shootEffect = HitMeltRgbFx[it]
                smokeEffect = HitMeltRgbFx[it]
                despawnEffect = HitMeltRgbFx[it]
                lightColor = R.C.PrismRgbFG[it]
            }
        }
    }
val LiquidBullets: HashMap<LiquidBulletType, List<LiquidBulletType>> = HashMap()
const val LiquidTintLerp = 0.4f
val LiquidBulletType.tinted: List<LiquidBulletType>
    get() = LiquidBullets.getOrPut(this) {
        RgbList {
            try {
                val b = TintLiquidBulletT(this.liquid)
                b.copyFrom(this)
                b.apply {
                    tintColor = R.C.PrismRgbBK[it].cpy().lerp(
                        this@tinted.liquid.color, LiquidTintLerp
                    )
                }
            } catch (e: Exception) {
                this
            }
        }
    }
val MassDriverBolts: HashMap<MassDriverBolt, List<MassDriverBolt>> = HashMap()
const val MassDriverLerp = 0.4f
val MassDriverBolt.tinted: List<MassDriverBolt>
    get() = MassDriverBolts.getOrPut(this) {
        RgbList {
            try {
                val b = MassDriverBoltT()
                b.copyFrom(this)
                b.apply {
                    tintColor = R.C.PrismRgbFG[it].cpy().lerp(
                        Pal.bulletYellow, MassDriverLerp
                    )
                    tintBKColor = R.C.PrismRgbBK[it].cpy().lerp(
                        Pal.bulletYellowBack, MassDriverLerp
                    )
                    hitEffect = HitBulletBigRgbFx[it]
                    despawnEffect = HitBulletBigRgbFx[it]
                }
            } catch (e: Exception) {
                this
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
val BulletType.tintRedGeneral: List<BulletType>
    get() = GeneralBullets.getOrPut(this) {
        RgbList {
            (this.copy() as BulletType).apply {
                shootEffect = ShootRgbFx[it]
                hitEffect = SmallRgbFx[it]
                despawnEffect = SmallRgbFx[it]
                smokeEffect = SmallRgbFx[it]
            }
        }
    }
val IgnoredBullets: HashSet<BulletType> = HashSet()
val IgnoredClass: HashSet<Class<out BulletType>> = HashSet()
fun BulletType.ignoreRGB() =
    IgnoredBullets.add(this)

fun Class<out BulletType>.ignoreRGB() =
    IgnoredClass.add(this)

fun <T> RgbList(gen: (Int) -> T) =
    ArrayList(3) {
        gen(it)
    }