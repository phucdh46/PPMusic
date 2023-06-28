package com.dhp.musicplayer.ui.setting.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.dhp.musicplayer.Preferences
import com.dhp.musicplayer.R
import com.dhp.musicplayer.databinding.AccentItemBinding
import com.dhp.musicplayer.utils.Theming

class AccentsAdapter(private val accents: IntArray):
    RecyclerView.Adapter<AccentsAdapter.AccentsHolder>() {

    var selectedAccent = Preferences.getPrefsInstance().accent

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentsHolder {
        val binding = AccentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccentsHolder(binding)
    }

    override fun getItemCount() = accents.size

    override fun onBindViewHolder(holder: AccentsHolder, position: Int) {
        holder.bindItems(accents[holder.adapterPosition])
    }

    inner class AccentsHolder(private val binding: AccentItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindItems(color: Int) {

            with(binding.root) {

                val accentFullName = Theming.getAccentName(resources, adapterPosition)
                contentDescription = accentFullName

                setCardBackgroundColor(color)
                radius = resources.getDimensionPixelSize(
                    if (adapterPosition != selectedAccent) {
                        strokeWidth = 0
                        R.dimen.accent_radius
                    } else {
                        strokeWidth = resources.getDimensionPixelSize(R.dimen.accent_stroke)
                        val stroke = if (ColorUtils.calculateLuminance(color) < 0.35) Color.WHITE else Color.DKGRAY
                        strokeColor = ColorUtils.setAlphaComponent(stroke, 75)
                        R.dimen.accent_radius_alt
                    }
                ).toFloat()

                setOnClickListener {
                    if (adapterPosition != selectedAccent) {
                        notifyItemChanged(selectedAccent)
                        selectedAccent = adapterPosition
                        notifyItemChanged(adapterPosition)
                    }
                }

                setOnLongClickListener {
                    Toast.makeText(context, accentFullName, Toast.LENGTH_SHORT)
                        .show()
                    return@setOnLongClickListener true
                }
            }
        }
    }
}