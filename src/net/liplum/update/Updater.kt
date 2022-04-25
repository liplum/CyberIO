package net.liplum.update

import arc.Core
import arc.util.Http
import arc.util.Log
import arc.util.io.Streams
import arc.util.serialization.Jval
import kotlinx.coroutines.*
import mindustry.Vars
import mindustry.ui.dialogs.ModsDialog
import net.liplum.*
import net.liplum.utils.getMethodBy
import java.io.ByteArrayOutputStream
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

    fun fetchLatestVersion() {
        if (Vars.headless || Settings.ShowUpdate) {
            Log.info("CyberIO update checking...")
            accessJob = launch(
                CoroutineExceptionHandler { _, e ->
                    Log.err("Can't fetch the latest version of CyberIO because of ${e.javaClass} ${e.message}.")
                }
            ) {
                val url = URL(Meta.UpdateInfo)
                val bytes = url.readBytes()
                val updateInfo = String(bytes)
                val allInfos = updateInfo.split('\n')
                val versionInfo = allInfos[0]
                latestVersion = runCatching {
                    Version2.valueOf(versionInfo)
                }.getOrDefault(Meta.DetailedVersion)
                Log.info("The latest CyberIO version is $latestVersion")
            }
        }
    }
    @HeadlessOnly
    fun checkHeadlessUpdate() {
        launch {
            accessJob?.join()
            Config.loadConfigJob?.join()
            if (requireUpdate) {
                if (Config.AutoUpdate) {
                    Log.info("[Auto-Update ON] Now updating...")
                    updateSelfByReplace(onSuccess = {
                        Core.app.post {
                            Log.info("The game will close soon to reload CyberIO.")
                            Core.app.exit()
                        }
                    })
                } else {
                    Log.info("[Auto-Update OFF] Current CyberIO is ${Meta.DetailedVersion} and need to be updated to $latestVersion manually.")
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
                    Log.err("Can't update CyberIO's latest version$latestVersion", e)
                    onFailed("${e.javaClass.name} ${e.message}")
                }
            } else {
                Log.err("CyberIO has no jar file at this release.")
                onFailed("CyberIO has no jar file at this release.")
            }
        }) {
            Log.err("Can't acquire CyberIO info", it)
            onFailed("Can't acquire CyberIO info ${it.message}")
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
            Streams.copyProgress(
                req.resultAsStream,
                bytes,
                length, Streams.defaultBufferSize,
                onProgress
            )
            Streams.copy(
                bytes.toByteArray().inputStream(),
                CioMod.jarFile.outputStream()
            )
            Log.info("CyberIO updated successfully.")
            onSuccess()
        } else {
            Log.err("CyberIO's jar file can't be found.")
            onFailed("CyberIO's jar file can't be found.")
        }
    }

    val requireUpdate: Boolean
        get() = latestVersion > Meta.DetailedVersion
}
