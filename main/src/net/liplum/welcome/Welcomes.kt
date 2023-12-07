package net.liplum.welcome

import net.liplum.Settings

object Welcomes {
    val default = WelcomeScene(
        id = "Default",
        template = WelcomeTemplates.Story(),
    )
    val employmentVerification = WelcomeScene(
        id = "EmploymentVerification",
        condition = WelcomeConditions.SpecialDishes,
    )
    val discord = WelcomeScene(
        id = "Discord",
        template = WelcomeTemplates.DoAction(
            yesAction = WelcomeActions.OpenLink("@link"),
        ),
        iconPath = "discord",
        chance = 750,
    )
    val updateECHO = WelcomeScene(
        id = "UpdateECHO",
        template = WelcomeTemplates.UpdateCyberIO,
        iconPath = "update-icon",
        condition = WelcomeConditions.CheckUpdate,
    )
    val researchProject = WelcomeScene(
        id = "ResearchProject",
        iconPath = "welcome-holo-project",
        condition = WelcomeConditions.SpecialDishes,
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
        template = WelcomeTemplates.DoAction(
            noAction = WelcomeActions.StopReceiveWelcome,
        ),
        iconPath = "ad-block",
        chance = 300,
    )
    val `404NotFound` = WelcomeScene(
        id = "404NotFound",
        template = WelcomeTemplates.PlainText(
            fontSize = 1.2f,
            showPoliteWelcome = false,
        ),
        condition = WelcomeConditions.Expr {
            Settings.ClickWelcomeTimes > 5 && Settings.ShouldShowWelcome
        },
        chance = 200,
    )
    val ohNo = WelcomeScene(
        id = "OhNo",
        iconPath = "@error",
        template = WelcomeTemplates.Story(
            showPoliteWelcome = false,
        ),
        chance = 200,
    )
    val rateUs = WelcomeScene(
        id = "RateUs",
        template = WelcomeTemplates.RateStar(
            starSize = 60f,
            starNumber = 5,
            activeStarIconPath = "star",
            inactiveStarIconPath = "star-inactive",
        ),
        iconPath = "@error",
        chance = 200,
    )
    val erekirDetected = WelcomeScene(
        id = "ErekirDetected",
        iconPath = "spec-erekir",
        condition = WelcomeConditions.SpecialDishes,
    )
    val setOutErekir = WelcomeScene(
        id = "SetOutErekir",
        condition = WelcomeConditions.Expr {
            Settings.ContentSpecific == "vanilla" && Settings.ShouldShowWelcome
        },
        iconPath = "spec-erekir",
        template = WelcomeTemplates.DoAction(
            yesAction = WelcomeActions.Navigation("/CyberIO/ContentSpecific"),
        ),
    )
    val cyberionForErekir = WelcomeScene(
        id = "CyberionForErekir",
        iconPath = "cyberion-for-erekir",
        template = WelcomeTemplates.Story(),
        condition = WelcomeConditions.Expr {
            Settings.ContentSpecific == "erekir" && Settings.ShouldShowWelcome
        },
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