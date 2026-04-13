package com.example.sabicare_j.ui.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.FragmentProfileBinding
import com.example.sabicare_j.ui.shared.ChildViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()
    private lateinit var childrenAdapter: ChildrenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChildrenList()
        setupButtons()
        observeData()
    }

    private fun setupChildrenList() {
        childrenAdapter = ChildrenAdapter(
            onChildClick = { child ->
                // Switch active child
                childViewModel.switchActiveChild(child.id)
            },
            onEditClick = { child ->
                val bundle = Bundle().apply { putLong("childId", child.id) }
                findNavController().navigate(R.id.action_profile_to_addChild, bundle)
            },
            onDeleteClick = { child ->
                childViewModel.deleteChild(child)
            }
        )
        binding.rvChildren.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = childrenAdapter
        }
    }

    private fun setupButtons() {
        binding.fabAddChild.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addChild)
        }
        binding.itemSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }
    }

    private fun observeData() {
        childViewModel.allChildren.observe(viewLifecycleOwner) { children ->
            childrenAdapter.submitList(children)
        }
        childViewModel.activeChild.observe(viewLifecycleOwner) { activeChild ->
            childrenAdapter.setActiveChildId(activeChild?.id ?: -1L)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}