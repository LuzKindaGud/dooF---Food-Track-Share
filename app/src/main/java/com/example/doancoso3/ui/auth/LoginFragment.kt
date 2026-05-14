package com.example.doancoso3.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.doancoso3.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var etFamilyId: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvGoToRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        observeState()
    }

    private fun initViews(view: View) {
        etFamilyId = view.findViewById(R.id.etFamilyId)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        tvEmailError = view.findViewById(R.id.tvEmailError)
        tvPasswordError = view.findViewById(R.id.tvPasswordError)
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister)
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword)

        // Set styled text for register link
        tvGoToRegister.text = HtmlCompat.fromHtml(
            "New to the pantry? <font color='#B9F600'><u>Create a Family ID</u></font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            clearErrors()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            viewModel.login(email, password)
        }

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }
    }

    private fun observeState() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    btnLogin.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                }
                is AuthState.Success -> {
                    (requireActivity() as AuthActivity).navigateToMain()
                }
                is AuthState.Error -> {
                    btnLogin.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.emailError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                tvEmailError.text = error
                tvEmailError.visibility = View.VISIBLE
            } else {
                tvEmailError.visibility = View.GONE
            }
        }

        viewModel.passwordError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                tvPasswordError.text = error
                tvPasswordError.visibility = View.VISIBLE
            } else {
                tvPasswordError.visibility = View.GONE
            }
        }
    }

    private fun clearErrors() {
        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE
        tvError.visibility = View.GONE
    }
}
