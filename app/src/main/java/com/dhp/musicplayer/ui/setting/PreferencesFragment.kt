package com.dhp.musicplayer.ui.setting

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.dialogs.RecyclerSheet
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.utils.Theming

class PreferencesFragment: PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mUIControlInterface: UIControlInterface
    private lateinit var mMediaControlInterface: MediaControlInterface

    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mGoPreferences get() = Preferences.getPrefsInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUIControlInterface = activity as UIControlInterface
            mMediaControlInterface = activity as MediaControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(getString(R.string.theme_pref))?.icon = ContextCompat.getDrawable(requireContext(), if (Theming.isThemeNight(resources)) {
            R.drawable.ic_night
        } else {
            R.drawable.ic_day
        })

        findPreference<Preference>(getString(R.string.accent_pref))?.run {
            summary = Theming.getAccentName(resources, mGoPreferences.accent)
            onPreferenceClickListener = this@PreferencesFragment
        }

        findPreference<Preference>(getString(R.string.active_tabs_pref))?.run {
            summary = mGoPreferences.activeTabs.size.toString()
            onPreferenceClickListener = this@PreferencesFragment
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.theme_pref) -> mUIControlInterface.onAppearanceChanged(isThemeChanged = true)

        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.accent_pref) -> RecyclerSheet.newInstance(RecyclerSheet.ACCENT_TYPE)
                .show(requireActivity().supportFragmentManager, RecyclerSheet.TAG_MODAL_RV)
            getString(R.string.active_tabs_pref) -> RecyclerSheet.newInstance(RecyclerSheet.TABS_TYPE)
                .show(requireActivity().supportFragmentManager, RecyclerSheet.TAG_MODAL_RV)

        }
        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment PreferencesFragment.
         */
        @JvmStatic
        fun newInstance() = PreferencesFragment()
    }

}