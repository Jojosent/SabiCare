package com.example.sabicare_j.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.databinding.FragmentProfileBinding
import com.example.sabicare_j.ui.shared.ChildViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()
    private lateinit var childrenAdapter: ChildrenAdapter


    // ProfileFragment.kt ішіне қосылатын жолдар:

    private var childList = listOf<ChildEntity>()
    private var currentIndex = 0

    private fun setupChildSwiper() {
        childViewModel.allChildren.observe(viewLifecycleOwner) { children ->
            childList = children
            if (children.isEmpty()) {
                binding.tvChildName.text = "Баланы қосыңыз"
                binding.tvChildAge.text = ""
                binding.layoutDots.removeAllViews()
                return@observe
            }
            // Белсенді баланың индексін табу
            val activeId = childViewModel.activeChild.value?.id
            currentIndex = children.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
            updateChildDisplay()
            updateDots()
        }

        binding.btnPrevChild.setOnClickListener {
            if (childList.isEmpty()) return@setOnClickListener
            currentIndex = (currentIndex - 1 + childList.size) % childList.size
            switchToChild(childList[currentIndex].id)
        }

        binding.btnNextChild.setOnClickListener {
            if (childList.isEmpty()) return@setOnClickListener
            currentIndex = (currentIndex + 1) % childList.size
            switchToChild(childList[currentIndex].id)
        }

        binding.btnAddChild.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addChild)
        }
    }

    private fun switchToChild(childId: Long) {
        childViewModel.switchActiveChild(childId)
        updateChildDisplay()
        updateDots()
    }

    private fun updateChildDisplay() {
        if (childList.isEmpty()) return
        val child = childList[currentIndex]
        binding.tvChildName.text = child.name

        // Жасты есептеу
        val ageMs = System.currentTimeMillis() - child.birthDate
        val days = (ageMs / (1000 * 60 * 60 * 24)).toInt()
        binding.tvChildAge.text = when {
            days < 30 -> "$days күн"
            days < 365 -> "${days / 30} ай"
            else -> "${days / 365} жас ${(days % 365) / 30} ай"
        }
    }

    private fun updateDots() {
        binding.layoutDots.removeAllViews()
        val context = requireContext()
        childList.forEachIndexed { i, _ ->
            val dot = View(context)
            val size = if (i == currentIndex) 10 else 7
            val params = LinearLayout.LayoutParams(
                (size * resources.displayMetrics.density).toInt(),
                (size * resources.displayMetrics.density).toInt()
            ).apply { setMargins(4, 0, 4, 0) }
            dot.layoutParams = params
            dot.background = if (i == currentIndex)
                ContextCompat.getDrawable(context, R.drawable.dot_active)
            else
                ContextCompat.getDrawable(context, R.drawable.dot_inactive)
            binding.layoutDots.addView(dot)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChildSwiper()
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
