package com.example.doancoso3.ui.fooditem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private val viewModel: FoodItemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InventoryComposeScreen(
                    viewModel = viewModel,
                    onAddItemClick = {
                        // Navigate to Add Item Screen
                        // findNavController().navigate(R.id.action_inventoryFragment_to_addEditFoodItemFragment)
                    }
                )
            }
        }
    }
}
