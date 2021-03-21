package com.a9ts.a9ts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.a9ts.a9ts.databinding.DetailFragmentBinding

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DetailFragmentBinding.inflate(inflater, container, false)

        val args = DetailFragmentArgs.fromBundle(requireArguments())


        val app = requireNotNull(activity).application

        val viewModelFactory = DetailViewModelFactory(args.appointment, app)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.cancelButtonClicked.observe(viewLifecycleOwner, {
            binding.cancelButton.isEnabled = false
            binding.cancelButton.text = "Cancelation request sent..."

        })

        return binding.root
    }
}