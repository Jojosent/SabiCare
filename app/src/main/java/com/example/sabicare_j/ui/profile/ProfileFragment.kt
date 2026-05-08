package com.example.sabicare_j.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sabicare_j.R
import com.example.sabicare_j.SabiCareApplication
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.databinding.FragmentProfileBinding
import com.example.sabicare_j.ui.auth.LoginActivity
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()
    private lateinit var childrenAdapter: ChildrenAdapter

    private var childList = listOf<ChildEntity>()
    private var currentIndex = 0

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

    private fun setupChildSwiper() {
        childViewModel.allChildren.observe(viewLifecycleOwner) { children ->
            childList = children
            if (children.isEmpty()) {
                binding.tvChildName.text = "Баланы қосыңыз"
                binding.tvChildAge.text = ""
                binding.layoutDots.removeAllViews()
                return@observe
            }
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

        val ageMs = System.currentTimeMillis() - child.birthDate
        val days = (ageMs / (1000 * 60 * 60 * 24)).toInt()
        binding.tvChildAge.text = when {
            days < 30  -> "$days күн"
            days < 365 -> "${days / 30} ай"
            else       -> "${days / 365} жас ${(days % 365) / 30} ай"
        }

        // NOTE: Uncomment the block below AFTER you add ivChildAvatar to fragment_profile.xml
        /*
        val photoUri = child.photoUri
        if (!photoUri.isNullOrBlank()) {
            Glide.with(this)
                .load(Uri.parse(photoUri))
                .circleCrop()
                .placeholder(R.drawable.ic_child_placeholder)
                .error(R.drawable.ic_child_placeholder)
                .into(binding.ivChildAvatar)
        } else {
            binding.ivChildAvatar.setImageResource(R.drawable.ic_child_placeholder)
        }
        */
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

    private fun setupChildrenList() {
        childrenAdapter = ChildrenAdapter(
            onChildClick = { child ->
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
        binding.btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Шығу")
                .setMessage("Есептік жазбадан шығасыз ба?")
                .setPositiveButton("Шығу") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            (requireActivity().application as SabiCareApplication).clearAllLocalData()
                        } catch (_: Exception) {}
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                .setNegativeButton("Бас тарту", null)
                .show()
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