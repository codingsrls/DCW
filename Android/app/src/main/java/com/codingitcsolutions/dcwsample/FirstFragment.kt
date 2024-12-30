package com.codingitcsolutions.dcwsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codingitcsolutions.dcwsample.databinding.FragmentFirstBinding
import com.google.gson.Gson

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var DCW:DCWService
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        //TODO da cancellare
        binding.CF.setText("MNFGBR74R30F735I")
        binding.Password.setText("@Coding2024")
        binding.PIN.setText("5047865807")
        binding.PIVA.setText("03601370780")
        binding.apikey.setText("245c85cd35e3a9b4f773a52c71972097")

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            DCW= DCWService(
                binding.CF.text.toString(),
                binding.Password.text.toString(),
                binding.PIN.text.toString(),
                binding.PIVA.text.toString(),
                "incaricato",
                binding.apikey.text.toString()
                )

            DCW.login { response-> activity?.runOnUiThread{

                binding.resultDCW.setText(Gson().toJson(response))

            } }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}