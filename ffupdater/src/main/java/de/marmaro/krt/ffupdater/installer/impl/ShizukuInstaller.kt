package de.marmaro.krt.ffupdater.installer.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import de.marmaro.krt.ffupdater.installer.entity.Installer
import de.marmaro.krt.ffupdater.installer.exceptions.InstallationFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

/**
 * https://github.com/Iamlooker/Droid-ify/blob/59e4675f220520c9416b55697987ce6f374bd179/installer/src/main/java/com/looker/installer/installer/ShizukuInstaller.kt
 *
 * For further improvements: https://www.xda-developers.com/implementing-shizuku/
 */
class ShizukuInstaller(
    app: App,
    private val deviceSdkTester: DeviceSdkTester = DeviceSdkTester.INSTANCE,
) : AbstractAppInstaller(app) {
    override val type = Installer.SHIZUKU_INSTALLER

    init {
        if (!deviceSdkTester.supportsAndroidMarshmallow()) {
            throw RuntimeException("Shizuku is not supported on this device")
        }
    }

    override suspend fun executeInstallerSpecificLogic(context: Context, file: File) {
        val downloadFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        require(file.parentFile == downloadFolder)

        val appName = app.impl.packageName
            .replace('.', '_')
            .replace("""\W""", "_")
        require(file.name.startsWith(appName))
        require(file.name.endsWith(".apk"))
        require(!file.name.removeSuffix(".apk").contains(Regex("""\W""")))

        failIfShizukuPermissionIsMissing()
        val size = file.length().toInt()
        val sessionId = createInstallationSession(size)
        installApp(sessionId, size, file)
    }

    private fun failIfShizukuPermissionIsMissing() {
        val permission = try {
            Shizuku.checkSelfPermission()
        } catch (e: IllegalStateException) {
            throw InstallationFailedException(
                "Shizuku is not running. Please start the Shizuku service.",
                -432
            )
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(42)
            throw InstallationFailedException("Missing Shizuku permission. Retry again.", -431)
        }
    }

    private suspend fun createInstallationSession(size: Int): Int {
        val result = if (deviceSdkTester.supportsAndroidNougat()) {
            execute("pm install-create --user current -i ${app.impl.packageName} -S $size")
        } else {
            execute("pm install-create -i ${app.impl.packageName} -S $size")
        }

        val sessionIdMatch = Regex("""\d+""").find(result)
        checkNotNull(sessionIdMatch) { "Can't find session id with regex pattern. Output: $result" }

        val sessionId = sessionIdMatch.groups[0]
        checkNotNull(sessionId) { "Can't find match group containing the session id. Output: $result" }

        return sessionId.value.toInt()
    }

    private suspend fun installApp(sessionId: Int, size: Int, file: File) {
        execute("""cat "${file.absolutePath}" | pm install-write -S $size $sessionId "${file.name}"""")
        execute("""pm install-commit $sessionId""")
    }

    private suspend fun execute(command: String): String {
        return withContext(Dispatchers.IO) {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val resultCode = process.waitFor()
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            if (resultCode != 0) {
                throw InstallationFailedException(
                    "Shizuku command '$command' failed. Result code is: '$resultCode', " +
                            "stdout: '$stdout', stderr: '$stderr'",
                    -403
                )
            }
            stdout
        }
    }
}
