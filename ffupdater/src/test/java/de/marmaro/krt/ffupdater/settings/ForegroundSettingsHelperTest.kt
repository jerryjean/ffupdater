package de.marmaro.krt.ffupdater.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class ForegroundSettingsHelperTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var deviceSdkTester: DeviceSdkTester

    @BeforeEach
    fun setUp() {
        sharedPreferences = SPMockBuilder().createSharedPreferences()
        deviceSdkTester = mockk()
    }

    companion object {
        @JvmStatic
        fun testDataForBooleanSettings(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "isUpdateCheckOnMeteredAllowed",
                "foreground__update_check__metered",
                true,
                { helper: ForegroundSettingsHelper -> helper.isUpdateCheckOnMeteredAllowed }),

            Arguments.of(
                "isDownloadOnMeteredAllowed",
                "foreground__download__metered",
                true,
                { helper: ForegroundSettingsHelper -> helper.isDownloadOnMeteredAllowed }),

            Arguments.of(
                "isDeleteUpdateIfInstallSuccessful",
                "foreground__delete_cache_if_install_successful",
                true,
                { helper: ForegroundSettingsHelper -> helper.isDeleteUpdateIfInstallSuccessful }),

            Arguments.of(
                "isDeleteUpdateIfInstallFailed",
                "foreground__delete_cache_if_install_failed",
                true,
                { helper: ForegroundSettingsHelper -> helper.isDeleteUpdateIfInstallFailed }),

            Arguments.of(
                "isHideWarningButtonForInstalledApps",
                "foreground__hide_warning_button_for_installed_apps",
                false,
                { helper: ForegroundSettingsHelper -> helper.isHideWarningButtonForInstalledApps }),
        )
    }

    @ParameterizedTest(name = "has \"{0}\" the correct default value \"{2}\"")
    @MethodSource("testDataForBooleanSettings")
    fun `has boolean settings the correct default value`(
        @Suppress("unused") name: String,
        @Suppress("unused") preferenceKey: String,
        defaultValue: Boolean,
        getValue: (ForegroundSettingsHelper) -> Boolean,
    ) {
        val sut = ForegroundSettingsHelper(sharedPreferences, deviceSdkTester)
        val actual = getValue(sut)
        Assertions.assertEquals(defaultValue, actual)
    }

    @ParameterizedTest(name = "has \"{0}\" the correct value when changed to true")
    @MethodSource("testDataForBooleanSettings")
    fun `has boolean settings the correct value when changed to true`(
        @Suppress("unused") name: String,
        preferenceKey: String,
        @Suppress("unused") defaultValue: Boolean,
        getValue: (ForegroundSettingsHelper) -> Boolean,
    ) {
        sharedPreferences.edit().putBoolean(preferenceKey, true).commit()
        val sut = ForegroundSettingsHelper(sharedPreferences, deviceSdkTester)
        val actual = getValue(sut)
        Assertions.assertTrue(actual)
    }

    @ParameterizedTest(name = "has \"{0}\" the correct value when changed to false")
    @MethodSource("testDataForBooleanSettings")
    fun `has boolean settings the correct value when changed to false`(
        @Suppress("unused") name: String,
        preferenceKey: String,
        @Suppress("unused") defaultValue: Boolean,
        getValue: (ForegroundSettingsHelper) -> Boolean,
    ) {
        sharedPreferences.edit().putBoolean(preferenceKey, false).commit()
        val sut = ForegroundSettingsHelper(sharedPreferences, deviceSdkTester)
        val actual = getValue(sut)
        Assertions.assertFalse(actual)
    }

    @ParameterizedTest(name = "has \"{0}\" the correct value when changing values")
    @MethodSource("testDataForBooleanSettings")
    fun `has boolean settings the correct value when changing values`(
        @Suppress("unused") name: String,
        preferenceKey: String,
        @Suppress("unused") defaultValue: Boolean,
        getValue: (ForegroundSettingsHelper) -> Boolean,
    ) {
        val sut = ForegroundSettingsHelper(sharedPreferences, deviceSdkTester)
        sharedPreferences.edit().putBoolean(preferenceKey, false).commit()
        Assertions.assertFalse(getValue(sut))
        sharedPreferences.edit().putBoolean(preferenceKey, true).commit()
        Assertions.assertTrue(getValue(sut))
    }

    @Test
    fun getThemePreference_userHasNotChangedSetting_AndroidPAndBelow_returnDefaultValue() {
        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_userHasNotChangedSetting_AndroidQAndHigher_returnDefaultValue() {
        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withInvalidValue_null_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", null).commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withInvalidValue_emptyString_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withInvalidValue_text_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "lorem ipsum").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withInvalidValue_nonExistingNumber_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "6").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_FOLLOW_SYSTEM_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "-1").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_NO_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "1").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_NO,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_NO,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_YES_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "2").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_YES,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_YES,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_AUTO_BATTERY_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "3").commit()

        every { deviceSdkTester.supportsAndroid10() } returns false
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )

        every { deviceSdkTester.supportsAndroid10() } returns true
        Assertions.assertEquals(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            ForegroundSettingsHelper(sharedPreferences, deviceSdkTester).themePreference
        )
    }
}