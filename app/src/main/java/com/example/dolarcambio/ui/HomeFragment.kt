package com.example.dolarcambio.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dolarcambio.R
import com.example.dolarcambio.data.Transaction
import com.example.dolarcambio.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var recyclerAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        recyclerAdapter = RecyclerAdapter()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSpinner()
        setUpButtons()
        setUpRecycerView()

        val fakeDb = mutableListOf<Transaction>()
        fakeDb.add(Transaction(0,0,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(1,0,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(2,1,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(3,0,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(4,1,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(5,0,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(6,1,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(7,0,"149","15000","15/4/2021"))
        fakeDb.add(Transaction(8,1,"149","15000","15/4/2021"))

        recyclerAdapter.setList(fakeDb)



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setUpSpinner() {
        val spinner = binding.spinnerHome
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_type,
            R.layout.spinner_custom
        )
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
    }

    fun setUpButtons(){

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chooseFragment)
        }

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.convert_calculator -> {
                    findNavController().navigate(R.id.action_homeFragment_to_calculatorFragment)
                    true
                }
                else -> false
            }
        }

    }

    fun setUpRecycerView(){

        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }




}
