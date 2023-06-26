package com.dhp.musicplayer.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.base.BaseBottomSheetDialogFragment
import com.dhp.musicplayer.databinding.ModalRvBinding
import com.dhp.musicplayer.databinding.SleeptimerItemBinding
import com.dhp.musicplayer.extensions.handleViewVisibility
import com.dhp.musicplayer.player.MediaControlInterface
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.dhp.musicplayer.player.UIControlInterface
import com.dhp.musicplayer.utils.Theming

class RecyclerSheet: BaseBottomSheetDialogFragment<ModalRvBinding>() {

    // sheet type
    var sheetType = ACCENT_TYPE
    // interfaces
    private lateinit var mUIControlInterface: UIControlInterface
    private lateinit var mMediaControlInterface: MediaControlInterface
    var onQueueCancelled: (() -> Unit)? = null
    var onFavoritesDialogCancelled: (() -> Unit)? = null
    var onSleepTimerDialogCancelled: (() -> Unit)? = null
    var onSleepTimerEnabled: ((Boolean, String) -> Unit)? = null

    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private val mPreferences get() = Preferences.getPrefsInstance()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ModalRvBinding.inflate(layoutInflater, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getString(TAG_TYPE)?.let { which ->
            sheetType = which
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUIControlInterface = activity as UIControlInterface
            mMediaControlInterface = activity as MediaControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onSleepTimerDialogCancelled?.invoke()
    }

    override fun initUI() {
        super.initUI()
        var dialogTitle = getString(R.string.accent_pref_title)
        binding.run {
            when (sheetType) {
                SLEEPTIMER_TYPE -> {

                    dialogTitle = getString(R.string.sleeptimer)

                    val sleepTimerAdapter = SleepTimerAdapter()
                    modalRv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                    modalRv.adapter = sleepTimerAdapter
                    modalRv.setHasFixedSize(true)

                    sleepTimerElapsed.handleViewVisibility(show = false)

                    btnNegative.setOnClickListener {
                        dismiss()
                    }
                    btnPositive.setOnClickListener {
                        with(mMediaPlayerHolder) {
                            val isEnabled = pauseBySleepTimer(sleepTimerAdapter.getSelectedSleepTimerValue())
                            onSleepTimerEnabled?.invoke(isEnabled, sleepTimerAdapter.getSelectedSleepTimer())
                        }
                        dismiss()
                    }
                }

                SLEEPTIMER_ELAPSED_TYPE -> {

                    dialogTitle = getString(R.string.sleeptimer)

                    modalRv.handleViewVisibility(show = false)

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    with(btnPositive) {
                        text = getString(R.string.sleeptimer_stop)
                        contentDescription = getString(R.string.sleeptimer_cancel_desc)
                        setOnClickListener {
                            mMediaPlayerHolder.cancelSleepTimer()
                            onSleepTimerEnabled?.invoke(false, "")
                            dismiss()
                        }
                    }
                }
            }
            title.text = dialogTitle

        }
    }

    fun updateCountdown(value: String) {
        binding.sleepTimerElapsed.text = value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onQueueCancelled?.invoke()
        onFavoritesDialogCancelled?.invoke()
        onSleepTimerDialogCancelled?.invoke()
    }

    private inner class SleepTimerAdapter : RecyclerView.Adapter<SleepTimerAdapter.SleepTimerHolder>() {

        private val sleepOptions = resources.getStringArray(R.array.sleepOptions)
        private val sleepOptionValues = resources.getIntArray(R.array.sleepOptionsValues)

        private var mSelectedPosition = 0

        private val mDefaultTextColor =
            Theming.resolveColorAttr(requireActivity(), android.R.attr.textColorPrimary)

        fun getSelectedSleepTimer(): String = sleepOptions[mSelectedPosition]
        fun getSelectedSleepTimerValue() = sleepOptionValues[mSelectedPosition].toLong()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepTimerHolder {
            val binding = SleeptimerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SleepTimerHolder(binding)
        }

        override fun getItemCount(): Int {
            return sleepOptions.size
        }

        override fun onBindViewHolder(holder: SleepTimerHolder, position: Int) {
            holder.bindItems(sleepOptions[holder.adapterPosition])
        }

        inner class SleepTimerHolder(private val binding: SleeptimerItemBinding): RecyclerView.ViewHolder(binding.root) {

            fun bindItems(itemSleepOption: String) {

                with(binding.root) {
                    text = itemSleepOption
                    contentDescription = itemSleepOption
                    setTextColor(if (mSelectedPosition == adapterPosition) {
                        Theming.resolveThemeColor(resources)
                    } else {
                        mDefaultTextColor
                    })
                    setOnClickListener {
                        notifyItemChanged(mSelectedPosition)
                        mSelectedPosition = adapterPosition
                        notifyItemChanged(mSelectedPosition)
                    }
                }
            }
        }
    }


    companion object {

        const val TAG_MODAL_RV = "MODAL_RV"
        private const val TAG_TYPE = "MODAL_RV_TYPE"

        // Modal rv type
        const val ACCENT_TYPE = "MODAL_ACCENT"
        const val TABS_TYPE = "MODAL_TABS"
        const val FILTERS_TYPE = "MODAL_FILTERS"
        const val QUEUE_TYPE = "MODAL_QUEUE"
        const val FAV_TYPE = "MODAL_FAVORITES"
        const val SLEEPTIMER_TYPE = "MODAL_SLEEPTIMER"
        const val SLEEPTIMER_ELAPSED_TYPE = "MODAL_SLEEPTIMER_ELAPSED"
        const val NOTIFICATION_ACTIONS_TYPE = "MODAL_NOTIFICATION_ACTIONS"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [RecyclerSheet].
         */
        @JvmStatic
        fun newInstance(which: String) = RecyclerSheet().apply {
            arguments = bundleOf(TAG_TYPE to which)
        }
    }

}