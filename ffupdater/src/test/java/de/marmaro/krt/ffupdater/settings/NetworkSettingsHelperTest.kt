package de.marmaro.krt.ffupdater.settings

import android.content.SharedPreferences
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class NetworkSettingsHelperTest {

    private lateinit var networkSettingsHelper: NetworkSettingsHelper
    private lateinit var sharedPreferences: SharedPreferences

    @BeforeEach
    fun setUp() {
        sharedPreferences = SPMockBuilder().createSharedPreferences()
        networkSettingsHelper = NetworkSettingsHelper(sharedPreferences)
    }

    @Test
    fun areUserCAsTrusted_withDefault_returnFalse() {
        assertFalse(networkSettingsHelper.areUserCAsTrusted)
    }

    @Test
    fun areUserCAsTrusted_withTrue_returnTrue() {
        sharedPreferences.edit().putBoolean("network__trust_user_cas", true).apply()
        assertTrue(networkSettingsHelper.areUserCAsTrusted)
    }

    @Test
    fun areUserCAsTrusted_withFalse_returnFalse() {
        sharedPreferences.edit().putBoolean("network__trust_user_cas", false).apply()
        assertFalse(networkSettingsHelper.areUserCAsTrusted)
    }

    @Test
    fun areUserCAsTrusted_withChangingValue_returnCorrectValue() {
        sharedPreferences.edit().putBoolean("network__trust_user_cas", true).apply()
        assertTrue(networkSettingsHelper.areUserCAsTrusted)

        sharedPreferences.edit().putBoolean("network__trust_user_cas", false).apply()
        assertFalse(networkSettingsHelper.areUserCAsTrusted)

        sharedPreferences.edit().putBoolean("network__trust_user_cas", true).apply()
        assertTrue(networkSettingsHelper.areUserCAsTrusted)

        sharedPreferences.edit().putBoolean("network__trust_user_cas", false).apply()
        assertFalse(networkSettingsHelper.areUserCAsTrusted)
    }
}