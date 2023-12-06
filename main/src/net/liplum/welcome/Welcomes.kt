package net.liplum.welcome

object Welcomes {
    val default = WelcomeScene(
        id = "Default",
        templateID = "Story",
    )
    val employmentVerification = WelcomeScene(
        id = "EmploymentVerification",
        templateID = "SpecialDishes",
    )
    val discord = WelcomeScene(
        id = "Discord",
        templateID = "DoAction",
        data = mapOf(
            "YesAction" to "OpenLink",
            "Link" to "@link",
        ),
        iconPath = "discord",
        chance = 750,
    )
    val updateECHO = WelcomeScene(
        id = "UpdateECHO",
        templateID = "Update",
        data = mapOf(
            "ActionA" to "UpdateCyberIO",
            "ActionC" to "@SkipThisUpdate",
        ),
        iconPath = "update-icon",
        conditionID = "CheckUpdate",
    )
    val researchProject = WelcomeScene(
        id = "ResearchProject",
        iconPath = "welcome-holo-project",
        conditionID = "SpecialDishes",
    )
    val theShip = WelcomeScene(
        id = "TheSHIP",
    )
    val echo = WelcomeScene(
        id = "ECHO",
    )
    val newBrains = WelcomeScene(
        id = "NewBrains",
        iconPath = "heimdall",
    )
    val adBlock = WelcomeScene(
        id = "AdBlock",
        templateID = "DoAction",
        iconPath = "ad-block",
        data = mapOf(
            "NoAction" to "StopReceiveWelcome",
        ),
        chance = 300,
    )
    val `404NotFound` = WelcomeScene(
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
    )
    val ohNo = WelcomeScene(
        id = "OhNo",
        iconPath = "@error",
        data = mapOf(
            "ShowPoliteWelcome" to false,
        ),
        chance = 200,
    )
    val rateUs = WelcomeScene(
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
    )
    val erekirDetected = WelcomeScene(
        id = "ErekirDetected",
        iconPath = "spec-erekir",
        conditionID = "SpecialDishes",
    )
    val setOutErekir = WelcomeScene(
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
    )
    val cyberionForErekir = WelcomeScene(
        id = "CyberionForErekir",
        conditionID = "SettingsReq",
        iconPath = "cyberion-for-erekir",
        data = mapOf(
            "ShowPoliteWelcome" to true,
            "CExpression" to "@ContentSpecific == erekir && @ShouldShowWelcome",
        ),
    )
    val tips = listOf(
        default,
        employmentVerification,
        discord,
        updateECHO,
        researchProject,
        theShip,
        echo,
        newBrains,
        adBlock,
        `404NotFound`,
        ohNo,
        rateUs,
        erekirDetected,
        setOutErekir,
        cyberionForErekir,
    )
    val id2Tips = tips.associateBy { it.id }
}

operator fun Welcomes.get(id: String) = id2Tips[id] ?: WelcomeScene.Default
inline fun Welcomes.find(filter: (WelcomeScene) -> Boolean): WelcomeScene? {
    return id2Tips.values.find(filter)
}

inline fun Welcomes.findAll(filter: (WelcomeScene) -> Boolean): List<WelcomeScene> {
    return id2Tips.values.filter(filter)
}