package com.dhp.musicplayer.ui.all_music

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.dhp.musicplayer.MusicViewModel
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentAllMusicBinding
import com.dhp.musicplayer.extensions.setTitleColor
import com.dhp.musicplayer.model.Music
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.ui.all_music.adapter.AllMusicAdapter
import com.dhp.musicplayer.utils.Lists
import com.dhp.musicplayer.utils.Theming

class AllMusicFragment: BaseFragment<FragmentAllMusicBinding>()  {

    private lateinit var mMusicViewModel: MusicViewModel
    private var mAllMusic: List<Music>? = null
    private lateinit var mMediaControlInterface: MediaControlInterface
    private lateinit var mUIControlInterface: UIControlInterface

    // sorting
    private lateinit var mSortMenuItem: MenuItem
    private var mSorting = Preferences.getPrefsInstance().allMusicSorting

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

//                            Lists.getSortedMusicListForAllMusic(
//                            mSorting,
//                            mMusicViewModel.deviceMusicFiltered
//                        )
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
                stb.inflateMenu(R.menu.menu_music_search)
                stb.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort)
                stb.setNavigationOnClickListener {
                    mUIControlInterface.onCloseActivity()
                }
                with(stb.menu) {

                    mSortMenuItem = Lists.getSelectedSortingForMusic(mSorting, this)

                    with (findItem(R.id.action_search).actionView as SearchView) {
                        //setOnQueryTextListener(this@AllMusicFragment)
                        setOnQueryTextFocusChangeListener { _, hasFocus ->
                            stb.menu.setGroupVisible(R.id.sorting, !hasFocus)
                            stb.menu.findItem(R.id.sleeptimer).isVisible = !hasFocus
                        }
                    }

                    setMenuOnItemClickListener(stb.menu)
                }
            }
        }
    }

    private fun setMenuOnItemClickListener(menu: Menu) {

        binding.searchToolbar.setOnMenuItemClickListener {

            if (it.itemId == R.id.default_sorting || it.itemId == R.id.ascending_sorting
                || it.itemId == R.id.descending_sorting || it.itemId == R.id.date_added_sorting
                || it.itemId == R.id.date_added_sorting_inv || it.itemId == R.id.artist_sorting
                || it.itemId == R.id.artist_sorting_inv || it.itemId == R.id.album_sorting
                || it.itemId == R.id.album_sorting_inv) {

                mSorting = it.order
                mAllMusic = Lists.getSortedMusicListForAllMusic(mSorting, mAllMusic)

                setMusicDataSource(mAllMusic)

                mSortMenuItem.setTitleColor(
                    Theming.resolveColorAttr(requireContext(), android.R.attr.textColorPrimary)
                )

                mSortMenuItem = Lists.getSelectedSortingForMusic(mSorting, menu).apply {
                    setTitleColor(Theming.resolveThemeColor(resources))
                }

                Preferences.getPrefsInstance().allMusicSorting = mSorting

            } else if (it.itemId != R.id.action_search) {
                mUIControlInterface.onOpenSleepTimerDialog()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun setMusicDataSource(musicList: List<Music>?) {
        musicList?.run {
            mAllMusic = this
            binding.allMusicRv.adapter?.notifyDataSetChanged()
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