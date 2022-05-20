package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(
            this,
            MainViewModel.Factory(activity.application)
        )[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        val adapter = AsteroidListAdapter(AsteroidClickListener { asteroid ->
            this.findNavController().navigate(
                MainFragmentDirections.actionShowDetail(asteroid)
            )
        })

        // set the adapter for the RecyclerView
        binding.asteroidRecycler.adapter = adapter

        viewModel.asteroidApiStatus.observe(viewLifecycleOwner) { status ->
            handleAsteroidApiStatus(status)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun handleAsteroidApiStatus(status: AsteroidApiStatus) {
        when (status) {
            AsteroidApiStatus.ERROR -> {
                Snackbar.make(binding.root, R.string.asteroid_api_error_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_refresh_data) {
                        viewModel.refreshData()
                    }
                    .show()
                viewModel.finishedDisplayingApiErrorMessage()
            }
            AsteroidApiStatus.LOADING -> {
                Snackbar.make(binding.root, R.string.asteroid_api_loading_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.show_today_menu -> viewModel.setFilteredView(MainViewModel.FILTERED_VIEW_TODAY)
            R.id.show_all_menu -> viewModel.setFilteredView(MainViewModel.FILTERED_VIEW_ALL)
            else -> viewModel.refreshData()
        }
        return true
    }
}
