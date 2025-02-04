package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.preference.PreferenceManager
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceAbiExtractor
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.mozillaci.MozillaCiLogConsumer
import de.marmaro.krt.ffupdater.settings.DeviceSettingsHelper
import de.marmaro.krt.ffupdater.settings.NetworkSettingsHelper

/**
 * https://firefox-ci-tc.services.mozilla.com/tasks/index/mobile.v2.fenix.release.latest
 * https://www.apkmirror.com/apk/mozilla/firefox/
 */
class FirefoxRelease(
    private val consumer: MozillaCiLogConsumer = MozillaCiLogConsumer.INSTANCE,
    private val deviceAbiExtractor: DeviceAbiExtractor = DeviceAbiExtractor.INSTANCE,
) : AppBase() {
    override val app = App.FIREFOX_RELEASE
    override val codeName = "FirefoxRelease"
    override val packageName = "org.mozilla.firefox"
    override val title = R.string.firefox_release__title
    override val description = R.string.firefox_release__description
    override val installationWarning = R.string.firefox_release__warning
    override val downloadSource = "Mozilla CI"
    override val icon = R.drawable.ic_logo_firefox_release
    override val minApiLevel = Build.VERSION_CODES.LOLLIPOP

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04"
    override val supportedAbis = ARM32_ARM64_X86_X64
    override val projectPage =
        "https://firefox-ci-tc.services.mozilla.com/tasks/index/mobile.v2.fenix.release.latest"
    override val displayCategory = DisplayCategory.FROM_MOZILLA

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun findLatestUpdate(context: Context): LatestUpdate {
        Log.d(LOG_TAG, "check for latest version")
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val networkSettings = NetworkSettingsHelper(preferences)
        val deviceSettings = DeviceSettingsHelper(preferences)

        val abiString = when (deviceAbiExtractor.findBestAbi(supportedAbis, deviceSettings.prefer32BitApks)) {
            ABI.ARMEABI_V7A -> "armeabi-v7a"
            ABI.ARM64_V8A -> "arm64-v8a"
            ABI.X86 -> "x86"
            ABI.X86_64 -> "x86_64"
            else -> throw IllegalArgumentException("ABI is not supported")
        }
        val result = consumer.updateCheck(
            task = "mobile.v2.fenix.release.latest.$abiString",
            apkArtifact = "public/build/$abiString/target.apk",
            settings = networkSettings
        )
        val version = result.version
        Log.i(LOG_TAG, "found latest version $version")
        return LatestUpdate(
            downloadUrl = result.url,
            version = version,
            publishDate = result.releaseDate,
            exactFileSizeBytesOfDownload = null,
            fileHash = null,
        )
    }

    companion object {
        private const val LOG_TAG = "FirefoxRelease"
    }
}