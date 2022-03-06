package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Angles
import mindustry.entities.Effect
import mindustry.entities.bullet.*
import mindustry.gen.Bullet
import net.liplum.R
import net.liplum.utils.copyFrom

class TintedBullets {
    companion object {
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
        fun tintBulletRGB(red: Bullet, green: Bullet, blue: Bullet) {
            val redType = red.type
            val greenType = green.type
            val blueType = blue.type
            if (redType is BasicBulletType && greenType is BasicBulletType && blueType is BasicBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is ShrapnelBulletType && greenType is ShrapnelBulletType && blueType is ShrapnelBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is LaserBulletType && greenType is LaserBulletType && blueType is LaserBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is ContinuousLaserBulletType && greenType is ContinuousLaserBulletType && blueType is ContinuousLaserBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is FireBulletType && greenType is FireBulletType && blueType is FireBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is LiquidBulletType && greenType is LiquidBulletType && blueType is LiquidBulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            } else if (redType is BulletType && greenType is BulletType && blueType is BulletType) {
                red.type(redType.tintRed)
                blue.type(blueType.tintBlue)
                green.type(greenType.tintGreen)
                return
            }
        }
        @JvmStatic
        val RedBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val GreenBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val BlueBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val BasicTintLerp = 0.4f
        @JvmStatic
        val BasicBulletType.tintRed: BasicBulletType
            get() = RedBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismRedFG.lerp(
                        this@tintRed.frontColor, BasicTintLerp
                    )
                    backColor = R.C.PrismRedBK.lerp(
                        this@tintRed.backColor, BasicTintLerp
                    )
                }
            }
        @JvmStatic
        val BasicBulletType.tintGreen: BasicBulletType
            get() = GreenBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismGreenFG.lerp(
                        this@tintGreen.frontColor, BasicTintLerp
                    )
                    backColor = R.C.PrismGreenBK.lerp(
                        this@tintGreen.backColor, BasicTintLerp
                    )
                }
            }
        @JvmStatic
        val BasicBulletType.tintBlue: BasicBulletType
            get() = BlueBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismBlueFG.lerp(
                        this@tintBlue.frontColor, BasicTintLerp
                    )
                    backColor = R.C.PrismBlueBK.lerp(
                        this@tintBlue.backColor, BasicTintLerp
                    )
                }
            }
        @JvmStatic
        val RedShrapnelBullets: HashMap<ShrapnelBulletType, ShrapnelBulletType> = HashMap()
        @JvmStatic
        val GreenShrapnelBullets: HashMap<ShrapnelBulletType, ShrapnelBulletType> = HashMap()
        @JvmStatic
        val BlueShrapnelBullets: HashMap<ShrapnelBulletType, ShrapnelBulletType> = HashMap()
        @JvmStatic
        val ShrapnelBulletType.tintRed: ShrapnelBulletType
            get() = RedShrapnelBullets.getOrPut(this) {
                (this.copy() as ShrapnelBulletType).apply {
                    fromColor = R.C.PrismRedFG
                    toColor = R.C.PrismRedBK
                }
            }
        @JvmStatic
        val ShrapnelBulletType.tintGreen: ShrapnelBulletType
            get() = GreenShrapnelBullets.getOrPut(this) {
                (this.copy() as ShrapnelBulletType).apply {
                    fromColor = R.C.PrismGreenFG
                    toColor = R.C.PrismGreenBK
                }
            }
        @JvmStatic
        val ShrapnelBulletType.tintBlue: ShrapnelBulletType
            get() = BlueShrapnelBullets.getOrPut(this) {
                (this.copy() as ShrapnelBulletType).apply {
                    fromColor = R.C.PrismBlueFG
                    toColor = R.C.PrismBlueBK
                }
            }
        @JvmStatic
        val RedLightningBullets: HashMap<LightningBulletType, LightningBulletType> = HashMap()
        @JvmStatic
        val GreenLightningBullets: HashMap<LightningBulletType, LightningBulletType> = HashMap()
        @JvmStatic
        val BlueLightningBullets: HashMap<LightningBulletType, LightningBulletType> = HashMap()
        @JvmStatic
        val LightningBulletType.tintRed: LightningBulletType
            get() = RedLightningBullets.getOrPut(this) {
                (this.copy() as LightningBulletType).apply {
                    lightningColor = R.C.PrismRedFG
                }
            }
        @JvmStatic
        val LightningBulletType.tintGreen: LightningBulletType
            get() = GreenLightningBullets.getOrPut(this) {
                (this.copy() as LightningBulletType).apply {
                    lightningColor = R.C.PrismGreenFG
                }
            }
        @JvmStatic
        val LightningBulletType.tintBlue: LightningBulletType
            get() = BlueLightningBullets.getOrPut(this) {
                (this.copy() as LightningBulletType).apply {
                    lightningColor = R.C.PrismBlueFG
                }
            }
        @JvmStatic
        val RedSapBullets: HashMap<SapBulletType, SapBulletType> = HashMap()
        @JvmStatic
        val GreenSapBullets: HashMap<SapBulletType, SapBulletType> = HashMap()
        @JvmStatic
        val BlueSapBullets: HashMap<SapBulletType, SapBulletType> = HashMap()
        @JvmStatic
        val SapBulletType.tintRed: SapBulletType
            get() = RedSapBullets.getOrPut(this) {
                (this.copy() as SapBulletType).apply {
                    color = R.C.PrismRedFG
                }
            }
        @JvmStatic
        val SapBulletType.tintGreen: SapBulletType
            get() = GreenSapBullets.getOrPut(this) {
                (this.copy() as SapBulletType).apply {
                    color = R.C.PrismGreenFG
                }
            }
        @JvmStatic
        val SapBulletType.tintBlue: SapBulletType
            get() = BlueSapBullets.getOrPut(this) {
                (this.copy() as SapBulletType).apply {
                    color = R.C.PrismBlueFG
                }
            }
        @JvmStatic
        val RedFireBullets: HashMap<FireBulletType, FireBulletType> = HashMap()
        @JvmStatic
        val GreenFireBullets: HashMap<FireBulletType, FireBulletType> = HashMap()
        @JvmStatic
        val BlueFireBullets: HashMap<FireBulletType, FireBulletType> = HashMap()
        @JvmStatic
        val FireBulletType.tintRed: FireBulletType
            get() = RedFireBullets.getOrPut(this) {
                (this.copy() as FireBulletType).apply {
                    colorFrom = R.C.PrismRedFG
                    colorMid = R.C.PrismRedFG
                    colorTo = R.C.PrismRedBK
                }
            }
        @JvmStatic
        val FireBulletType.tintGreen: FireBulletType
            get() = GreenFireBullets.getOrPut(this) {
                (this.copy() as FireBulletType).apply {
                    colorFrom = R.C.PrismGreenFG
                    colorMid = R.C.PrismGreenFG
                    colorTo = R.C.PrismGreenBK
                }
            }
        @JvmStatic
        val FireBulletType.tintBlue: FireBulletType
            get() = BlueFireBullets.getOrPut(this) {
                (this.copy() as FireBulletType).apply {
                    colorFrom = R.C.PrismBlueFG
                    colorMid = R.C.PrismBlueFG
                    colorTo = R.C.PrismBlueBK
                }
            }
        @JvmStatic
        val RedLaserBullets: HashMap<LaserBulletType, LaserBulletType> = HashMap()
        @JvmStatic
        val GreenLaserBullets: HashMap<LaserBulletType, LaserBulletType> = HashMap()
        @JvmStatic
        val BlueLaserBullets: HashMap<LaserBulletType, LaserBulletType> = HashMap()
        @JvmStatic
        val RedLaserColors = arrayOf(R.C.PrismRedFG, R.C.PrismRedBK, Color.white)
        @JvmStatic
        val GreenLaserColors = arrayOf(R.C.PrismGreenFG, R.C.PrismGreenBK, Color.white)
        @JvmStatic
        val BlueLaserColors = arrayOf(R.C.PrismBlueFG, R.C.PrismBlueBK, Color.white)
        @JvmStatic
        val LaserBulletType.tintRed: LaserBulletType
            get() = RedLaserBullets.getOrPut(this) {
                (this.copy() as LaserBulletType).apply {
                    colors = RedLaserColors
                }
            }
        @JvmStatic
        val LaserBulletType.tintGreen: LaserBulletType
            get() = GreenLaserBullets.getOrPut(this) {
                (this.copy() as LaserBulletType).apply {
                    colors = GreenLaserColors
                }
            }
        @JvmStatic
        val LaserBulletType.tintBlue: LaserBulletType
            get() = BlueLaserBullets.getOrPut(this) {
                (this.copy() as LaserBulletType).apply {
                    colors = BlueLaserColors
                }
            }
        @JvmStatic
        val RedContinuousLaserBullets: HashMap<ContinuousLaserBulletType, ContinuousLaserBulletType> = HashMap()
        @JvmStatic
        val GreenContinuousLaserBullets: HashMap<ContinuousLaserBulletType, ContinuousLaserBulletType> = HashMap()
        @JvmStatic
        val BlueContinuousLaserBullets: HashMap<ContinuousLaserBulletType, ContinuousLaserBulletType> = HashMap()
        @JvmStatic
        val RedContinuousLaserColors = arrayOf(R.C.PrismRedFG, R.C.PrismRedFG, R.C.PrismRedBK, Color.white)
        @JvmStatic
        val GreenContinuousLaserColors = arrayOf(R.C.PrismGreenFG, R.C.PrismGreenFG, R.C.PrismGreenBK, Color.white)
        @JvmStatic
        val BlueContinuousLaserColors = arrayOf(R.C.PrismBlueFG, R.C.PrismBlueFG, R.C.PrismBlueBK, Color.white)
        @JvmStatic
        val ContinuousLaserBulletType.tintRed: ContinuousLaserBulletType
            get() = RedContinuousLaserBullets.getOrPut(this) {
                (this.copy() as ContinuousLaserBulletType).apply {
                    colors = RedContinuousLaserColors
                }
            }
        @JvmStatic
        val ContinuousLaserBulletType.tintGreen: ContinuousLaserBulletType
            get() = GreenContinuousLaserBullets.getOrPut(this) {
                (this.copy() as ContinuousLaserBulletType).apply {
                    colors = GreenContinuousLaserColors
                }
            }
        @JvmStatic
        val ContinuousLaserBulletType.tintBlue: ContinuousLaserBulletType
            get() = BlueContinuousLaserBullets.getOrPut(this) {
                (this.copy() as ContinuousLaserBulletType).apply {
                    colors = BlueContinuousLaserColors
                }
            }
        @JvmStatic
        val RedLiquidBullets: HashMap<LiquidBulletType, LiquidBulletType> = HashMap()
        @JvmStatic
        val GreenLiquidBullets: HashMap<LiquidBulletType, LiquidBulletType> = HashMap()
        @JvmStatic
        val BlueLiquidBullets: HashMap<LiquidBulletType, LiquidBulletType> = HashMap()
        @JvmStatic
        val LiquidTintLerp = 0.4f
        @JvmStatic
        val LiquidBulletType.tintRed: LiquidBulletType
            get() = RedLiquidBullets.getOrPut(this) {
                try {
                    val b = TintLiquidBulletT(this.liquid)
                    b.copyFrom(this)
                    b.apply {
                        tintColor = R.C.PrismRedFG.cpy().lerp(
                            this@tintRed.liquid.color, LiquidTintLerp
                        )
                    }
                } catch (e: Exception) {
                    this
                }
            }
        @JvmStatic
        val LiquidBulletType.tintGreen: LiquidBulletType
            get() = GreenLiquidBullets.getOrPut(this) {
                try {
                    val b = TintLiquidBulletT(this.liquid)
                    b.copyFrom(this)
                    b.apply {
                        tintColor = R.C.PrismGreenFG.cpy().lerp(
                            this@tintGreen.liquid.color, LiquidTintLerp
                        )
                    }
                } catch (e: Exception) {
                    this
                }
            }
        @JvmStatic
        val LiquidBulletType.tintBlue: LiquidBulletType
            get() = BlueLiquidBullets.getOrPut(this) {
                try {
                    val b = TintLiquidBulletT(this.liquid)
                    b.copyFrom(this)
                    b.apply {
                        tintColor = R.C.PrismBlueFG.cpy().lerp(
                            this@tintBlue.liquid.color, LiquidTintLerp
                        )
                    }
                } catch (e: Exception) {
                    this
                }
            }
        @JvmStatic
        val RedBullets: HashMap<BulletType, BulletType> = HashMap()
        @JvmStatic
        val GreenBullets: HashMap<BulletType, BulletType> = HashMap()
        @JvmStatic
        val BlueBullets: HashMap<BulletType, BulletType> = HashMap()
        @JvmStatic
        val RgbSmallEffects = arrayOf(
            shootSmallRGBGen(3.35f, 17f, R.C.PrismRedFG, R.C.PrismRedBK),
            shootSmallRGBGen(3.35f, 17f, R.C.PrismGreenFG, R.C.PrismGreenBK),
            shootSmallRGBGen(3.35f, 17f, R.C.PrismBlueFG, R.C.PrismBlueBK),
        )
        @JvmStatic
        val RgbEffects = arrayOf(
            shootRGBGen(4f, 60f, R.C.PrismRedFG, R.C.PrismRedBK),
            shootRGBGen(4f, 60f, R.C.PrismGreenFG, R.C.PrismGreenBK),
            shootRGBGen(4f, 60f, R.C.PrismBlueFG, R.C.PrismBlueBK),
        )
        @JvmStatic
        val BulletType.tintRed: BulletType
            get() = RedBullets.getOrPut(this) {
                (this.copy() as BulletType).apply {
                    shootEffect = RgbEffects[0]
                    hitEffect = RgbSmallEffects[0]
                }
            }
        @JvmStatic
        val BulletType.tintGreen: BulletType
            get() = GreenBullets.getOrPut(this) {
                (this.copy() as BulletType).apply {
                    shootEffect = RgbEffects[1]
                    hitEffect = RgbSmallEffects[1]
                }
            }
        @JvmStatic
        val BulletType.tintBlue: BulletType
            get() = BlueBullets.getOrPut(this) {
                (this.copy() as BulletType).apply {
                    shootEffect = RgbEffects[2]
                    hitEffect = RgbSmallEffects[2]
                }
            }
    }
}