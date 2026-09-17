package com.hassiel.rideadvisor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.data.HistoryEntry
import com.hassiel.rideadvisor.databinding.ItemHistoryBinding
import com.hassiel.rideadvisor.utils.RejectionReasonText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Adaptador para la pantalla "Historial" (sección 13). */
class HistoryAdapter : ListAdapter<HistoryEntry, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(entry: HistoryEntry) {
            val context = binding.root.context

            binding.textItemApp.text = entry.app

            val price = entry.price?.let { String.format(Locale.getDefault(), "$%.0f", it) } ?: "--"
            val distance = entry.distance?.let { String.format(Locale.getDefault(), "%.1f km", it) } ?: "--"
            binding.textItemPriceDistance.text = "$price  •  $distance"

            binding.textItemDatetime.text = dateFormat.format(Date(entry.timestamp))

            if (entry.accepted) {
                binding.textItemResult.text = context.getString(R.string.overlay_accepted)
                binding.textItemResult.setBackgroundResource(R.drawable.bg_pill_accepted)
                binding.textItemReason.visibility = View.GONE
            } else {
                binding.textItemResult.text = "RECHAZADA"
                binding.textItemResult.setBackgroundResource(R.drawable.bg_pill_rejected)
                val reasonText = RejectionReasonText.forName(context, entry.rejectionReason)
                if (reasonText != null) {
                    binding.textItemReason.visibility = View.VISIBLE
                    binding.textItemReason.text = reasonText
                } else {
                    binding.textItemReason.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryEntry>() {
            override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) =
                oldItem == newItem
        }
    }
}
