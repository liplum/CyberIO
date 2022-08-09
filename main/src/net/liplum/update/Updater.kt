package net.liplum.update

import arc.Core
import arc.func.Floatc
import arc.util.ArcRuntimeException
import arc.util.Http
import arc.util.Strings
import arc.util.Time
import arc.util.io.Streams
import arc.util.serialization.Json
import arc.util.serialization.Jval
import arc.util.serialization.Jval.Jformat
import kotlinx.coroutines.*
import mindustry.Vars
import net.liplum.CLog
import net.liplum.CioMod
import net.liplum.ConfigEntry.Companion.Config
import net.liplum.Meta
import net.liplum.common.replaceByteBy
import net.liplum.common.util.useFakeHeader
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.HeadlessOnly
import net.liplum.util.ZipUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import kotlin.coroutines.CoroutineContext

object Updater : CoroutineScope {
    var latestVersion: Version2 = Meta.DetailedVersion
    var updateInfo = UpdateInfo.X
    var accessJob: Job? = null
    var json = Json()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    var modImportProgress = 0f
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

    private fun updateFailed(error: Throwable) {
        Vars.ui.loadfrag.hide()
        if (error is NoSuchMethodError ||
            Strings.getCauses(error).toList().any { t ->
                val msg = t.message
                msg != null && ("trust anchor" in msg || "SSL" in msg || "protocol" in msg)
            }
        ) {
            Vars.ui.showErrorMessage("@feature.unsupported");
        } else if (error is Http.HttpStatusException) {
            Vars.ui.showErrorMessage(
                Core.bundle.format("connectfail", Strings.capitalize("${error.status}".lowercase()))
            )
        } else {
            Vars.ui.showException(error);
        }
    }

    object Preview {
        val url get() = "https://nightly.link/liplum/CyberIO/workflows/Push/${Meta.GitHubBranch}/CyberIO-Unzip-This.zip"
        @JvmStatic
        fun updateDebugPreview() {
            modImportProgress = 0f
            Vars.ui.loadfrag.show("@downloading")
            Vars.ui.loadfrag.setProgress { modImportProgress }
            Http.get(url, { res ->
                val zipFile = Vars.tmpDirectory.child("CyberIO-Preview-packed.zip")
                val len = res.contentLength
                val cons = if (len <= 0) Floatc { modImportProgress = 0.5f }
                else Floatc { modImportProgress = it }
                Streams.copyProgress(res.resultAsStream, zipFile.write(false), len, Streams.defaultBufferSize, cons)
                modImportProgress = 1f
                val unpackedDir = Vars.tmpDirectory.child("CyberIO-Preview-unpacked")
                ZipUtil.unzip(zipFile.file(), unpackedDir.path())
                val jar = unpackedDir.list().first() ?: throw ArcRuntimeException("There is no jar in this preview.")
                try {
                    if (CioMod.jarFile != null) {
                        CioMod.jarFile.replaceByteBy(jar.file())
                    } else {
                        val mod = Vars.mods.importMod(jar)
                        mod.repo = Meta.Repo
                    }
                } catch (e: Exception) {
                    throw e
                } finally {
                    zipFile.delete()
                    unpackedDir.deleteDirectory()
                }
                Time.run(10f) {
                    Vars.ui.showInfoOnHidden("@mods.reloadexit", Core.app::exit)
                }
            }, ::updateFailed)
        }
    }

    object Android {
        @JvmStatic
        fun updateSelfByBuiltIn() {
            val version = latestVersion.toString()
            updateModBuiltin("CyberIO-$version.jar")
        }

        private fun updateModBuiltin(fileName: String? = null) {
            modImportProgress = 0f
            Vars.ui.loadfrag.show("@downloading")
            Vars.ui.loadfrag.setProgress { modImportProgress }
            //grab latest release
            Http.get(Meta.LatestRelease, { res: Http.HttpResponse ->
                val json = Jval.read(res.resultAsString)
                val assets = json["assets"].asArray().toList()
                val asset = if (fileName != null)
                    assets.find { it.getString("name") == fileName }
                        ?: assets.find { it.getString("name").endsWith(".jar") }
                else
                    assets.find { it.getString("name").endsWith(".jar") }
                if (asset != null) {
                    //grab actual file
                    val url = asset.getString("browser_download_url")
                    Http.get(url, ::handleNewVersion, ::updateFailed)
                } else {
                    throw ArcRuntimeException("No JAR file found in releases. Make sure you have a valid jar file in the mod's latest Github Release.")
                }
            }, ::updateFailed)
        }

        private fun handleNewVersion(result: Http.HttpResponse) {
            try {
                val file = Vars.tmpDirectory.child("${Meta.RepoFileSystem}.zip")
                val len = result.contentLength
                val cons = if (len <= 0) Floatc { }
                else Floatc { modImportProgress = it }
                Streams.copyProgress(result.resultAsStream, file.write(false), len, 4096, cons)
                val mod = Vars.mods.importMod(file)
                mod.repo = Meta.Repo
                file.delete()
                Time.run(10f) {
                    Vars.ui.showInfoOnHidden("@mods.reloadexit", Core.app::exit)
                }
            } catch (e: Throwable) {
                updateFailed(e)
            }
        }
    }

    object Desktop {
        val curDownloadURL: String
            get() {
                val version = latestVersion.toString()
                return "${Meta.GitHubUrl}/${Meta.Repo}/releases/download/v$version/CyberIO-$version.jar"
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
        private fun downloadAndReplaceLatest(
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
    }

    object Headless {
        @HeadlessOnly
        @JvmOverloads
        fun tryUpdateHeadless(shouldUpdateOverride: Boolean = false) {
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
        @JvmStatic
        private fun updateSelfByReplaceFinding(
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
                    Desktop.updateSelfByReplace(url, onProgress, onSuccess, onFailed)
                } else {
                    onFailed("Jar file wasn't found at this release.")
                }
            }) {
                onFailed("Can't acquire update info $it")
            }
        }
    }
}

class UpdateInfo {
    var Latest = ""
    var ClientLatest = ""
    var ServerLatest = ""
    var Description = ""
    var MinGameVersion = 137
    var BreakUpdateList = emptyArray<String>()

    companion object {
        internal val X = UpdateInfo()
        fun UpdateInfo.isDefault() =
            this == X
    }
}