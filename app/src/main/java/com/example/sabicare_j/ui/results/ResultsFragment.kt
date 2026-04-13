package com.example.sabicare_j.ui.results

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sabicare_j.databinding.FragmentResultsBinding
import com.example.sabicare_j.ui.shared.ChildViewModel
import com.example.sabicare_j.utils.PdfGenerator

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val childViewModel: ChildViewModel by activityViewModels()
    private val resultsViewModel: ResultsViewModel by viewModels()
    private lateinit var adapter: ResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.btnExportPdf.setOnClickListener {
            val child = childViewModel.activeChild.value ?: return@setOnClickListener
            val cards = resultsViewModel.resultCards.value ?: return@setOnClickListener

            try {
                val file = PdfGenerator.generate(requireContext(), child, cards)
                PdfGenerator.share(requireContext(), file)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "PDF қате: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        childViewModel.activeChild.value?.let {
            resultsViewModel.loadForChild(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}