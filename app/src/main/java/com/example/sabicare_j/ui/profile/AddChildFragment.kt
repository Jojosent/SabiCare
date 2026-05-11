package com.example.sabicare_j.ui.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.databinding.FragmentAddChildBinding
import com.example.sabicare_j.ui.shared.ChildViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddChildFragment : Fragment() {

    private var _binding: FragmentAddChildBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()

    // childId = -1 → new child, > 0 → edit existing
    private val childId: Long by lazy { arguments?.getLong("childId", -1L) ?: -1L }
    private val isOnboarding: Boolean by lazy { arguments?.getBoolean("isOnboarding", false) ?: false }

    private var selectedDateMillis: Long = 0L
    private var selectedGender: String = ""
    private var selectedPhotoUri: Uri? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Photo picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            binding.ivChildPhoto.setImageURI(it)
        }
    }

    companion object {
        fun newInstance(isOnboarding: Boolean = false): AddChildFragment {
            return AddChildFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isOnboarding", isOnboarding)
                    putLong("childId", -1L)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChildBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupGenderChips()
        setupDatePicker()
        setupPhotoSelector()
        setupSaveButton()

        // If editing existing child — prefill fields
        if (childId > 0) {
            prefillForEdit()
        }
    }

    private fun setupUI() {
        if (isOnboarding) {
            binding.tvTitle.text = getString(R.string.onboarding_add_baby)
            binding.btnSave.text = getString(R.string.onboarding_start)
        } else if (childId > 0) {
            binding.tvTitle.text = getString(R.string.edit_child)
        } else {
            binding.tvTitle.text = getString(R.string.add_child)
        }
    }

    private fun setupPhotoSelector() {
        binding.btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.ivChildPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupGenderChips() {
        binding.chipMale.setOnClickListener {
            selectedGender = "MALE"
            binding.chipMale.isChecked = true
            binding.chipFemale.isChecked = false
        }
        binding.chipFemale.setOnClickListener {
            selectedGender = "FEMALE"
            binding.chipFemale.isChecked = true
            binding.chipMale.isChecked = false
        }
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.tilBirthDate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day, 0, 0, 0)
                selected.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = selected.timeInMillis
                binding.etBirthDate.setText(dateFormat.format(selected.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveChild()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (selectedDateMillis == 0L) {
            binding.tilBirthDate.error = getString(R.string.error_date_required)
            isValid = false
        } else {
            binding.tilBirthDate.error = null
        }

        if (selectedGender.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_gender_required), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveChild() {
        val name = binding.etName.text.toString().trim()

        val child = ChildEntity(
            id = if (childId > 0) childId else 0,
            name = name,
            birthDate = selectedDateMillis,
            gender = selectedGender,
            photoUri = selectedPhotoUri?.toString(),
            isActive = true
        )

        if (childId > 0) {
            childViewModel.updateChild(child)
        } else {
            childViewModel.addChild(child)
        }

        if (isOnboarding) {
            (activity as? com.example.sabicare_j.ui.onboarding.OnboardingActivity)?.finishOnboarding()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun prefillForEdit() {
        childViewModel.allChildren.observe(viewLifecycleOwner) { children ->
            val child = children.find { it.id == childId } ?: return@observe
            binding.etName.setText(child.name)
            selectedDateMillis = child.birthDate
            binding.etBirthDate.setText(dateFormat.format(Date(child.birthDate)))
            selectedGender = child.gender
            if (child.gender == "MALE") {
                binding.chipMale.isChecked = true
            } else {
                binding.chipFemale.isChecked = true
            }

            // Load photo if exists
            if (!child.photoUri.isNullOrEmpty()) {
                selectedPhotoUri = Uri.parse(child.photoUri)
                binding.ivChildPhoto.setImageURI(selectedPhotoUri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}