package com.example.doancoso3.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doancoso3.R

class RecentInventoryAdapter(
    private val onItemClick: (RecentInventoryItem) -> Unit = {}
) : ListAdapter<RecentInventoryItem, RecentInventoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFoodIcon: ImageView = itemView.findViewById(R.id.iv_food_icon)
        private val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        private val tvFoodExpiry: TextView = itemView.findViewById(R.id.tv_food_expiry)
        private val tvFoodQuantity: TextView = itemView.findViewById(R.id.tv_food_quantity)
        private val viewStatusDot: View = itemView.findViewById(R.id.view_status_dot)

        fun bind(item: RecentInventoryItem) {
            tvFoodName.text = item.name
            tvFoodExpiry.text = item.expiryText
            tvFoodQuantity.text = item.quantity

            // Set icon based on food name keywords
            val iconRes = getFoodIcon(item.name)
            ivFoodIcon.setImageResource(iconRes)

            // Set colors based on expiry status
            val context = itemView.context
            when (item.status) {
                ExpiryDisplayStatus.GOOD -> {
                    tvFoodQuantity.setTextColor(ContextCompat.getColor(context, R.color.lime_primary))
                    tvFoodExpiry.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
                    viewStatusDot.setBackgroundResource(R.drawable.bg_dot_lime)
                }
                ExpiryDisplayStatus.WARNING -> {
                    tvFoodQuantity.setTextColor(ContextCompat.getColor(context, R.color.lime_primary))
                    tvFoodExpiry.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
                    viewStatusDot.setBackgroundResource(R.drawable.bg_dot_lime)
                }
                ExpiryDisplayStatus.EXPIRED -> {
                    tvFoodQuantity.setTextColor(ContextCompat.getColor(context, R.color.error))
                    tvFoodExpiry.setTextColor(ContextCompat.getColor(context, R.color.error))
                    tvFoodQuantity.text = "EXPIRED"
                    viewStatusDot.setBackgroundResource(R.drawable.bg_dot_red)
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun getFoodIcon(name: String): Int {
            val lowerName = name.lowercase()
            return when {
                lowerName.contains("egg") || lowerName.contains("trứng") -> R.drawable.ic_egg
                lowerName.contains("milk") || lowerName.contains("sữa") || lowerName.contains("dairy") -> R.drawable.ic_milk_bottle
                lowerName.contains("chicken") || lowerName.contains("meat") || lowerName.contains("thịt") || lowerName.contains("gà") -> R.drawable.ic_meat
                lowerName.contains("salad") || lowerName.contains("vegetable") || lowerName.contains("rau") -> R.drawable.ic_salad_bowl
                else -> R.drawable.ic_egg // Default food icon
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecentInventoryItem>() {
        override fun areItemsTheSame(oldItem: RecentInventoryItem, newItem: RecentInventoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentInventoryItem, newItem: RecentInventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
