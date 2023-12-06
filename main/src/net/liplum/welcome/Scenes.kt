package net.liplum.welcome

object Scenes {
    val common = WelcomeTipPack(
        scenes = listOf(
            "UpdateECHO",
            "TheSHIP",
            "ECHO",
            "NewBrains",
            "Discord",
            "AdBlock",
            "404NotFound",
            "OhNo",
            "RateUs",
            "SetOutErekir",
            "CyberionForErekir"
        )
    )
    val v2 = WelcomeTipPack(
        default = "EmploymentVerification",
        scenes = listOf(
            "EmploymentVerification"
        )
    )
    val v3 = common.inherit(
        default = "ResearchProject",
        scenes = listOf(
            "EmploymentVerification"
        )
    )
    val v4 = common.inherit(
        default = "ErekirDetected",
        scenes = listOf(
            "ErekirDetected",
            "SetOutErekir"
        )
    )
    val v4_1 = v4.inherit(
        scenes = listOf(
            "CyberionForErekir"
        )
    )
    val v5 = v4_1.inherit()
    val v5_1 = v5.inherit()
}