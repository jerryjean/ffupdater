package de.marmaro.krt.ffupdater.installer.impl

import android.content.Context
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.R.string.download_activity__downloaded_application_is_not_verified
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.installer.AppInstaller
import de.marmaro.krt.ffupdater.installer.entity.InstallResult
import de.marmaro.krt.ffupdater.installer.exceptions.InstallationFailedException
import de.marmaro.krt.ffupdater.security.FingerprintValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class AbstractAppInstaller(
    protected val app: App,
) : AppInstaller {

    override suspend fun startInstallation(context: Context, file: File): InstallResult {
        return withContext(Dispatchers.IO) {
            install2Internal(context, file)
        }
    }

    @Throws(InstallationFailedException::class)
    private suspend fun install2Internal(context: Context, file: File): InstallResult {
        val validator = FingerprintValidator(context.packageManager)
        val fileResult = try {
            validator.checkApkFile(file, app.impl)
        } catch (e: Exception) {
            val em = "Failed to validate the signature of the download."
            throw InstallationFailedException("Can't validate the signature of the APK file.", e, -103, em)
        }
        val fileCertHash = fileResult.hexString
        if (!fileResult.isValid) {
            val message = "Downloaded application is NOT verified. Expected ${app.impl.signatureHash} but " +
                    "was $fileCertHash."
            val errorMessage = context.getString(download_activity__downloaded_application_is_not_verified)
            throw InstallationFailedException(message, -100, errorMessage)
        }

        // in failure, this will throw InstallationFailedException
        executeInstallerSpecificLogic(context, file)

        val appResult = try {
            validator.checkInstalledApp(app.impl)
        } catch (e: Exception) {
            val errorMessage = "Failed to check installed app."
            throw InstallationFailedException("Failed to check installed app.", e, -102, errorMessage)
        }
        if (!appResult.isValid || fileCertHash != appResult.hexString) {
            val errorMessage = context.getString(R.string.installed_app_is_not_verified)
            throw InstallationFailedException("Installed app is NOT verified", -101, errorMessage)
        }
        return InstallResult(fileCertHash)
    }

    protected abstract suspend fun executeInstallerSpecificLogic(context: Context, file: File)
}