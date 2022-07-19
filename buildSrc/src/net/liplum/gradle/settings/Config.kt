package net.liplum.gradle.settings

import org.gradle.api.Project
import java.util.*

data class Config(
    val env: String = "prod",
)

object Settings {
    @JvmStatic
    private var all: Properties? = null
    val Project.localProperties: Properties
        get() = all ?: load()
    val Project.local: PropertiesSpec
        get() = PropertiesSpec(localProperties)
    val initialText = """
        cyberio.env=prod
    """.trimIndent()

    private fun Project.load(): Properties {
        val properties = Properties()
        val file = rootDir.resolve("local.properties")
        if (file.exists()) {
            file.inputStream().use { properties.load(it) }
            logger.info("local.properties was found.")
        } else {
            file.writeText(initialText)
            logger.info("local.properties was created.")
        }
        all = properties
        return properties
    }

    val Project.localConfig
        get() = local.run {
            Config(env = this["cyberio.env"] ?: "prod")
        }
}
@JvmInline
value class PropertiesSpec(
    val properties: Properties,
) {
    operator fun get(key: String): String? = properties.getProperty(key)
}