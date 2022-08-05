package net.liplum.update

import arc.Core
import arc.util.Http
import arc.util.io.Streams
import arc.util.serialization.Json
import arc.util.serialization.Jval
import arc.util.serialization.Jval.Jformat
import kotlinx.coroutines.*
import mindustry.Vars
import mindustry.ui.dialogs.ModsDialog
import net.liplum.CLog
import net.liplum.CioMod
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.Meta
import net.liplum.Settings
import net.liplum.common.replaceByteBy
import net.liplum.common.util.getMethodBy
import net.liplum.common.util.useFakeHeader
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.HeadlessOnly
import plumy.core.UseReflection
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import kotlin.coroutines.CoroutineContext

@UseReflection
private val ImportModFunc: Method = ModsDialog::class.java.getMethodBy(
    "githubImportMod", String::class.java, Boolean::class.java
)
@UseReflection
private fun ModsDialog.ImportMod(repo: String, isJava: Boolean) {
    ImportModFunc(this, repo, isJava)
}

object Updater : CoroutineScope {
    var latestVersion: Version2 = Meta.DetailedVersion
    var updateInfo = UpdateInfo.X
    var accessJob: Job? = null
    var json = Json()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    val ClientVersionRegex = "(?<=Client:).*".toRegex()
    val ServerVersionRegex = "(?<=Server:).*".toRegex()
    /**
     * Retrieve the update info from given url or file path.
     * It's silent when any exception is raised.
     * So you can call this safely.
     * @param updateInfoFileURL the url for retrieving the update info. Check order: File path ->
     * @param onFailed when it can't fetch the latest version, this will be called.
     */
    fun fetchLatestVersion(
        updateInfoFileURL: String? = Meta.UpdateInfoURL,
        onFailed: (String) -> Unit = {},
    ) {
        val url = updateInfoFileURL ?: Meta.UpdateInfoURL
        CLog.info("Update checking...")
        accessJob = launch(
            CoroutineExceptionHandler { _, e ->
                CLog.err("Can't fetch the latest version because of ${e.javaClass} ${e.message}.")
                onFailed("${e.javaClass} ${e.message}")
            }
        ) {
            val info: String
            val testFile = File(url)
            info = if (testFile.isFile && testFile.exists()) {
                testFile.readText()
            } else {
                URL(updateInfoFileURL).readText()
            }
            analyzeUpdateInfo(info)
            CLog.info("The latest version is $latestVersion")
        }
    }

    fun analyzeUpdateInfo(text: String) {
        val info = json.fromJson(
            UpdateInfo::class.java,
            Jval.read(text).toString(Jformat.plain)
        )
        updateInfo = info
        ClientOnly {
            latestVersion = Version2.valueOf(info.ClientLatest)
        }
        HeadlessOnly {
            latestVersion = Version2.valueOf(info.ServerLatest)
        }
    }
    @HeadlessOnly
    @JvmOverloads
    fun checkHeadlessUpdate(shouldUpdateOverride: Boolean = false) {
        launch {
            accessJob?.join()
            if (requireUpdate) {
                if (Config.AutoUpdate || shouldUpdateOverride) {
                    CLog.info("[Auto-Update ON] Now updating...")
                    updateSelfByReplaceFinding(
                        onFailed = { error ->
                            CLog.err(error)
                        },
                        onSuccess = {
                            Core.app.post {
                                CLog.info("The game will close soon to reload CyberIO.")
                                Core.app.exit()
                            }
                        })
                } else {
                    CLog.info("[Auto-Update OFF] Current version is ${Meta.DetailedVersion} and need to be updated to $latestVersion manually.")
                }
            }
        }
    }
    @ClientOnly
    val curDownloadURL: String
        get() {
            val version = latestVersion.toString()
            return "${Settings.GitHubMirrorUrl}/${Meta.Repo}/releases/download/v$version/CyberIO-$version.jar"
        }
    @JvmStatic
    fun updateSelfByBuiltIn() {
        val modsDialog = Vars.ui.mods
        modsDialog.show()
        modsDialog.ImportMod(Meta.Repo, true)
    }
    @JvmStatic
    fun updateSelfByReplaceFinding(
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onFailed: (String) -> Unit = {},
    ) {
        Http.get(Meta.LatestRelease, {
            val json = Jval.read(it.resultAsString)
            val assets = json["assets"].asArray()
            val asset = assets.find { j: Jval -> j.getString("name").endsWith(".jar") }
            if (asset != null) {
                //grab actual file
                val url = asset.getString("browser_download_url")
                updateSelfByReplace(url, onProgress, onSuccess, onFailed)
            } else {
                onFailed("Jar file wasn't found at this release.")
            }
        }) {
            onFailed("Can't acquire update info $it")
        }
    }
    @JvmStatic
    fun updateSelfByReplace(
        jarUrl: String,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onFailed: (String) -> Unit = {},
    ) {
        Http.get(jarUrl).useFakeHeader().error { e ->
            onFailed("Can't update the latest version$latestVersion $e")
        }.submit { res ->
            downloadAndReplaceLatest(res, onProgress, onSuccess, onFailed)
        }
    }
    @JvmStatic
    fun downloadAndReplaceLatest(
        req: Http.HttpResponse,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onFailed: (String) -> Unit = {},
    ) {
        if (CioMod.jarFile != null) {
            val length = req.contentLength
            val bytes = ByteArrayOutputStream()
            CLog.info("v$latestVersion is downloading.")
            Streams.copyProgress(
                req.resultAsStream,
                bytes,
                length, Streams.defaultBufferSize,
                onProgress
            )
            CLog.info("v$latestVersion downloaded successfully, replacing file.")
            CioMod.jarFile.replaceByteBy(bytes.toByteArray())
            CLog.info("Updated successfully.")
            onSuccess()
        } else {
            onFailed("Jar file not found.")
        }
    }

    val requireUpdate: Boolean
        get() = latestVersion > Meta.DetailedVersion
    val isCurrentBreakUpdate: Boolean
        get() = latestVersion.toString() in updateInfo.BreakUpdateList
    val hasUpdateDescription: Boolean
        get() = updateInfo.Description.isNotEmpty()
    val UpdateDescription: String
        get() = updateInfo.Description
    val matchMinGameVersion: Boolean
        get() = updateInfo.MinGameVersion <= Meta.CurGameVersion
}

class UpdateInfo {
    var Latest = ""
    var ClientLatest = ""
    var ServerLatest = ""
    var Description = ""
    var MinGameVersion = 136
    var BreakUpdateList = emptyArray<String>()

    companion object {
        internal val X = UpdateInfo()
        fun UpdateInfo.isDefault() =
            this == X
    }
}