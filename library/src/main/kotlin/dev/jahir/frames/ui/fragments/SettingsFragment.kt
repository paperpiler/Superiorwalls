package dev.jahir.frames.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.BuildConfig
import dev.jahir.frames.R
import dev.jahir.frames.extensions.*
import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.frames.ui.fragments.base.BasePreferenceFragment
import dev.jahir.frames.utils.Prefs

open class SettingsFragment : BasePreferenceFragment() {

    private var currentThemeKey: Int = -1

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val interfacePreferences = findPreference<PreferenceCategory?>("interface_prefs")

        currentThemeKey = prefs.currentTheme.value
        val themePreference = findPreference<Preference?>("app_theme")
        themePreference?.setSummary(Prefs.ThemeKey.fromValue(currentThemeKey).stringResId)
        themePreference?.setOnClickListener {
            showDialog {
                title(R.string.app_theme)
                singleChoiceItems(R.array.app_themes, currentThemeKey) { _, which ->
                    currentThemeKey = which
                }
                positiveButton(android.R.string.ok) {
                    prefs.currentTheme = Prefs.ThemeKey.fromValue(currentThemeKey)
                    it.dismiss()
                }
            }
        }

        val amoledPreference = findPreference<SwitchPreference?>("use_amoled")
        amoledPreference?.isChecked = prefs.usesAmoledTheme
        amoledPreference?.setOnCheckedChangeListener { prefs.usesAmoledTheme = it }

        val coloredNavbarPref = findPreference<SwitchPreference?>("colored_navigation_bar")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            interfacePreferences?.removePreference(coloredNavbarPref)
        else {
            coloredNavbarPref?.isChecked = prefs.shouldColorNavbar
            coloredNavbarPref?.setOnCheckedChangeListener { prefs.shouldColorNavbar = it }
        }

        val animationsPref = findPreference<SwitchPreference?>("interface_animations")
        animationsPref?.isChecked = prefs.animationsEnabled
        animationsPref?.setOnCheckedChangeListener { prefs.animationsEnabled = it }

        val fullResPicturesPref = findPreference<SwitchPreference?>("full_res_previews")
        fullResPicturesPref?.isChecked = prefs.shouldLoadFullResPictures
        fullResPicturesPref?.setOnCheckedChangeListener {
            prefs.shouldLoadFullResPictures = it
        }

        val cropPicturesPrefs = findPreference<SwitchPreference?>("crop_pictures")
        cropPicturesPrefs?.isChecked = prefs.shouldCropWallpaperBeforeApply
        cropPicturesPrefs?.setOnCheckedChangeListener {
            prefs.shouldCropWallpaperBeforeApply = it
        }

        val downloadLocationPref = findPreference<Preference?>("download_location")
        downloadLocationPref?.summary = ""

        val clearCachePref = findPreference<Preference?>("clear_data_cache")
        clearCachePref?.summary =
            string(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        clearCachePref?.setOnClickListener {
            context?.clearDataAndCache()
            clearCachePref.summary =
                string(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        }

        val notificationsPrefs = findPreference<SwitchPreference?>("notifications")
        notificationsPrefs?.isChecked = prefs.notificationsEnabled
        notificationsPrefs?.setOnCheckedChangeListener {
            prefs.notificationsEnabled = it
        }

        if (context?.boolean(R.bool.show_versions_in_settings, true) == true) {
            val appVersionPrefs = findPreference<Preference?>("app_version")
            appVersionPrefs?.title = context?.getAppName("App") ?: "App"
            appVersionPrefs?.summary =
                "${context?.currentVersionName} (${context?.currentVersionCode})"

            val dashboardVersionPrefs = findPreference<Preference?>("dashboard_version")
            dashboardVersionPrefs?.title = BuildConfig.DASHBOARD_NAME
            dashboardVersionPrefs?.summary = BuildConfig.DASHBOARD_VERSION
        } else {
            preferenceScreen?.removePreference(findPreference("versions"))
        }

        setupLegalLinks()
    }

    private fun setupLegalLinks() {
        val privacyLink = string(R.string.privacy_policy_link)
        val termsLink = string(R.string.terms_conditions_link)

        val prefsScreen = findPreference<PreferenceScreen?>("prefs")
        val legalCategory = findPreference<PreferenceCategory?>("legal")

        if (privacyLink.hasContent() || termsLink.hasContent()) {
            val privacyPref = findPreference<Preference?>("privacy")
            if (privacyLink.hasContent()) {
                privacyPref?.setOnClickListener {
                    try {
                        context?.openLink(privacyLink)
                    } catch (e: Exception) {
                    }
                }
            } else {
                legalCategory?.removePreference(privacyPref)
            }

            val termsPref = findPreference<Preference?>("terms")
            if (termsLink.hasContent()) {
                termsPref?.setOnClickListener {
                    try {
                        context?.openLink(termsLink)
                    } catch (e: Exception) {
                    }
                }
            } else {
                legalCategory?.removePreference(termsPref)
            }
        } else {
            prefsScreen?.removePreference(legalCategory)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showDialog(options: MaterialAlertDialogBuilder.() -> MaterialAlertDialogBuilder): Boolean {
        showDialog(requireContext().mdDialog(options))
        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showDialog(dialog: AlertDialog?): Boolean {
        (activity as? SettingsActivity)?.showDialog(dialog)
        return true
    }

    companion object {
        internal const val TAG = "settings_fragment"
    }
}
