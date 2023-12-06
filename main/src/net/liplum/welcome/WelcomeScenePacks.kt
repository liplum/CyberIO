package net.liplum.welcome

object WelcomeScenePacks {
    val common = WelcomeScenePack(
        default = Welcomes.default,
        scenes = listOf(
            Welcomes.updateECHO,
            Welcomes.theShip,
            Welcomes.echo,
            Welcomes.newBrains,
            Welcomes.discord,
            Welcomes.adBlock,
            Welcomes.`404NotFound`,
            Welcomes.ohNo,
            Welcomes.rateUs,
        )
    )
    val v2 = WelcomeScenePack(
        default = Welcomes.employmentVerification,
        scenes = listOf(
            Welcomes.employmentVerification,
        )
    )
    val v3 = common.inherit(
        default = Welcomes.researchProject,
        scenes = listOf(
            Welcomes.employmentVerification,
        )
    )
    val v4 = common.inherit(
        default = Welcomes.erekirDetected,
        scenes = listOf(
            Welcomes.erekirDetected,
            Welcomes.setOutErekir,
        )
    )
    val v4_1 = v4.inherit(
        scenes = listOf(
            Welcomes.cyberionForErekir,
        )
    )
    val v5 = v4_1.inherit()
    val v5_1 = v5.inherit()
}