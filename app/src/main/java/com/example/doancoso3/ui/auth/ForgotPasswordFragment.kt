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
class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var tvSuccess: TextView
    private lateinit var tvBackToLogin: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        observeState()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        btnResetPassword = view.findViewById(R.id.btnResetPassword)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        tvEmailError = view.findViewById(R.id.tvEmailError)
        tvSuccess = view.findViewById(R.id.tvSuccess)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        // Set styled text for back to login link
        tvBackToLogin.text = HtmlCompat.fromHtml(
            "Remember your password? <font color='#B9F600'><u>Sign In</u></font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupListeners() {
        btnResetPassword.setOnClickListener {
            clearErrors()
            val email = etEmail.text.toString().trim()
            viewModel.resetPassword(email)
        }

        tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewModel.resetPasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResetPasswordState.Loading -> {
                    btnResetPassword.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    tvSuccess.visibility = View.GONE
                }
                is ResetPasswordState.Success -> {
                    btnResetPassword.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    tvSuccess.visibility = View.VISIBLE
                }
                is ResetPasswordState.Error -> {
                    btnResetPassword.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                    tvSuccess.visibility = View.GONE
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
    }

    private fun clearErrors() {
        tvEmailError.visibility = View.GONE
        tvError.visibility = View.GONE
        tvSuccess.visibility = View.GONE
    }
}
