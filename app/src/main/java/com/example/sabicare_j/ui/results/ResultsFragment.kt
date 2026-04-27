package com.example.sabicare_j.ui.results

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.R
import com.example.sabicare_j.databinding.FragmentResultsBinding
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.utils.PdfGenerator
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildViewModel by activityViewModels()
    private val resultsViewModel: ResultsViewModel by viewModels()
    private lateinit var adapter: ResultsAdapter

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupPdfButton()
    }

    private fun setupRecyclerView() {
        adapter = ResultsAdapter()
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    private fun observeData() {
        childViewModel.activeChild.observe(viewLifecycleOwner) { child ->
            if (child != null) {
                binding.tvChildName.text = child.name
                resultsViewModel.loadForChild(child)
            }
        }
        resultsViewModel.resultCards.observe(viewLifecycleOwner) { cards ->
            adapter.submitList(cards)
        }
    }

    private fun setupPdfButton() {
        binding.btnExportPdf.setOnClickListener { showDateRangeDialog() }
    }

    private fun showDateRangeDialog() {
        val child = childViewModel.activeChild.value ?: return
        val cards = resultsViewModel.resultCards.value ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_pdf_date_range, null)
        val tvFrom = dialogView.findViewById<TextView>(R.id.tvDateFrom)
        val tvTo = dialogView.findViewById<TextView>(R.id.tvDateTo)
        val btnFrom = dialogView.findViewById<View>(R.id.btnPickFrom)
        val btnTo = dialogView.findViewById<View>(R.id.btnPickTo)
        val chip7 = dialogView.findViewById<Chip>(R.id.chip7days)
        val chip30 = dialogView.findViewById<Chip>(R.id.chip30days)
        val chip3m = dialogView.findViewById<Chip>(R.id.chip3months)
        val chip6m = dialogView.findViewById<Chip>(R.id.chip6months)
        val chipAll = dialogView.findViewById<Chip>(R.id.chipAllTime)

        val calTo = Calendar.getInstance().also { c ->
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59)
        }
        val calFrom = Calendar.getInstance().also { c ->
            c.add(Calendar.DAY_OF_YEAR, -30)
            c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0)
        }

        fun refresh() { tvFrom.text = dateFormat.format(calFrom.time); tvTo.text = dateFormat.format(calTo.time) }
        refresh()

        fun setRange(daysBack: Int, fromBirth: Boolean = false) {
            calTo.timeInMillis = System.currentTimeMillis()
            calTo.set(Calendar.HOUR_OF_DAY, 23); calTo.set(Calendar.MINUTE, 59); calTo.set(Calendar.SECOND, 59)
            if (fromBirth) { calFrom.timeInMillis = child.birthDate }
            else {
                calFrom.timeInMillis = calTo.timeInMillis; calFrom.add(Calendar.DAY_OF_YEAR, -daysBack)
                calFrom.set(Calendar.HOUR_OF_DAY, 0); calFrom.set(Calendar.MINUTE, 0); calFrom.set(Calendar.SECOND, 0)
            }
            refresh()
        }

        chip7.setOnClickListener { setRange(7) }
        chip30.setOnClickListener { setRange(30) }
        chip3m.setOnClickListener { setRange(90) }
        chip6m.setOnClickListener { setRange(180) }
        chipAll.setOnClickListener { setRange(0, fromBirth = true) }

        btnFrom.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calFrom.set(y, m, d, 0, 0, 0); calFrom.set(Calendar.MILLISECOND, 0)
                tvFrom.text = dateFormat.format(calFrom.time)
            }, calFrom.get(Calendar.YEAR), calFrom.get(Calendar.MONTH), calFrom.get(Calendar.DAY_OF_MONTH))
                .also { it.datePicker.maxDate = calTo.timeInMillis }.show()
        }

        btnTo.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calTo.set(y, m, d, 23, 59, 59); calTo.set(Calendar.MILLISECOND, 999)
                tvTo.text = dateFormat.format(calTo.time)
            }, calTo.get(Calendar.YEAR), calTo.get(Calendar.MONTH), calTo.get(Calendar.DAY_OF_MONTH))
                .also { it.datePicker.minDate = calFrom.timeInMillis; it.datePicker.maxDate = System.currentTimeMillis() }.show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📄  PDF Есебі")
            .setView(dialogView)
            .setPositiveButton("Жасау") { _, _ ->
                val from = calFrom.timeInMillis; val to = calTo.timeInMillis
                if (from > to) { Toast.makeText(requireContext(), "Бастапқы күн соңғы күннен кейін болмауы керек", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                try {
                    val file = PdfGenerator.generate(requireContext(), child, cards, from, to)
                    PdfGenerator.share(requireContext(), file)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "PDF қате: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        childViewModel.activeChild.value?.let { resultsViewModel.loadForChild(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}