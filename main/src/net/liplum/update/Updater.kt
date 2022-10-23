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
import net.liplum.common.replaceBy
import plumy.core.ClientOnly
import plumy.core.HeadlessOnly
import net.liplum.utils.ZipUtil
import java.io.ByteArrayOutputStream
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
     * @param onFailed when it can't fetch the latest version, this will be called.
     */
    fun fetchLatestVersion(
        onFailed: (Throwable) -> Unit = {
            CLog.err("Can't fetch the latest version because $it.")
        },
    ) {
        val url = Meta.UpdateInfoURL
        CLog.info("Update checking...")
        accessJob = launch(
            CoroutineExceptionHandler { _, e ->
                onFailed(e)
            }
        ) {
            val info = URL(url).readText()
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

    fun updateFailed(error: Throwable) {
        Vars.ui.loadfrag.hide()
        if (error is NoSuchMethodError ||
            Strings.getCauses(error).toList().any { t ->
                val msg = t.message
                msg != null && ("trust anchor" in msg || "SSL" in msg || "protocol" in msg)
            }
        ) {
            Vars.ui.showErrorMessage("@feature.unsupported")
        } else if (error is Http.HttpStatusException) {
            Vars.ui.showErrorMessage(
                Core.bundle.format("connectfail", Strings.capitalize("${error.status}".lowercase()))
            )
        } else {
            Vars.ui.showException(error)
        }
    }

    object Preview {
        val previewUrl get() = "https://nightly.link/liplum/CyberIO/workflows/Push/${Meta.GitHubBranch}/CyberIO-Unzip-This.zip"
        fun update() {
            modImportProgress = 0f
            Vars.ui.loadfrag.show("@downloading")
            Vars.ui.loadfrag.setProgress { modImportProgress }
            Http.get(previewUrl, { res ->
                val zipFile = Vars.tmpDirectory.child("CyberIO-Preview-packed.zip")
                val len = res.contentLength
                val cons = if (len <= 0) Floatc { modImportProgress = 0.5f }
                else Floatc { modImportProgress = it }
                Streams.copyProgress(res.resultAsStream, zipFile.write(false), len, Streams.defaultBufferSize, cons)
                modImportProgress = 1f
                val unpackedDir = Vars.tmpDirectory.child("CyberIO-Preview-unpacked")
                unpackedDir.deleteDirectory()
                ZipUtil.unzip(zipFile.file(), unpackedDir.path())
                val jar = unpackedDir.list().first() ?: throw ArcRuntimeException("There is no jar in this preview.")
                if (CioMod.jarFile != null) {
                    CioMod.jarFile.replaceBy(jar.file())
                } else {
                    val mod = Vars.mods.importMod(jar)
                    mod.repo = Meta.Repo
                }
                Time.run(10f) {
                    Vars.ui.showInfoOnHidden("@mods.reloadexit", Core.app::exit)
                }
            }, ::updateFailed)
        }
    }

    fun releaseUrl(version: String) = "${Meta.GitHubUrl}/${Meta.Repo}/releases/download/v$version/CyberIO-$version.jar"

    object Release {
        fun update() {
            modImportProgress = 0f
            Vars.ui.loadfrag.show("@downloading")
            Vars.ui.loadfrag.setProgress { modImportProgress }
            Http.get(releaseUrl("$latestVersion"), { download ->
                val new = Vars.tmpDirectory.child("${Meta.RepoFileSystem}.zip")
                val len = download.contentLength
                val cons = if (len <= 0) Floatc { modImportProgress = 0.5f }
                else Floatc { modImportProgress = it }
                Streams.copyProgress(download.resultAsStream, new.write(false), len, 4096, cons)
                modImportProgress = 1f
                if (CioMod.jarFile != null) {
                    CioMod.jarFile.replaceBy(new.file())
                } else {
                    val mod = Vars.mods.importMod(new)
                    mod.repo = Meta.Repo
                }
                Time.run(10f) {
                    Vars.ui.showInfoOnHidden("@mods.reloadexit", Core.app::exit)
                }
            }, ::updateFailed)
        }
    }

    object Headless {
        @HeadlessOnly
        fun tryUpdateHeadless(shouldUpdate: Boolean = Config.AutoUpdate) {
            launch {
                accessJob?.join()
                if (!requireUpdate) {
                    CLog.info("[Auto-Update] The current version ${Meta.DetailedVersion} is the latest.")
                    return@launch
                }
                if (shouldUpdate) {
                    CLog.info("[Auto-Update ON] Now updating...")
                    Http.get(releaseUrl("$latestVersion"), { download ->
                        CLog.info("v$latestVersion is downloading.")
                        val bos = ByteArrayOutputStream()
                        Streams.copy(download.resultAsStream, bos, Streams.defaultBufferSize)
                        CLog.info("v$latestVersion downloaded successfully, replacing file.")
                        if (CioMod.jarFile == null) {
                            CLog.err("CyberIO mod doesn't exist.")
                            return@get
                        }
                        CioMod.jarFile.replaceBy(bos.toByteArray())
                        CLog.info("Cyber IO was replaced with v$latestVersion successfully, now restart to reload mod.")
                        Core.app.exit()
                    }) {
                        CLog.err(it)
                    }
                } else {
                    CLog.info("[Auto-Update OFF] Current version is ${Meta.DetailedVersion} and need to be updated to $latestVersion manually.")
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
}