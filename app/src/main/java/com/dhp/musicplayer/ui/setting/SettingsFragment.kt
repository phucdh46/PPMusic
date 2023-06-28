package com.dhp.musicplayer.ui.setting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentSettingsBinding
import com.dhp.musicplayer.player.UIControlInterface

class SettingsFragment: BaseFragment<FragmentSettingsBinding>() {
    private lateinit var mUIControlInterface: UIControlInterface
    private var mPreferencesFragment: PreferencesFragment? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentSettingsBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUIControlInterface = activity as UIControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPreferencesFragment = PreferencesFragment.newInstance()
        mPreferencesFragment?.let { fm ->
            childFragmentManager.commit {
                replace(R.id.fragment_layout, fm)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}