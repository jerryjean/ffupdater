package de.marmaro.krt.ffupdater.network.fdroid

import androidx.annotation.MainThread
import com.google.gson.annotations.SerializedName
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.network.ApiConsumer
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.security.Sha256Hash
import de.marmaro.krt.ffupdater.settings.NetworkSettingsHelper
import java.time.Instant

class CustomRepositoryConsumer(
    private val apiConsumer: ApiConsumer,
) {

    @MainThread
    suspend fun getLatestUpdate(
        settings: NetworkSettingsHelper,
        repoUrl: String,
        packageName: String,
        abi: ABI
    ): LatestUpdate {
        val mainObject = try {
            apiConsumer.consume("$repoUrl/index-v1.json", settings, MainObject::class)
        } catch (e: NetworkException) {
            throw NetworkException("Fail to find the latest version from index-v1.json.", e)
        }

        val packageObject = checkNotNull(mainObject.packages[packageName])
        val apk = packageObject
            // always accept APKs without ABI requirements
            .filter { apkObject -> apkObject.abis!!.contains(abi.codeName) }
            .maxBy { apkObject -> apkObject.versionCode }

        val version = apk.versionName
        return LatestUpdate(
            downloadUrl = "$repoUrl/${apk.apkName}",
            version = version,
            publishDate = Instant.ofEpochMilli(apk.added).toString(),
            exactFileSizeBytesOfDownload = apk.size,
            fileHash = Sha256Hash(apk.hash),
        )
    }

    data class MainObject(
        @SerializedName("packages")
        val packages: Map<String, List<ApkObject>>,
    )

    data class ApkObject(
        @SerializedName("added")
        val added: Long,
        @SerializedName("apkName")
        val apkName: String,
        @SerializedName("nativecode")
        val abis: List<String>?,
        @SerializedName("hash")
        val hash: String,
        @SerializedName("size")
        val size: Long,
        @SerializedName("versionCode")
        val versionCode: Long,
        @SerializedName("versionName")
        val versionName: String,
    )

    companion object {
        val INSTANCE = CustomRepositoryConsumer(ApiConsumer.INSTANCE)
    }
}