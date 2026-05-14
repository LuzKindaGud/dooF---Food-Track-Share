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
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var etDisplayName: EditText
    private lateinit var etFamilyId: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvError: TextView
    private lateinit var tvDisplayNameError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvConfirmPasswordError: TextView
    private lateinit var tvGoToLogin: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        observeState()
    }

    private fun initViews(view: View) {
        etDisplayName = view.findViewById(R.id.etDisplayName)
        etFamilyId = view.findViewById(R.id.etFamilyId)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        tvDisplayNameError = view.findViewById(R.id.tvDisplayNameError)
        tvEmailError = view.findViewById(R.id.tvEmailError)
        tvPasswordError = view.findViewById(R.id.tvPasswordError)
        tvConfirmPasswordError = view.findViewById(R.id.tvConfirmPasswordError)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)

        // Set styled text for login link
        tvGoToLogin.text = HtmlCompat.fromHtml(
            "Already have an account? <font color='#B9F600'><u>Sign In</u></font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            clearErrors()
            val displayName = etDisplayName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            viewModel.register(email, password, confirmPassword, displayName)
        }

        tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun observeState() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    btnRegister.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                }
                is AuthState.Success -> {
                    (requireActivity() as AuthActivity).navigateToMain()
                }
                is AuthState.Error -> {
                    btnRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.displayNameError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                tvDisplayNameError.text = error
                tvDisplayNameError.visibility = View.VISIBLE
            } else {
                tvDisplayNameError.visibility = View.GONE
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

        viewModel.confirmPasswordError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                tvConfirmPasswordError.text = error
                tvConfirmPasswordError.visibility = View.VISIBLE
            } else {
                tvConfirmPasswordError.visibility = View.GONE
            }
        }
    }

    private fun clearErrors() {
        tvDisplayNameError.visibility = View.GONE
        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE
        tvConfirmPasswordError.visibility = View.GONE
        tvError.visibility = View.GONE
    }
}
