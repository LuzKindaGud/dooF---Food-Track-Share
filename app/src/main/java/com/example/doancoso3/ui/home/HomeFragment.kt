package com.example.doancoso3.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doancoso3.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var tvItemCount: TextView
    private lateinit var tvExpiringCount: TextView
    private lateinit var tvWasteSaved: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvActivity1Title: TextView
    private lateinit var tvActivity1Subtitle: TextView
    private lateinit var tvActivity2Title: TextView
    private lateinit var tvActivity2Subtitle: TextView
    private lateinit var tvActivity3Title: TextView
    private lateinit var tvActivity3Subtitle: TextView
    private lateinit var tvMealPrepDesc: TextView
    private lateinit var rvRecentInventory: RecyclerView
    private lateinit var btnAddItem: TextView
    private lateinit var btnViewRecipe: TextView
    private lateinit var tvSeeAll: TextView

    private val recentAdapter = RecentInventoryAdapter { item ->
        // Handle item click - navigate to detail
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun bindViews(view: View) {
        tvItemCount = view.findViewById(R.id.tv_item_count)
        tvExpiringCount = view.findViewById(R.id.tv_expiring_count)
        tvWasteSaved = view.findViewById(R.id.tv_waste_saved)
        tvSavings = view.findViewById(R.id.tv_savings)
        tvActivity1Title = view.findViewById(R.id.tv_activity_1_title)
        tvActivity1Subtitle = view.findViewById(R.id.tv_activity_1_subtitle)
        tvActivity2Title = view.findViewById(R.id.tv_activity_2_title)
        tvActivity2Subtitle = view.findViewById(R.id.tv_activity_2_subtitle)
        tvActivity3Title = view.findViewById(R.id.tv_activity_3_title)
        tvActivity3Subtitle = view.findViewById(R.id.tv_activity_3_subtitle)
        tvMealPrepDesc = view.findViewById(R.id.tv_meal_prep_desc)
        rvRecentInventory = view.findViewById(R.id.rv_recent_inventory)
        btnAddItem = view.findViewById(R.id.btn_add_item)
        btnViewRecipe = view.findViewById(R.id.btn_view_recipe)
        tvSeeAll = view.findViewById(R.id.tv_see_all)
    }

    private fun setupRecyclerView() {
        rvRecentInventory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        btnAddItem.setOnClickListener {
            // TODO: Navigate to add food item screen
        }

        btnViewRecipe.setOnClickListener {
            // TODO: Navigate to meal prep / recipe suggestion
        }

        tvSeeAll.setOnClickListener {
            // TODO: Navigate to full inventory list
        }
    }

    private fun observeViewModel() {
        viewModel.totalItems.observe(viewLifecycleOwner) { count ->
            tvItemCount.text = "$count Items"
        }

        viewModel.expiringCount.observe(viewLifecycleOwner) { count ->
            tvExpiringCount.text = "$count Expiring in 48h"
        }

        viewModel.wasteSaved.observe(viewLifecycleOwner) { value ->
            tvWasteSaved.text = value
        }

        viewModel.savings.observe(viewLifecycleOwner) { value ->
            tvSavings.text = value
        }

        viewModel.recentItems.observe(viewLifecycleOwner) { items ->
            recentAdapter.submitList(items)
        }

        viewModel.familyActivities.observe(viewLifecycleOwner) { activities ->
            if (activities.isNotEmpty()) {
                activities.getOrNull(0)?.let { activity ->
                    tvActivity1Title.text = activity.title
                    tvActivity1Subtitle.text = activity.subtitle
                }
                activities.getOrNull(1)?.let { activity ->
                    tvActivity2Title.text = activity.title
                    tvActivity2Subtitle.text = activity.subtitle
                } ?: run {
                    tvActivity2Title.visibility = View.GONE
                    tvActivity2Subtitle.visibility = View.GONE
                }
                activities.getOrNull(2)?.let { activity ->
                    tvActivity3Title.text = activity.title
                    tvActivity3Subtitle.text = activity.subtitle
                } ?: run {
                    tvActivity3Title.visibility = View.GONE
                    tvActivity3Subtitle.visibility = View.GONE
                }
            }
        }

        viewModel.mealPrepCount.observe(viewLifecycleOwner) { count ->
            tvMealPrepDesc.text = "You have $count ingredients expiring soon that work great in a stir-fry."
        }
    }
}
