package com.dhp.musicplayer.ui.all_music

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.dhp.musicplayer.MusicViewModel
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentAllMusicBinding
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicAdapter

class AllMusicFragment: BaseFragment<FragmentAllMusicBinding>()  {

    private lateinit var mMusicViewModel: MusicViewModel
    private var mAllMusic: List<Music>? = null
    private lateinit var mMediaControlInterface: MediaControlInterface
    private lateinit var mUIControlInterface: UIControlInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DHP", "AllMusicFragment onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAllMusicBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mMediaControlInterface = activity as MediaControlInterface
            mUIControlInterface = activity as UIControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)
        mMusicViewModel =
            ViewModelProvider(requireActivity())[MusicViewModel::class.java].apply {
                deviceMusic.observe(viewLifecycleOwner) { returnedMusic ->
                    if (!returnedMusic.isNullOrEmpty()) {
                        mAllMusic = returnedMusic
                        Log.d("DHP","all fra: $returnedMusic")
                        finishSetup()
                    }
                }
            }
    }

    private fun finishSetup() {
        binding.run {
            allMusicRv.adapter = AllMusicAdapter(mAllMusic, mMediaControlInterface)
            searchToolbar.let { stb ->
                stb.setNavigationOnClickListener {
                    mUIControlInterface.onCloseActivity()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment AllMusicFragment.
         */
        @JvmStatic
        fun newInstance() = AllMusicFragment()
    }

}