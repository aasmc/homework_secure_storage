package com.otus.securehomework.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.databinding.FragmentHomeBinding
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.logout
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        binding.progressbar.visible(false)

        viewModel.getUser()

        viewModel.user.observe(viewLifecycleOwner, {
            when (it) {
                is Response.Success -> {
                    binding.progressbar.visible(false)
                    updateUI(it.value.user)
                }
                is Response.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Response.Failure -> {
                    handleApiError(it)
                }
            }
        })

        binding.buttonLogout.setOnClickListener {
            logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBiometricsEnabled.collect { enabled ->
                    binding.biometricToggle.isChecked = enabled
                }
            }
        }

        setSwitchListener()
    }

    private fun setSwitchListener() {
        binding.biometricToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShouldUseBiometrics(isChecked)
        }
    }

    private fun updateUI(user: User) {
        with(binding) {
            textViewId.text = user.id.toString()
            textViewName.text = user.name
            textViewEmail.text = user.email
        }
    }
}