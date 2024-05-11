package com.dhp.musicplayer.ui.setting

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.Constants.Companion.DEFAULT_ACTIVE_FRAGMENTS
import com.dhp.musicplayer.Constants.Companion.DEVELOPER_MODE_ACTIVE_FRAGMENTS
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.utils.Log
import com.dhp.musicplayer.utils.Theming


class PreferencesFragment: PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mUIControlInterface: UIControlInterface
    private lateinit var mMediaControlInterface: MediaControlInterface

    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mPreferences get() = Preferences.getPrefsInstance()

    private var developerModePreference: SwitchPreferenceCompat? = null

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

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        // Find the developer_mode preference
        developerModePreference = findPreference(getString(R.string.developer_mode))
        if (developerModePreference != null) {
            developerModePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val isEnabled = newValue as Boolean
                    Log.d("developerModePreference: $isEnabled")
                    if (isEnabled) {
                        // Show password dialog when developer_mode is enabled
                        showConfirmationTurnOnDeveloperModeDialog()
                    } else {
                        performAction(isEnabled = false)
                    }
                    // Return true to allow the preference to be changed
                    true
                }
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.theme_pref) -> mUIControlInterface.onAppearanceChanged(isThemeChanged = true)
            getString(R.string.developer_mode) -> {
                //showConfirmationTurnOnDeveloperModeDialog()
            }

        }
    }

    private fun showConfirmationTurnOnDeveloperModeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Password")

        // Inflate and set the layout for the dialog

        // Inflate and set the layout for the dialog
        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.dialog_text_input, null)
        builder.setView(dialogView)

        val passwordEditText = dialogView.findViewById<EditText>(R.id.editText)

        // Add action buttons

        // Add action buttons
        builder.setPositiveButton("Confirm") { dialog, which ->
            val password = passwordEditText.text.toString()
            // Check if the password is correct (for demonstration, use a hardcoded password)
            if (password == Constants.PASS) {
                // Password is correct, proceed with the action
                performAction()
            } else {
                developerModePreference?.setChecked(false)
                dialog.dismiss()
                // Password is incorrect, show a message or handle accordingly
                Toast.makeText(requireContext(), "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which ->
            developerModePreference?.setChecked(false)
            dialog.dismiss()
        }

        // Create and show the dialog

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun performAction(isEnabled: Boolean = true) {
        Log.d("performAction: ${mPreferences.developerMode} - ${mPreferences.activeTabs}")
        if (mPreferences.developerMode && isEnabled) {
            mPreferences.activeTabs = DEVELOPER_MODE_ACTIVE_FRAGMENTS
        } else {
            mPreferences.activeTabs = DEFAULT_ACTIVE_FRAGMENTS
        }
        mUIControlInterface.onAppearanceChanged(isThemeChanged = false)
    }


    override fun onPreferenceClick(preference: Preference): Boolean {

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