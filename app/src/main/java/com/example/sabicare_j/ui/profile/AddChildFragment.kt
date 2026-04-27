package com.example.sabicare_j.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sabicare_j.R
import com.example.sabicare_j.data.local.entities.ChildEntity
import com.example.sabicare_j.databinding.FragmentAddChildBinding
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddChildFragment : Fragment() {

    private var _binding: FragmentAddChildBinding? = null
    private val binding get() = _binding!!
    private val childViewModel: ChildViewModel by activityViewModels()

    private val childId: Long by lazy { arguments?.getLong("childId", -1L) ?: -1L }
    private val isOnboarding: Boolean by lazy { arguments?.getBoolean("isOnboarding", false) ?: false }

    private var selectedDateMillis: Long = 0L
    private var selectedGender: String = ""
    private var selectedPhotoUri: String? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) { /* not all URIs support persistable permission */ }
                selectedPhotoUri = uri.toString()
                loadChildPhoto(uri.toString())
            }
        }

    companion object {
        fun newInstance(isOnboarding: Boolean = false) = AddChildFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isOnboarding", isOnboarding)
                putLong("childId", -1L)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddChildBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isEdit = childId > 0

        setupToolbar(isEdit)
        setupPhotoSection()
        setupGenderToggle()
        setupDatePicker()
        setupSaveButton(isEdit)
        if (isEdit) setupDeleteButton()
        else binding.btnDelete.visibility = View.GONE

        if (isEdit) loadExistingChild()
    }

    // ── Toolbar ────────────────────────────────────────────────────────────
    private fun setupToolbar(isEdit: Boolean) {
        binding.toolbar.title = if (isEdit) "Балаға өзгерістер" else "Бала қосу"
        binding.toolbar.setNavigationOnClickListener {
            if (isOnboarding) requireActivity().finish()
            else findNavController().navigateUp()
        }
    }

    // ── Photo picker ───────────────────────────────────────────────────────
    private fun setupPhotoSection() {
        binding.ivChildAvatar.setOnClickListener { launchPhotoPicker() }
        binding.btnPickPhoto.setOnClickListener { launchPhotoPicker() }
    }

    private fun launchPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun loadChildPhoto(uri: String?) {
        if (uri != null) {
            Glide.with(this)
                .load(Uri.parse(uri))
                .circleCrop()
                .placeholder(R.drawable.ic_child_placeholder)
                .into(binding.ivChildAvatar)
            binding.tvPhotoHint.text = "Фотоны өзгерту"
        } else {
            binding.ivChildAvatar.setImageResource(R.drawable.ic_child_placeholder)
            binding.tvPhotoHint.text = "Фото қосу"
        }
    }

    // ── Gender ─────────────────────────────────────────────────────────────
    private fun setupGenderToggle() {
        binding.btnMale.setOnClickListener {
            selectedGender = "MALE"
            binding.btnMale.isSelected = true
            binding.btnFemale.isSelected = false
            updateGenderUI()
        }
        binding.btnFemale.setOnClickListener {
            selectedGender = "FEMALE"
            binding.btnMale.isSelected = false
            binding.btnFemale.isSelected = true
            updateGenderUI()
        }
    }

    private fun updateGenderUI() {
        val activeColor = requireContext().getColor(R.color.primary)
        val inactiveColor = requireContext().getColor(R.color.surface_variant)
        val activeTextColor = requireContext().getColor(R.color.on_primary)
        val inactiveTextColor = requireContext().getColor(R.color.on_surface)

        if (selectedGender == "MALE") {
            binding.btnMale.backgroundTintList = android.content.res.ColorStateList.valueOf(activeColor)
            binding.btnMale.setTextColor(activeTextColor)
            binding.btnFemale.backgroundTintList = android.content.res.ColorStateList.valueOf(inactiveColor)
            binding.btnFemale.setTextColor(inactiveTextColor)
        } else {
            binding.btnFemale.backgroundTintList = android.content.res.ColorStateList.valueOf(activeColor)
            binding.btnFemale.setTextColor(activeTextColor)
            binding.btnMale.backgroundTintList = android.content.res.ColorStateList.valueOf(inactiveColor)
            binding.btnMale.setTextColor(inactiveTextColor)
        }
    }

    // ── Date picker ────────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener { openDatePicker() }
        binding.tilBirthDate.setEndIconOnClickListener { openDatePicker() }
    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        if (selectedDateMillis > 0) cal.timeInMillis = selectedDateMillis

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = cal.timeInMillis
                binding.etBirthDate.setText(dateFormat.format(cal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    // ── Load existing child ────────────────────────────────────────────────
    private fun loadExistingChild() {
        childViewModel.allChildren.observe(viewLifecycleOwner) { children ->
            val child = children.firstOrNull { it.id == childId } ?: return@observe
            binding.etChildName.setText(child.name)
            selectedDateMillis = child.birthDate
            binding.etBirthDate.setText(dateFormat.format(Date(child.birthDate)))
            selectedGender = child.gender
            selectedPhotoUri = child.photoUri
            if (selectedGender == "MALE") {
                binding.btnMale.performClick()
            } else {
                binding.btnFemale.performClick()
            }
            loadChildPhoto(child.photoUri)
        }
    }

    // ── Save ───────────────────────────────────────────────────────────────
    private fun setupSaveButton(isEdit: Boolean) {
        binding.btnSave.text = if (isEdit) "Сақтау" else "Қосу"
        binding.btnSave.setOnClickListener {
            val name = binding.etChildName.text?.toString()?.trim() ?: ""
            if (name.isBlank()) {
                binding.tilChildName.error = "Атын енгізіңіз"
                return@setOnClickListener
            }
            if (selectedDateMillis == 0L) {
                binding.tilBirthDate.error = "Туған күнін таңдаңыз"
                return@setOnClickListener
            }
            if (selectedGender.isBlank()) {
                Toast.makeText(requireContext(), "Жынысын таңдаңыз", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.tilChildName.error = null
            binding.tilBirthDate.error = null

            if (isEdit) {
                val updated = ChildEntity(
                    id = childId,
                    name = name,
                    birthDate = selectedDateMillis,
                    gender = selectedGender,
                    photoUri = selectedPhotoUri
                )
                childViewModel.updateChild(updated)
                Toast.makeText(requireContext(), "Өзгерістер сақталды ✓", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                val newChild = ChildEntity(
                    name = name,
                    birthDate = selectedDateMillis,
                    gender = selectedGender,
                    photoUri = selectedPhotoUri,
                    isActive = true
                )
                childViewModel.addChild(newChild)
                if (isOnboarding) {
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    private fun setupDeleteButton() {
        binding.btnDelete.visibility = View.VISIBLE
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Баланы жою")
                .setMessage("\"${binding.etChildName.text}\" профилі және барлық өлшемдер жойылады. Растайсыз ба?")
                .setPositiveButton("Жою") { _, _ ->
                    val children = childViewModel.allChildren.value ?: return@setPositiveButton
                    val child = children.firstOrNull { it.id == childId } ?: return@setPositiveButton
                    childViewModel.deleteChild(child)
                    Toast.makeText(requireContext(), "Жойылды", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .setNegativeButton("Бас тарту", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}