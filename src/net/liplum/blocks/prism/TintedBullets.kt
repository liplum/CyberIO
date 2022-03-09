package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Angles
import arc.util.Time
import mindustry.entities.Effect
import mindustry.entities.bullet.*
import net.liplum.R
import net.liplum.utils.ArrayList
import net.liplum.utils.copyFrom

class TintedBullets {
    companion object {
        @JvmStatic
        val BulletType.isTintIgnored: Boolean
            get() = this in IgnoredBullets || this::class.java in IgnoredClass
        @JvmStatic
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
                else -> b.tintRedGeneral
            }
        }
        val AutoRGB =  {
            val rgb = R.C.PrismRgbFG
            val len = rgb.size
            val total = len * 60f
            rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]!!
        }
        @JvmStatic
        val BasicBullets: HashMap<BasicBulletType, ArrayList<BasicBulletType>> = HashMap()
        @JvmStatic
        val BasicTintLerp = 0.4f
        @JvmStatic
        val BasicBulletType.tinted: ArrayList<BasicBulletType>
            get() = BasicBullets.getOrPut(this) {
                ArrayList(3) {
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
        @JvmStatic
        val ShrapnelBullets: HashMap<ShrapnelBulletType, ArrayList<ShrapnelBulletType>> = HashMap()
        @JvmStatic
        val ShrapnelBulletType.tinted: ArrayList<ShrapnelBulletType>
            get() = ShrapnelBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as ShrapnelBulletType).apply {
                        fromColor = R.C.PrismRgbFG[it]
                        toColor = R.C.PrismRgbBK[it]
                    }
                }
            }
        @JvmStatic
        val LightningBullets: HashMap<LightningBulletType, ArrayList<LightningBulletType>> = HashMap()
        @JvmStatic
        val LightningBulletType.tinted: ArrayList<LightningBulletType>
            get() = LightningBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as LightningBulletType).apply {
                        lightningColor = R.C.PrismRgbBK[it]
                    }
                }
            }
        @JvmStatic
        val RedSapBullets: HashMap<SapBulletType, ArrayList<SapBulletType>> = HashMap()
        @JvmStatic
        val SapBulletType.tinted: ArrayList<SapBulletType>
            get() = RedSapBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as SapBulletType).apply {
                        color = R.C.PrismRgbBK[it]
                    }
                }
            }
        @JvmStatic
        val FireBullets: HashMap<FireBulletType, ArrayList<FireBulletType>> = HashMap()
        @JvmStatic
        val FireBulletType.tinted: ArrayList<FireBulletType>
            get() = FireBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as FireBulletType).apply {
                        colorFrom = R.C.PrismRgbFG[it]
                        colorMid = R.C.PrismRgbFG[it]
                        colorTo = R.C.PrismRgbBK[it]
                    }
                }
            }
        @JvmStatic
        val LaserBullets: HashMap<LaserBulletType, ArrayList<LaserBulletType>> = HashMap()
        @JvmStatic
        val LaserColorRgb = arrayOf(
            arrayOf(R.C.PrismRedFG, R.C.PrismRedBK, Color.white),
            arrayOf(R.C.PrismGreenFG, R.C.PrismGreenBK, Color.white),
            arrayOf(R.C.PrismBlueFG, R.C.PrismBlueBK, Color.white)
        )
        @JvmStatic
        val LaserBulletType.tinted: ArrayList<LaserBulletType>
            get() = LaserBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as LaserBulletType).apply {
                        colors = LaserColorRgb[it]
                    }
                }
            }
        @JvmStatic
        val ContinuousLaserBullets: HashMap<ContinuousLaserBulletType, ArrayList<ContinuousLaserBulletType>> = HashMap()
        @JvmStatic
        val ContinuousLaserColorRgb = arrayOf(
            arrayOf(R.C.PrismRedFG, R.C.PrismRedFG, R.C.PrismRedBK, Color.white),
            arrayOf(R.C.PrismGreenFG, R.C.PrismGreenFG, R.C.PrismGreenBK, Color.white),
            arrayOf(R.C.PrismBlueFG, R.C.PrismBlueFG, R.C.PrismBlueBK, Color.white)
        )
        @JvmStatic
        val ContinuousLaserBulletType.tinted: ArrayList<ContinuousLaserBulletType>
            get() = ContinuousLaserBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as ContinuousLaserBulletType).apply {
                        colors = ContinuousLaserColorRgb[it]
                    }
                }
            }
        @JvmStatic
        val LiquidBullets: HashMap<LiquidBulletType, ArrayList<LiquidBulletType>> = HashMap()
        @JvmStatic
        val LiquidTintLerp = 0.4f
        @JvmStatic
        val LiquidBulletType.tinted: ArrayList<LiquidBulletType>
            get() = LiquidBullets.getOrPut(this) {
                ArrayList(3) {
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
        @JvmStatic
        val Registry: HashMap<BulletType, ArrayList<BulletType>> = HashMap()
        @JvmStatic
        fun BulletType.registerRGB(register: BulletType.() -> Triple<BulletType, BulletType, BulletType>) {
            val (r, g, b) = register()
            Registry[this] = arrayListOf(r, g, b)
        }
        @JvmStatic
        fun BulletType.registerRGBArrayList(register: BulletType.() -> ArrayList<BulletType>) {
            Registry[this] = register()
        }
        /**
         * @param registerGen a generator which accepts 0(red), 1(green) and 2(blue) and returns the corresponding [BulletType] object.
         */
        @JvmStatic
        fun BulletType.registerRGBGen(registerGen: BulletType.() -> ((Int) -> BulletType)) {
            val gen = registerGen()
            Registry[this] = ArrayList(3, gen)
        }
        @JvmStatic
        fun getRegistered(b: BulletType): ArrayList<BulletType>? =
            Registry[b]
        @JvmStatic
        val RgbSmallEffects = ArrayList(3) {
            shootSmallRGBGen(3.35f, 17f, R.C.PrismRgbFG[it], R.C.PrismRgbBK[it])
        }
        @JvmStatic
        val RgbEffects = ArrayList(3) {
            shootSmallRGBGen(4f, 60f, R.C.PrismRgbFG[it], R.C.PrismRgbBK[it])
        }
        @JvmStatic
        fun shootSmallRGBGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
            Effect(lifetime, clipSize) {
                Draw.color(fg, bk, Color.gray, it.fin())

                Angles.randLenVectors(
                    it.id.toLong(), 8, it.finpow() * 60f, it.rotation, 10f
                ) { x: Float, y: Float ->
                    Fill.circle(it.x + x, it.y + y, 0.65f + it.fout() * 1.5f)
                }
            }
        @JvmStatic
        fun shootRGBGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
            Effect(lifetime, clipSize) {
                Draw.color(fg, bk, Color.gray, it.fin())

                Angles.randLenVectors(
                    it.id.toLong(), 10, it.finpow() * 70f, it.rotation, 10f
                ) { x: Float, y: Float ->
                    Fill.circle(it.x + x, it.y + y, 0.65f + it.fout() * 1.6f)
                }
            }
        @JvmStatic
        val GeneralBullets: HashMap<BulletType, ArrayList<BulletType>> = HashMap()
        @JvmStatic
        val BulletType.tintRedGeneral: ArrayList<BulletType>
            get() = GeneralBullets.getOrPut(this) {
                ArrayList(3) {
                    (this.copy() as BulletType).apply {
                        shootEffect = RgbEffects[it]
                        hitEffect = RgbSmallEffects[0]
                    }
                }
            }
        @JvmStatic
        val IgnoredBullets: HashSet<BulletType> = HashSet()
        @JvmStatic
        val IgnoredClass: HashSet<Class<out BulletType>> = HashSet()
        @JvmStatic
        fun BulletType.ignoreRGB() =
            IgnoredBullets.add(this)

        fun Class<out BulletType>.ignoreRGB() =
            IgnoredClass.add(this)
    }
}

