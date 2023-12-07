package net.liplum.welcome

object WelcomeScenePacks {
    val common = WelcomeScenePack(
        default = WelcomeScene.Default,
        scenes = listOf(
            WelcomeScenes.updateECHO,
            WelcomeScenes.theShip,
            WelcomeScenes.echo,
            WelcomeScenes.newBrains,
            WelcomeScenes.discord,
            WelcomeScenes.adBlock,
            WelcomeScenes.`404NotFound`,
            WelcomeScenes.ohNo,
            WelcomeScenes.rateUs,
        )
    )
    val v2 = WelcomeScenePack(
        default = WelcomeScenes.employmentVerification,
        scenes = listOf(
            WelcomeScenes.employmentVerification,
        )
    )
    val v3 = common.inherit(
        default = WelcomeScenes.researchProject,
        scenes = listOf(
            WelcomeScenes.employmentVerification,
        )
    )
    val v4 = common.inherit(
        default = WelcomeScenes.erekirDetected,
        scenes = listOf(
            WelcomeScenes.erekirDetected,
            WelcomeScenes.setOutErekir,
        )
    )
    val v4_1 = v4.inherit(
        scenes = listOf(
            WelcomeScenes.cyberionForErekir,
        )
    )
    val v5 = v4_1.inherit()
    val v5_1 = v5.inherit()
}