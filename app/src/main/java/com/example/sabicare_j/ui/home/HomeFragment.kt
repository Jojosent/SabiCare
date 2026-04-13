package com.example.sabicare_j.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.FragmentHomeBinding
import com.example.sabicare_j.ui.shared.ChildViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var medicationAdapter: MedicationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        observeData()
    }

    private fun setupAdapters() {
        // Reminders
        reminderAdapter = ReminderAdapter { reminder ->
            // Navigate to TrackerFragment with pre-selected type
            val bundle = android.os.Bundle().apply {
                putString("measurementType", reminder.type.name)
            }
            findNavController().navigate(R.id.action_home_to_addEntry, bundle)
        }
        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
            isNestedScrollingEnabled = false
        }

        // Articles — horizontal scroll
        articleAdapter = ArticleAdapter()
        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = articleAdapter
        }

        // Medications — horizontal scroll
        medicationAdapter = MedicationAdapter()
        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = medicationAdapter
        }
    }

    private fun observeData() {
        // Active child → update header card + load reminders
        childViewModel.activeChild.observe(viewLifecycleOwner) { child ->
            if (child != null) {
                binding.tvChildName.text = child.name
                binding.tvChildAge.text = homeViewModel.getAgeString(child.birthDate)
                binding.tvGenderBadge.text =
                    if (child.gender == "MALE") "👦 Ұл бала" else "👧 Қыз бала"
                homeViewModel.loadRemindersForChild(child.id)
            }
        }

        // Reminders
        homeViewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            reminderAdapter.submitList(reminders)

            // Show/hide "all good" empty state
            val hasDue = reminders.any { it.isDue }
            binding.tvRemindersSubtitle.text = if (hasDue)
                "Бүгін өлшеу уақыты келген параметрлер бар"
            else
                "Барлық өлшемдер жоспарда ✓"
        }

        // Articles
        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter.submitList(articles)
        }

        // Medications
        homeViewModel.medications.observe(viewLifecycleOwner) { meds ->
            medicationAdapter.submitList(meds)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh reminders when returning from Tracker
        childViewModel.activeChild.value?.id?.let { id ->
            homeViewModel.loadRemindersForChild(id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}