package net.liplum.update

import arc.Core
import arc.util.Http
import arc.util.io.Streams
import arc.util.serialization.Jval
import kotlinx.coroutines.*
import mindustry.Vars
import mindustry.ui.dialogs.ModsDialog
import net.liplum.*
import net.liplum.utils.getMethodBy
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import kotlin.coroutines.CoroutineContext

private val ImportModFunc: Method = ModsDialog::class.java.getMethodBy(
    "githubImportMod", String::class.java, Boolean::class.java
)

private fun ModsDialog.ImportMod(repo: String, isJava: Boolean) {
    ImportModFunc(this, repo, isJava)
}

object Updater : CoroutineScope {
    var latestVersion: Version2 = Meta.DetailedVersion
    var accessJob: Job? = null
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    val ClientVersionRegex = "(?<=Client:).*".toRegex()
    val ServerVersionRegex = "(?<=Server:).*".toRegex()
    fun fetchLatestVersion(updateInfoFileURL: String) {
        if (Vars.headless || Settings.ShowUpdate) {
            Clog.info("Update checking...")
            accessJob = launch(
                CoroutineExceptionHandler { _, e ->
                    Clog.err("Can't fetch the latest version because of ${e.javaClass} ${e.message}.")
                }
            ) {
                val info: String
                val testFile = File(updateInfoFileURL)
                info = if (testFile.isFile && testFile.exists()) {
                    testFile.readText()
                } else {
                    URL(updateInfoFileURL).readText()
                }
                val allInfos = info.split('\n')
                /*
                    Removed since 3.3
                    val versionInfo = allInfos[0]
                */
                ClientOnly {
                    val clientV = allInfos[1]// Client
                    val client = ClientVersionRegex.find(clientV)
                    if (client != null)
                        latestVersion = runCatching {
                            Version2.valueOf(client.value)
                        }.getOrDefault(Meta.DetailedVersion)
                }
                HeadlessOnly {
                    val serverV = allInfos[2]// Server
                    val server = ServerVersionRegex.find(serverV)
                    if (server != null)
                        latestVersion = runCatching {
                            Version2.valueOf(server.value)
                        }.getOrDefault(Meta.DetailedVersion)
                }

                Clog.info("The latest version is $latestVersion")
            }
        }
    }
    @HeadlessOnly
    @JvmOverloads
    fun checkHeadlessUpdate(shouldUpdateOverride: Boolean = false) {
        launch {
            accessJob?.join()
            if (requireUpdate) {
                if (Config.AutoUpdate || shouldUpdateOverride) {
                    Clog.info("[Auto-Update ON] Now updating...")
                    updateSelfByReplace(
                        onFailed = { error ->
                            Clog.err(error)
                        },
                        onSuccess = {
                            Core.app.post {
                                Clog.info("The game will close soon to reload CyberIO.")
                                Core.app.exit()
                            }
                        })
                } else {
                    Clog.info("[Auto-Update OFF] Current version is ${Meta.DetailedVersion} and need to be updated to $latestVersion manually.")
                }
            }
        }
    }
    @JvmStatic
    fun updateSelfByBuiltIn() {
        val modsDialog = Vars.ui.mods
        modsDialog.show()
        modsDialog.ImportMod(Meta.Repo, true)
    }
    @JvmStatic
    fun updateSelfByReplace(
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        Http.get(Meta.LatestRelease, {
            val json = Jval.read(it.resultAsString)
            val assets = json["assets"].asArray()
            val asset = assets.find { j: Jval -> j.getString("name").endsWith(".jar") }
            if (asset != null) {
                //grab actual file
                val url = asset.getString("browser_download_url")
                Http.get(url, { res ->
                    downloadLatest(res, onProgress, onSuccess, onFailed)
                }) { e ->
                    onFailed("Can't update the latest version$latestVersion $e")
                }
            } else {
                onFailed("Jar file wasn't found at this release.")
            }
        }) {
            onFailed("Can't acquire update info $it")
        }
    }
    @JvmStatic
    fun downloadLatest(
        req: Http.HttpResponse,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (CioMod.jarFile != null) {
            val length = req.contentLength
            val bytes = ByteArrayOutputStream()
            Clog.info("v$latestVersion is downloading.")
            Streams.copyProgress(
                req.resultAsStream,
                bytes,
                length, Streams.defaultBufferSize,
                onProgress
            )
            Clog.info("v$latestVersion downloaded successfully, replacing file.")
            Streams.copy(
                bytes.toByteArray().inputStream(),
                CioMod.jarFile.outputStream()
            )
            Clog.info("Updated successfully.")
            onSuccess()
        } else {
            onFailed("Jar file not found.")
        }
    }

    val requireUpdate: Boolean
        get() = latestVersion > Meta.DetailedVersion
}
