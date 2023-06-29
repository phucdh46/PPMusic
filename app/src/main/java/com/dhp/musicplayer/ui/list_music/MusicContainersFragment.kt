package com.dhp.musicplayer.ui.list_music

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.MusicViewModel
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseFragment
import com.dhp.musicplayer.databinding.FragmentMusicContainersBinding
import com.dhp.musicplayer.databinding.GenericItemBinding
import com.dhp.musicplayer.extensions.handleViewVisibility
import com.dhp.musicplayer.extensions.loadWithError
import com.dhp.musicplayer.extensions.setTitleColor
import com.dhp.musicplayer.extensions.waitForCover
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.utils.Lists
import com.dhp.musicplayer.utils.Log
import com.dhp.musicplayer.utils.Theming

class MusicContainersFragment: BaseFragment<FragmentMusicContainersBinding>(),
    SearchView.OnQueryTextListener {

    // View model
    private lateinit var mMusicViewModel: MusicViewModel
    private var mLaunchedBy = Constants.ARTIST_VIEW
    private var mList: MutableList<String>? = null
    private lateinit var mListAdapter: MusicContainersAdapter

    private lateinit var mUiControlInterface: UIControlInterface
    private lateinit var mMediaControlInterface: MediaControlInterface
    private var mSorting = Constants.DESCENDING_SORTING
    private lateinit var mSortMenuItem: MenuItem

    private var actionMode: ActionMode? = null
    private val isActionMode get() = actionMode != null

    private val sLaunchedByArtistView get() = mLaunchedBy == Constants.ARTIST_VIEW
    private val sLaunchedByAlbumView get() = mLaunchedBy == Constants.ALBUM_VIEW

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMusicContainersBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getString(TAG_LAUNCHED_BY)?.let { launchedBy ->
            mLaunchedBy = launchedBy
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUiControlInterface = activity as UIControlInterface
            mMediaControlInterface = activity as MediaControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun bindUI(lifecycleOwner: LifecycleOwner) {
        super.bindUI(lifecycleOwner)
        mMusicViewModel =
            ViewModelProvider(requireActivity())[MusicViewModel::class.java].apply {
                deviceMusic.observe(viewLifecycleOwner) { returnedMusic ->
                    Log.d(returnedMusic.toString())
                    if (!returnedMusic.isNullOrEmpty()) {
                        mSorting = getSortingMethodFromPrefs()
                        mList = getSortedList()
                        finishSetup()
                    }
                }
            }
    }

    private fun getSortingMethodFromPrefs(): Int {
        return when (mLaunchedBy) {
            Constants.ARTIST_VIEW ->
                Preferences.getPrefsInstance().artistsSorting
            Constants.FOLDER_VIEW ->
                Preferences.getPrefsInstance().foldersSorting
            else ->
                Preferences.getPrefsInstance().albumsSorting
        }
    }

    private fun getSortedList(): MutableList<String>? {
        return when (mLaunchedBy) {
            Constants.ARTIST_VIEW ->
                Lists.getSortedList(
                    mSorting,
                    mMusicViewModel.deviceAlbumsByArtist?.keys?.toMutableList()
                )
            Constants.FOLDER_VIEW ->
                Lists.getSortedList(
                    mSorting,
                    mMusicViewModel.deviceMusicByFolder?.keys?.toMutableList()
                )
            else ->
                Lists.getSortedListWithNull(
                    mSorting,
                    mMusicViewModel.deviceMusicByAlbum?.keys?.toMutableList()
                )
        }
    }

    private fun getFragmentTitle(): String {
        val stringId = when (mLaunchedBy) {
            Constants.ARTIST_VIEW ->
                R.string.artists
            Constants.FOLDER_VIEW ->
                R.string.folders
            else ->
                R.string.albums
        }
        return getString(stringId)
    }

    private fun finishSetup() {

        binding.artistsFoldersRv.run {
            setHasFixedSize(true)
            itemAnimator = null
            mListAdapter = MusicContainersAdapter()
            adapter = mListAdapter
//            FastScrollerBuilder(this).useMd2Style().build()
            if (sLaunchedByAlbumView) recycledViewPool.setMaxRecycledViews(0, 0)
        }

        binding.searchToolbar.let { stb ->

            stb.inflateMenu(R.menu.menu_search)
            stb.title = getFragmentTitle()
            stb.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort)

            stb.setNavigationOnClickListener {
                mUiControlInterface.onCloseActivity()
            }

            with (stb.menu) {

                mSortMenuItem = Lists.getSelectedSorting(mSorting, this).apply {
                    setTitleColor(Theming.resolveThemeColor(resources))
                }

                with(findItem(R.id.action_search).actionView as SearchView) {
                    setOnQueryTextListener(this@MusicContainersFragment)
                    setOnQueryTextFocusChangeListener { _, hasFocus ->
                        stb.menu.setGroupVisible(R.id.sorting, !hasFocus)
                        stb.menu.findItem(R.id.sleeptimer).isVisible = !hasFocus
                    }
                }
                setMenuOnItemClickListener(this)
            }
        }

        tintSleepTimerIcon(enabled = MediaPlayerHolder.getInstance().isSleepTimer)
    }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        setListDataSource(Lists.processQueryForStringsLists(newText, getSortedList()) ?: mList)
        return false
    }

    private fun setListDataSource(selectedList: List<String>?) {
        if (!selectedList.isNullOrEmpty()) mListAdapter.swapList(selectedList)
    }

    private fun setMenuOnItemClickListener(menu: Menu) {
        binding.searchToolbar.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.sleeptimer -> mUiControlInterface.onOpenSleepTimerDialog()
                else -> if (it.itemId != R.id.action_search) {
                    mSorting = it.order

                    mList = getSortedList()
                    setListDataSource(mList)

                    mSortMenuItem.setTitleColor(
                        Theming.resolveColorAttr(
                            requireContext(),
                            android.R.attr.textColorPrimary
                        )
                    )

                    mSortMenuItem = Lists.getSelectedSorting(mSorting, menu).apply {
                        setTitleColor(Theming.resolveThemeColor(resources))
                    }

                    saveSortingMethodToPrefs(mSorting)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun saveSortingMethodToPrefs(sortingMethod: Int) {
        with(Preferences.getPrefsInstance()) {
            when (mLaunchedBy) {
                Constants.ARTIST_VIEW -> artistsSorting = sortingMethod
                Constants.FOLDER_VIEW -> foldersSorting = sortingMethod
                else -> albumsSorting = sortingMethod
            }
        }
    }

    fun tintSleepTimerIcon(enabled: Boolean) {
        binding.searchToolbar.run {
            Theming.tintSleepTimerMenuItem(this, enabled)
        }
    }

    private inner class MusicContainersAdapter : RecyclerView.Adapter<MusicContainersAdapter.ArtistHolder>() {

        private val itemsToHide = mutableListOf<String>()

        @SuppressLint("NotifyDataSetChanged")
        fun swapList(newItems: List<String>?) {
            mList = newItems?.toMutableList()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistHolder {
            val binding = GenericItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ArtistHolder(binding)
        }

        override fun getItemCount(): Int {
            return mList?.size?:0
        }

        override fun onBindViewHolder(holder: ArtistHolder, position: Int) {
            holder.bindItems(mList?.get(holder.adapterPosition)!!)
        }

        inner class ArtistHolder(private val binding: GenericItemBinding): RecyclerView.ViewHolder(binding.root) {

            fun bindItems(item: String) {

                with(binding) {

                    if (sLaunchedByAlbumView) {
                        albumCover.background.alpha = Theming.getAlbumCoverAlpha(requireContext())
                        mMusicViewModel.deviceMusicByAlbum?.get(item)?.first()?.albumId?.waitForCover(requireContext()) { bmp, error ->
                            albumCover.loadWithError(bmp, error, R.drawable.ic_music_note_cover_alt)
                        }
                    } else {
                        albumCover.handleViewVisibility(show = false)
                    }

                    title.text = item
                    subtitle.text = getItemsSubtitle(item)

                    selector.handleViewVisibility(show = itemsToHide.contains(item))

                    root.setOnClickListener {
                        if (isActionMode) {
                            setItemViewSelected(item, adapterPosition)
                            return@setOnClickListener
                        }
                        mUiControlInterface.onArtistOrFolderSelected(item, mLaunchedBy)
                    }
                    root.setOnLongClickListener {
//                        startActionMode()
                        setItemViewSelected(item, adapterPosition)
                        return@setOnLongClickListener true
                    }
                }
            }
        }

        private fun getItemsSubtitle(item: String): String? {
            return when (mLaunchedBy) {
                Constants.ARTIST_VIEW ->
                    getArtistSubtitle(item)
                Constants.FOLDER_VIEW ->
                    getString(
                        R.string.folder_info,
                        mMusicViewModel.deviceMusicByFolder?.getValue(item)?.size
                    )
                else -> mMusicViewModel.deviceMusicByAlbum?.get(item)?.first()?.artist
            }
        }

        private fun getArtistSubtitle(item: String) = getString(
            R.string.artist_info,
            mMusicViewModel.deviceAlbumsByArtist?.getValue(item)?.size,
            mMusicViewModel.deviceSongsByArtist?.getValue(item)?.size
        )

//        private fun startActionMode() {
//            if (!isActionMode) actionMode = binding.searchToolbar.startActionMode(actionModeCallback)
//        }

        private fun setItemViewSelected(itemTitle: String, position: Int) {
            if (!itemsToHide.remove(itemTitle)) {
                itemsToHide.add(itemTitle)
                mList?.run {
                    if (itemsToHide.size - 1 >= size - 1) itemsToHide.remove(itemTitle)
                }
            }
            actionMode?.title = itemsToHide.size.toString()
            notifyItemChanged(position)
//            if (itemsToHide.isEmpty()) {
//                stopActionMode()
//            } else {
//                actionMode?.menu?.findItem(R.id.action_play)?.isVisible = itemsToHide.size < 2
//            }
        }
    }

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