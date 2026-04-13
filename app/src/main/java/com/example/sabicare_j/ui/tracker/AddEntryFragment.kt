package com.example.sabicare_j.ui.tracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.sabicare_j.data.local.entities.MeasurementType
import com.example.sabicare_j.databinding.FragmentAddEntryBinding
import com.example.sabicare_j.ui.shared.ChildViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEntryFragment : Fragment() {

    private var _binding: FragmentAddEntryBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildViewModel by activityViewModels()
    private val trackerViewModel: TrackerViewModel by viewModels(
        ownerProducer = { requireParentFragment().requireParentFragment() }
    )

    private var selectedType: MeasurementType = MeasurementType.HEIGHT
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Pre-selected type from args (when clicking a specific card)
    private val preSelectedType: String? by lazy {
        arguments?.getString("measurementType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeChips()
        setupDatePicker()
        setupSaveButton()
        setupBackButton()

        // Pre-select type if coming from a card
        preSelectedType?.let { typeName ->
            try {
                selectedType = MeasurementType.valueOf(typeName)
                selectChipForType(selectedType)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    private fun setupTypeChips() {
        // Set initial chip selection
        selectChipForType(selectedType)

        binding.chipHeight.setOnClickListener {
            selectedType = MeasurementType.HEIGHT
            updateUnitLabel()
        }
        binding.chipWeight.setOnClickListener {
            selectedType = MeasurementType.WEIGHT
            updateUnitLabel()
        }
        binding.chipFeedings.setOnClickListener {
            selectedType = MeasurementType.FEEDINGS_COUNT
            updateUnitLabel()
        }
        binding.chipCalories.setOnClickListener {
            selectedType = MeasurementType.CALORIES
            updateUnitLabel()
        }
        binding.chipSleep.setOnClickListener {
            selectedType = MeasurementType.SLEEP_DURATION
            updateUnitLabel()
        }
    }

    private fun selectChipForType(type: MeasurementType) {
        // Uncheck all first
        binding.chipHeight.isChecked = false
        binding.chipWeight.isChecked = false
        binding.chipFeedings.isChecked = false
        binding.chipCalories.isChecked = false
        binding.chipSleep.isChecked = false

        // Check the right one
        when (type) {
            MeasurementType.HEIGHT -> binding.chipHeight.isChecked = true
            MeasurementType.WEIGHT -> binding.chipWeight.isChecked = true
            MeasurementType.FEEDINGS_COUNT -> binding.chipFeedings.isChecked = true
            MeasurementType.CALORIES -> binding.chipCalories.isChecked = true
            MeasurementType.SLEEP_DURATION -> binding.chipSleep.isChecked = true
        }
        updateUnitLabel()
    }

    private fun updateUnitLabel() {
        binding.tilValue.hint = "${selectedType.displayNameRu} (${selectedType.unit})"
        binding.tvHint.text = getHintForType(selectedType)
    }

    private fun getHintForType(type: MeasurementType): String {
        return when (type) {
            MeasurementType.HEIGHT -> "Мысалы: 52.5 см"
            MeasurementType.WEIGHT -> "Мысалы: 3500 г"
            MeasurementType.FEEDINGS_COUNT -> "Мысалы: 8 рет"
            MeasurementType.CALORIES -> "Мысалы: 450 ккал"
            MeasurementType.SLEEP_DURATION -> "Мысалы: 840 мин (14 сағат)"
        }
    }

    private fun setupDatePicker() {
        // Default = today
        binding.etDate.setText(dateFormat.format(Date(selectedDateMillis)))

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day, 12, 0, 0)
                selectedDateMillis = selected.timeInMillis
                binding.etDate.setText(dateFormat.format(selected.time))
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
            val valueStr = binding.etValue.text.toString().trim()
            if (valueStr.isEmpty()) {
                binding.tilValue.error = "Мән енгізіңіз"
                return@setOnClickListener
            }
            val value = valueStr.toDoubleOrNull()
            if (value == null || value <= 0) {
                binding.tilValue.error = "Дұрыс мән енгізіңіз"
                return@setOnClickListener
            }
            binding.tilValue.error = null

            val note = binding.etNote.text.toString().trim().ifEmpty { null }
            val childId = childViewModel.activeChild.value?.id ?: return@setOnClickListener

            // ViewModel через Application
            val trackerVm = TrackerViewModel(requireActivity().application)
            trackerVm.addMeasurement(childId, selectedType, value, note, selectedDateMillis)

            Toast.makeText(requireContext(), "✅ Сақталды!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}