package com.app.tourism_app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.app.tourism_app.activities.CreateUserActivity
import com.app.tourism_app.activities.LoginActivity
import com.app.tourism_app.R

class GuestInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_guest_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_login)?.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        view.findViewById<Button>(R.id.btn_create)?.setOnClickListener {
            startActivity(Intent(requireContext(), CreateUserActivity::class.java))
            requireActivity().finish()
        }
    }
}