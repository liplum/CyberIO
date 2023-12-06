package net.liplum.welcome

object Welcomes {
    val tips = listOf(
        WelcomeTip(
            id = "Default",
            templateID = "Story",
        ),
        WelcomeTip(
            id = "EmploymentVerification",
            templateID = "SpecialDishes",
        ),
        WelcomeTip(
            id = "Discord",
            templateID = "DoAction",
            data = mapOf(
                "YesAction" to "OpenLink",
                "Link" to "@link",
            ),
            iconPath = "discord",
            chance = 750,
        ),
        WelcomeTip(
            id = "UpdateECHO",
            templateID = "Update",
            data = mapOf(
                "ActionA" to "UpdateCyberIO",
                "ActionC" to "@SkipThisUpdate",
            ),
            iconPath = "update-icon",
            conditionID = "CheckUpdate",
        ),
        WelcomeTip(
            id = "ResearchProject",
            iconPath = "welcome-holo-project",
            conditionID = "SpecialDishes",
        ),
        WelcomeTip(
            id = "TheSHIP",
        ),
        WelcomeTip(
            id = "ECHO",
        ),
        WelcomeTip(
            id = "NewBrains",
            iconPath = "heimdall",
        ),
        WelcomeTip(
            id = "AdBlock",
            templateID = "DoAction",
            iconPath = "ad-block",
            data = mapOf(
                "NoAction" to "StopReceiveWelcome",
            ),
            chance = 300,
        ),
        WelcomeTip(
            id = "404NotFound",
            templateID = "PlainText",
            iconPath = "ad-block",
            conditionID = "SettingsReq",
            data = mapOf(
                "FontSize" to 1.2,
                "ShowPoliteWelcome" to false,
                "CExpression" to "@ClickWelcomeTimes > 5 && @ShouldShowWelcome",
            ),
            chance = 200,
        ),
        WelcomeTip(
            id = "OhNo",
            iconPath = "@error",
            data = mapOf(
                "ShowPoliteWelcome" to false,
            ),
            chance = 200,
        ),
        WelcomeTip(
            id = "RateUs",
            templateID = "RateStar",
            iconPath = "@error",
            data = mapOf(
                "StarNumber" to 5,
                "InactiveStarIconPath" to "star-inactive",
                "ActiveStarIconPath" to "star",
                "StarSize" to 60.0,
            ),
            chance = 200,
        ),
        WelcomeTip(
            id = "ErekirDetected",
            iconPath = "spec-erekir",
            conditionID = "SpecialDishes",
        ),
        WelcomeTip(
            id = "SetOutErekir",
            templateID = "DoAction",
            conditionID = "SettingsReq",
            iconPath = "spec-erekir",
            data = mapOf(
                "ShowPoliteWelcome" to true,
                "CExpression" to "@ContentSpecific == vanilla && @ShouldShowWelcome",
                "YesAction" to "Navigation",
                "Locator" to "/CyberIO/ContentSpecific",
            ),
        ),
        WelcomeTip(
            id = "CyberionForErekir",
            conditionID = "SettingsReq",
            iconPath = "cyberion-for-erekir",
            data = mapOf(
                "ShowPoliteWelcome" to true,
                "CExpression" to "@ContentSpecific == erekir && @ShouldShowWelcome",
            ),
        ),
    )
    val id2Tips = tips.associateBy { it.id }

}

operator fun Welcomes.get(id: String) = id2Tips[id] ?: WelcomeTip.Default
inline fun Welcomes.find(filter: (WelcomeTip) -> Boolean): WelcomeTip? {
    return id2Tips.values.find(filter)
}

inline fun Welcomes.findAll(filter: (WelcomeTip) -> Boolean): List<WelcomeTip> {
    return id2Tips.values.filter(filter)
}