package net.liplum.blocks.cyberion

/*
TODO: Rewrite this
class CyberionMixerDrawer : DrawMulti {
    @ClientOnly lateinit var LiquidTR: TR
    @JvmField var lightColor: Color = Color.white

    constructor() : super()
    constructor(
        flameColor: Color,
        lightColor: Color
    ) : super(flameColor) {
        this.lightColor = lightColor
    }

    init {
        //flameRadius = 1.8f
    }

    override fun load(block: Block) {
        super.load(block)
        LiquidTR = block.sub("liquid")
    }

    override fun draw(build: GenericCrafter.GenericCrafterBuild) {
        Draw.rect(
            build.block.region, build.x, build.y,
            if (build.block.rotate)
                build.rotdeg()
            else 0f
        )

        Drawf.liquid(
            LiquidTR, build.x, build.y,
            build.liquids[CioLiquids.cyberion] / build.block.liquidCapacity,
            CioLiquids.cyberion.color,
            (build.rotation - 90).toFloat()
        )

        if (build.warmup > 0f && flameColor.a > 0.001f) {
            val g = 0.3f
            val r = 0.06f
            val cr = Mathf.random(0.1f)
            Draw.z(Layer.block + 0.01f)
            val random = Mathf.randomSeedRange(build.pos().toLong(), 12f)
            Draw.alpha(build.warmup * 0.8f)
            Draw.rect(top, build.x, build.y)
            Draw.alpha((1f - g + Mathf.absin(Time.time + random, 8f, g) + Mathf.random(r) - r) * build.warmup)
            Draw.tint(flameColor)
            Fill.circle(
                build.x,
                build.y,
                flameRadius + Mathf.absin(Time.time + random, flameRadiusScl, flameRadiusMag) + cr
            )
            Draw.color(lightColor)
            Draw.alpha(build.warmup * 0.8f)
            Fill.circle(
                build.x,
                build.y,
                flameRadiusIn + Mathf.absin(Time.time + random, flameRadiusScl, flameRadiusInMag) + cr
            )
            Draw.color()
        }
    }
}*/
