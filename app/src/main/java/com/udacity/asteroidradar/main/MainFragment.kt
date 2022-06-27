package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var asteroidAdapter: AsteroidAdapter
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
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        asteroidAdapter = AsteroidAdapter(AsteroidItemListListener { asteroid ->
            findNavController().navigate(
                MainFragmentDirections
                    .actionShowDetail(asteroid)
            )
        })

        val manager = LinearLayoutManager(activity)
        with(binding.asteroidRecycler) {
            adapter = asteroidAdapter
            layoutManager = manager
        }

        setHasOptionsMenu(true)

        viewModel.asteroids.observe(viewLifecycleOwner) { asteroids ->
            if (asteroids.isNotEmpty()) {
                asteroidAdapter.submitList(asteroids)

                binding.statusLoadingWheel.visibility = View.GONE
                binding.statusLoadingError.visibility = View.GONE
                viewModel.fetchedFromDatabase = true
            }
        }

        viewModel.downloadSuccess.observe(viewLifecycleOwner) { wasSuccess ->
            if (wasSuccess || viewModel.fetchedFromDatabase) {
                binding.statusLoadingError.visibility = View.GONE
            } else {
                binding.statusLoadingError.visibility = View.VISIBLE
                binding.statusLoadingWheel.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_today_asteroids_menu -> {
                viewModel.todayAsteroids.observe(viewLifecycleOwner) { asteroids ->
                    if (asteroids.isNotEmpty()) {
                        asteroidAdapter.submitList(asteroids)
                    }
                }
            }
            R.id.show_saved_asteroids_menu -> {
                viewModel.asteroids.observe(viewLifecycleOwner) { asteroids ->
                    if (asteroids.isNotEmpty()) {
                        asteroidAdapter.submitList(asteroids)
                    }
                }
            }
            R.id.show_week_asteroids_menu -> {
                viewModel.weekAsteroids.observe(viewLifecycleOwner) { asteroids ->
                    if (asteroids.isNotEmpty()) {
                        asteroidAdapter.submitList(asteroids)
                    }
                }
            }
        }
        return true
    }
}
