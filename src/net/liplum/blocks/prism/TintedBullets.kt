package net.liplum.blocks.prism

import arc.graphics.Color
import mindustry.entities.bullet.*
import net.liplum.R

class TintedBullets {
    companion object {
        @JvmStatic
        val RedBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val GreenBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val BlueBasicBullets: HashMap<BasicBulletType, BasicBulletType> = HashMap()
        @JvmStatic
        val BasicBulletType.tintRed: BasicBulletType
            get() = RedBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismRedFG
                    backColor = R.C.PrismRedBK
                }
            }
        @JvmStatic
        val BasicBulletType.tintGreen: BasicBulletType
            get() = GreenBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismGreenFG
                    backColor = R.C.PrismGreenBK
                }
            }
        @JvmStatic
        val BasicBulletType.tintBlue: BasicBulletType
            get() = BlueBasicBullets.getOrPut(this) {
                (this.copy() as BasicBulletType).apply {
                    frontColor = R.C.PrismBlueFG
                    backColor = R.C.PrismBlueBK
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
    }
}