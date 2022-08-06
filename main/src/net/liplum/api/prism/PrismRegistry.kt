package net.liplum.api.prism

import arc.graphics.Color
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.type.UnitType
import net.liplum.R
import net.liplum.blocks.prism.RgbList
import net.liplum.common.util.directSuperClass

enum class PrismData {
    Duplicate
}

class PrismDataColor : Color {
    constructor() : super()
    constructor(rgba8888: Int) : super(rgba8888)
    constructor(r: Float, g: Float, b: Float, a: Float) : super(r, g, b, a)
    constructor(r: Float, g: Float, b: Float) : super(r, g, b)
    constructor(color: Color) : super(color)

    companion object {
        val RgbFG = R.C.PrismRgbFG.map { PrismDataColor(it) }
        val RgbBK = R.C.PrismRgbBK.map { PrismDataColor(it) }
    }
}

object PrismRegistry {
    @JvmStatic
    val Registry: HashMap<BulletType, List<BulletType>> = HashMap()
    @JvmStatic
    fun <T : BulletType> T.registerRGB(register: T.() -> Triple<T, T, T>) {
        val (r, g, b) = register()
        Registry[this] = arrayListOf(r, g, b)
    }
    @JvmStatic
    val Any?.isDuplicate: Boolean
        get() = this == PrismData.Duplicate || this is PrismDataColor
    @JvmStatic
    fun Bullet.setDuplicate() {
        when (this.type) {
            else -> this.data = PrismData.Duplicate
        }
    }
    @JvmStatic
    fun <T : BulletType> T.registerRGBs(register: T.() -> List<T>) {
        Registry[this] = this.register()
    }
    @JvmStatic
    fun <T : BulletType> T.registerRGBIndex(register: T.(Int) -> T) {
        Registry[this] = RgbList { register(it) }
    }
    /**
     * @param registerGen a generator which accepts 0(red), 1(green) and 2(blue) and returns the corresponding [BulletType] object.
     */
    @JvmStatic
    fun BulletType.registerRGBGen(registerGen: BulletType.() -> ((Int) -> BulletType)) {
        val gen = registerGen()
        Registry[this] = RgbList(gen)
    }
    @JvmStatic
    fun getRegistered(b: BulletType): List<BulletType>? =
        Registry[b]
}

object PrismBlackList {
    @JvmStatic
    val BlackList: HashSet<BulletType> = HashSet()
    @JvmStatic
    val PrismClzNameBlackList: HashSet<String> = HashSet()
    @JvmStatic
    val BulletType.canDisperse: Boolean
        get() = this !in BlackList &&
                this.directSuperClass.name !in PrismClzNameBlackList
    @JvmStatic
    fun String.banNameInPrism() {
        PrismClzNameBlackList.add(this)
    }
    @JvmStatic
    fun BulletType.banInPrism() {
        BlackList.add(this)
    }
    @JvmStatic
    fun UnitType.banInWeapon(weaponName: String) {
        this.weapons.find {
            it.name == weaponName
        }?.bullet?.banInPrism()
    }
    @JvmStatic
    fun UnitType.banInWeapons(vararg weaponNames: String) {
        for (name in weaponNames) {
            this.banInWeapon(name)
        }
    }
    @JvmStatic
    fun UnitType.banAllInWeapons() {
        for (weapon in this.weapons) {
            weapon.bullet?.banInPrism()
        }
    }
}
