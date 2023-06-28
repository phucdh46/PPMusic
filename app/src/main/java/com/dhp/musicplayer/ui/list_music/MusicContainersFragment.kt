package com.dhp.musicplayer.ui.list_music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentMusicContainersBinding

class MusicContainersFragment: BaseFragment<FragmentMusicContainersBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMusicContainersBinding.inflate(layoutInflater, container, false)

    companion object {

        private const val TAG_LAUNCHED_BY = "SELECTED_FRAGMENT"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MusicContainersListFragment.
         */
        @JvmStatic
        fun newInstance(launchedBy: String) = MusicContainersFragment().apply {
            arguments = bundleOf(TAG_LAUNCHED_BY to launchedBy)
        }
    }
}