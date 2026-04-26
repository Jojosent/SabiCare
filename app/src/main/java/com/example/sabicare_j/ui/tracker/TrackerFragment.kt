package com.example.sabicare_j.ui.tracker

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.data.standards.GrowthStandardsHelper
import com.example.sabicare_j.databinding.FragmentTrackerBinding
import com.example.sabicare_j.ui.shared.ChildViewModel

class TrackerFragment : Fragment() {

    private var _binding: FragmentTrackerBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildViewModel by activityViewModels()
    private val trackerViewModel: TrackerViewModel by viewModels()
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = MeasurementAdapter { cardState ->
            // On card click → open AddEntry with this type pre-selected
            val bundle = Bundle().apply {
                putString("measurementType", cardState.type.name)
            }
            findNavController().navigate(R.id.action_tracker_to_addEntry, bundle)
        }
        binding.rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeasurements.adapter = adapter
    }

    private fun observeData() {
        childViewModel.activeChild.observe(viewLifecycleOwner) { child ->
            if (child != null) {
                binding.tvChildName.text = child.name
                trackerViewModel.loadForChild(child.id)
            }
        }

        trackerViewModel.cards.observe(viewLifecycleOwner) { cards ->
            adapter.submitList(cards)
        }
    }

    private fun setupFab() {
        binding.fabAddEntry.setOnClickListener {
            findNavController().navigate(R.id.action_tracker_to_addEntry)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh when coming back from AddEntry
        childViewModel.activeChild.value?.id?.let {
            trackerViewModel.refreshCards(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}