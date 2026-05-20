package com.example.doancoso3.ui.fooditem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext()).apply {
            text = "Inventory"
            textSize = 24f
            setTextColor(resources.getColor(com.example.doancoso3.R.color.on_surface, null))
            setPadding(48, 48, 48, 48)
        }
        return textView
    }
}
