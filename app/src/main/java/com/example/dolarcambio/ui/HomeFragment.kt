package com.example.dolarcambio.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dolarcambio.R
import com.example.dolarcambio.utils.SwipeDelete
import com.example.dolarcambio.data.Transaction
import com.example.dolarcambio.data.local.LocalDataSource
import com.example.dolarcambio.data.local.TransDatabase
import com.example.dolarcambio.data.remote.RemoteDataSource
import com.example.dolarcambio.data.remote.RetrofitInstance
import com.example.dolarcambio.databinding.FragmentHomeBinding
import com.example.dolarcambio.domain.RepoImplement
import com.example.dolarcambio.parseDate
import com.example.dolarcambio.utils.ViewsVisibility
import com.example.dolarcambio.viewmodel.MainViewModel
import com.example.dolarcambio.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class HomeFragment : Fragment(), RecyclerAdapter.OnClickRowListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var recyclerAdapter: RecyclerAdapter
    private var dbAllList = mutableListOf<Transaction>()
    lateinit var dbSellList: MutableList<Transaction>
    lateinit var dbBuyList: MutableList<Transaction>
    lateinit var viewsVisibility: ViewsVisibility
    private val viewModel by activityViewModels<MainViewModel> {
        ViewModelFactory(
            RepoImplement(
                LocalDataSource(TransDatabase.getInstance(requireContext().applicationContext)),
                RemoteDataSource(RetrofitInstance.webService)
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        recyclerAdapter = RecyclerAdapter(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        viewsVisibility = ViewsVisibility(binding)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSpinner()
        setUpButtons()
        setUpRecycerView()
        setUpDbObserver()
        getDolarApi()
        statusToast()

        binding.shimmer.startShimmer()

        ItemTouchHelper(SwipeDelete(recyclerAdapter,requireContext(), viewModel)).attachToRecyclerView(binding.recyclerView)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpSpinner() {
        val spinner = binding.spinnerHome
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_type,
            R.layout.spinner_custom
        )
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> recyclerAdapter.setList(dbAllList)
                    1 -> recyclerAdapter.setList(dbBuyList)
                    2 -> recyclerAdapter.setList(dbSellList)
                }
                recyclerAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setUpButtons(){

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chooseFragment)
        }

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.convert_calculator -> {
                    findNavController().navigate(R.id.action_homeFragment_to_calculatorFragment)
                    true
                }
                R.id.share_app -> {
                    share(binding.root.context)
                    true
                }
                else -> false
            }
        }

    }

    private fun share(context: Context) {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=com.aleaguero.dolarcambio&pli=1"
                )
            }

            context.startActivity(
                Intent.createChooser(
                    sharingIntent,
                    "Comparte la app Dolar Cambio"
                )
            )

    }

    private fun setUpRecycerView(){

        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setUpDbObserver(){
        viewModel.readAllData.observe(viewLifecycleOwner, Observer {dbList ->
            dbAllList = dbList
            dbSellList = dbList.filter { trans:  Transaction -> trans.type == 0 } as MutableList<Transaction>
            dbBuyList = dbList.filter { trans:  Transaction -> trans.type == 1 } as MutableList<Transaction>

            recyclerAdapter.setList(dbList)
            recyclerAdapter.notifyDataSetChanged()


        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDolarApi() {
        viewModel.apply {
//            getDolarOficial()
//            getDolarBlue()
            getDolarSi()

            dolarSi.observe(viewLifecycleOwner, Observer
            { response ->
                if (response.isSuccessful) {
                    val dolarBlue = response.body()?.find { it.casa.nombre == "Dolar Blue" }
                    val dolarOficial = response.body()?.find { it.casa.nombre == "Dolar Oficial" }

                    binding.apply {
                        buyOficialNum.text = ("$" + dolarOficial?.casa?.compra)
                        sellOficialNum.text = ("$" + dolarOficial?.casa?.venta)

                        buyBlueNum.text = ("$" + dolarBlue?.casa?.compra)
                        sellBlueNum.text = ("$" + dolarBlue?.casa?.venta)

                        dateLastUpdate.text =  getTodayAsFormattedString()


                        Handler(Looper.getMainLooper()).postDelayed({
                            shimmer.stopShimmer()
                            shimmer.visibility = View.GONE
                            viewsVisibility.showViews()
                        }, 500)
                    }
                }
            })

//            dolarOficial.observe(viewLifecycleOwner, Observer
//            { response ->
//                if (response.isSuccessful) {
//                    binding.apply {
//                        buyOficialNum.text = ("$" + response.body()?.compra)
//                        sellOficialNum.text = ("$" + response.body()?.venta)
//                        dateLastUpdate.text =  parseDate(response.body()?.fecha)
//
//                    }
//                }
//            })
//
//            dolarBlue.observe(viewLifecycleOwner, Observer { response ->
//                if(response.isSuccessful){
//                    binding.apply {
//                        buyBlueNum.text = ("$" + response.body()?.compra)
//                        sellBlueNum.text = ("$" + response.body()?.venta)
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            shimmer.stopShimmer()
//                            shimmer.visibility = View.GONE
//                            viewsVisibility.showViews()
//                        }, 500)
//
//
//                    }
//                }
//            })
        }
    }

    fun getTodayAsFormattedString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun statusToast(){
        viewModel.status.observe(viewLifecycleOwner, Observer { status ->
            if (!status) {
                Toast.makeText(requireContext(), "Compruebe su conexión a internet", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onClickRow(trans: Transaction) {

        val sellAction = HomeFragmentDirections.actionHomeFragmentToSellFragment(trans)
        val buyAction = HomeFragmentDirections.actionHomeFragmentToBuyFragment(trans)

      when(trans.type){
          0 -> findNavController().navigate(sellAction)
          1 -> findNavController().navigate(buyAction)
      }
    }

}
