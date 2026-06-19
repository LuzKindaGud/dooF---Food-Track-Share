package com.example.doancoso3.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doancoso3.R
import com.example.doancoso3.ui.components.FreshVitalityBackground

private object AuthRoute {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
}

@Composable
fun AuthComposeScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val loginState by viewModel.loginState.observeAsState()
    val registerState by viewModel.registerState.observeAsState()

    LaunchedEffect(loginState, registerState) {
        if (loginState is AuthState.Success || registerState is AuthState.Success) {
            onNavigateToMain()
        }
    }

    FreshVitalityBackground {
        NavHost(
            navController = navController,
            startDestination = AuthRoute.LOGIN,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AuthRoute.LOGIN) {
                LoginAuthScreen(viewModel, navController)
            }
            composable(AuthRoute.REGISTER) {
                RegisterAuthScreen(viewModel, navController)
            }
            composable(AuthRoute.FORGOT) {
                ForgotPasswordAuthScreen(viewModel, navController)
            }
        }
    }
}


@Composable
private fun LoginAuthScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    var familyId by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val loginState by viewModel.loginState.observeAsState()
    val emailError by viewModel.emailError.observeAsState()
    val passwordError by viewModel.passwordError.observeAsState()

    AuthPageContainer(topPadding = 80.dp) {
        AuthHeader(
            title = "Welcome Home",
            subtitle = "Food Share & Track"
        )

        AuthLabel("FAMILY ID", topPadding = 40.dp)
        AuthInputField(
            value = familyId,
            onValueChange = { familyId = it },
            hint = "e.g. Smith-123",
            iconRes = R.drawable.ic_group
        )

        AuthLabel("EMAIL", topPadding = 20.dp)
        AuthInputField(
            value = email,
            onValueChange = { email = it },
            hint = "Enter your email",
            iconRes = R.drawable.ic_email,
            keyboardType = KeyboardType.Email
        )
        if (!emailError.isNullOrBlank()) {
            FieldError(emailError!!)
        }

        AuthLabel("PASSWORD", topPadding = 20.dp)
        AuthInputField(
            value = password,
            onValueChange = { password = it },
            hint = "Enter your password",
            iconRes = R.drawable.ic_lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        if (!passwordError.isNullOrBlank()) {
            FieldError(passwordError!!)
        }

        Text(
            text = "Forgot Password?",
            color = colorResource(R.color.outline),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp, end = 4.dp)
                .clickable { navController.navigate(AuthRoute.FORGOT) }
        )

        if (loginState is AuthState.Error) {
            FieldError(
                message = (loginState as AuthState.Error).message,
                topPadding = 16.dp,
                centered = true
            )
        }

        PrimaryAuthButton(
            text = "Sign In",
            loading = loginState is AuthState.Loading,
            onClick = { viewModel.login(email.trim(), password) }
        )

        LinkLine(
            prefix = "New to the pantry? ",
            highlighted = "Create a Family ID",
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable { navController.navigate(AuthRoute.REGISTER) }
        )
    }
}

@Composable
private fun RegisterAuthScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var familyId by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val registerState by viewModel.registerState.observeAsState()
    val displayNameError by viewModel.displayNameError.observeAsState()
    val emailError by viewModel.emailError.observeAsState()
    val passwordError by viewModel.passwordError.observeAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.observeAsState()

    AuthPageContainer(topPadding = 60.dp) {
        AuthHeader(
            title = "Join the Pantry",
            subtitle = "Create your account to get started"
        )

        AuthLabel("DISPLAY NAME", topPadding = 32.dp)
        AuthInputField(
            value = displayName,
            onValueChange = { displayName = it },
            hint = "Enter your name",
            iconRes = R.drawable.ic_person
        )
        if (!displayNameError.isNullOrBlank()) {
            FieldError(displayNameError!!)
        }

        AuthLabel("FAMILY ID", topPadding = 16.dp)
        AuthInputField(
            value = familyId,
            onValueChange = { familyId = it },
            hint = "e.g. Smith-123 (optional)",
            iconRes = R.drawable.ic_group
        )

        AuthLabel("EMAIL", topPadding = 16.dp)
        AuthInputField(
            value = email,
            onValueChange = { email = it },
            hint = "Enter your email",
            iconRes = R.drawable.ic_email,
            keyboardType = KeyboardType.Email
        )
        if (!emailError.isNullOrBlank()) {
            FieldError(emailError!!)
        }

        AuthLabel("PASSWORD", topPadding = 16.dp)
        AuthInputField(
            value = password,
            onValueChange = { password = it },
            hint = "Enter your password",
            iconRes = R.drawable.ic_lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        if (!passwordError.isNullOrBlank()) {
            FieldError(passwordError!!)
        }

        AuthLabel("CONFIRM PASSWORD", topPadding = 16.dp)
        AuthInputField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            hint = "Confirm your password",
            iconRes = R.drawable.ic_lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        if (!confirmPasswordError.isNullOrBlank()) {
            FieldError(confirmPasswordError!!)
        }

        if (registerState is AuthState.Error) {
            FieldError(
                message = (registerState as AuthState.Error).message,
                topPadding = 16.dp,
                centered = true
            )
        }

        PrimaryAuthButton(
            text = "Create Account",
            loading = registerState is AuthState.Loading,
            onClick = {
                viewModel.register(
                    email = email.trim(),
                    password = password,
                    confirmPassword = confirmPassword,
                    displayName = displayName.trim()
                )
            }
        )

        LinkLine(
            prefix = "Already have an account? ",
            highlighted = "Sign In",
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable { navController.popBackStack() }
        )
    }
}

@Composable
private fun ForgotPasswordAuthScreen(
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    var email by rememberSaveable { mutableStateOf("") }
    val resetState by viewModel.resetPasswordState.observeAsState()
    val emailError by viewModel.emailError.observeAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    // Show dialog when state becomes Success
    LaunchedEffect(resetState) {
        if (resetState is ResetPasswordState.Success) {
            showResetDialog = true
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            email = email.trim(),
            state = resetState ?: ResetPasswordState.Loading,
            onDismiss = { 
                showResetDialog = false 
                // Optionally navigate back to login
                navController.popBackStack()
            }
        )
    }

    AuthPageContainer(topPadding = 80.dp) {
        AuthHeader(
            title = "Reset Password",
            subtitle = "Enter your email address and we'll send you a link to reset your password.",
            subtitleCentered = true
        )

        AuthLabel("EMAIL", topPadding = 40.dp)
        AuthInputField(
            value = email,
            onValueChange = { email = it },
            hint = "Enter your email",
            iconRes = R.drawable.ic_email,
            keyboardType = KeyboardType.Email
        )
        if (!emailError.isNullOrBlank()) {
            FieldError(emailError!!)
        }

        if (resetState is ResetPasswordState.Error) {
            FieldError(
                message = (resetState as ResetPasswordState.Error).message,
                topPadding = 16.dp,
                centered = true
            )
        }

        PrimaryAuthButton(
            text = "Send Reset Link",
            loading = resetState is ResetPasswordState.Loading,
            onClick = { viewModel.resetPassword(email.trim()) }
        )

        LinkLine(
            prefix = "Remember your password? ",
            highlighted = "Sign In",
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable { navController.popBackStack() }
        )
    }
}

@Composable
fun ResetPasswordDialog(
    email: String,
    state: ResetPasswordState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) { }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A170F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF112216))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (state is ResetPasswordState.Success) Color(0xFFD4FF00).copy(alpha = 0.1f)
                                else Color(0xFFDAB770).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(if (state is ResetPasswordState.Success) R.drawable.ic_leaf else R.drawable.ic_email),
                            contentDescription = null,
                            tint = if (state is ResetPasswordState.Success) Color(0xFFD4FF00) else Color(0xFFDAB770),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (state is ResetPasswordState.Success) "Email Sent!" else "Resetting...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when (state) {
                            is ResetPasswordState.Success -> "Chúng tôi đã gửi liên kết khôi phục đến:\n$email"
                            is ResetPasswordState.Error -> "Lỗi: ${state.message}"
                            else -> "Đang xử lý yêu cầu của bạn..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9E8A),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4FF00),
                            contentColor = Color(0xFF01180A)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = if (state is ResetPasswordState.Success) "GREAT, THANKS!" else "CLOSE",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AuthPageContainer(
    topPadding: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = topPadding, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun AuthHeader(
    title: String,
    subtitle: String,
    subtitleCentered: Boolean = false
) {
    Icon(
        painter = painterResource(R.drawable.ic_leaf),
        contentDescription = null,
        tint = colorResource(R.color.lime_primary),
        modifier = Modifier.size(48.dp)
    )
    Text(
        text = title,
        color = colorResource(R.color.on_surface),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
    Text(
        text = subtitle,
        color = colorResource(R.color.outline),
        fontSize = 14.sp,
        textAlign = if (subtitleCentered) TextAlign.Center else TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (subtitleCentered) 8.dp else 4.dp)
    )
}

@Composable
private fun AuthLabel(text: String, topPadding: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = colorResource(R.color.label_text),
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
    )
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    iconRes: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(top = 6.dp)
            .background(
                color = colorResource(R.color.input_bg),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colorResource(R.color.on_surface_variant),
            modifier = Modifier.size(24.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colorResource(R.color.on_surface),
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            singleLine = true,
            decorationBox = { inner ->
                if (value.isBlank()) {
                    Text(
                        text = hint,
                        color = colorResource(R.color.hint_text),
                        fontSize = 14.sp
                    )
                }
                inner()
            }
        )
    }
}

@Composable
private fun FieldError(
    message: String,
    topPadding: androidx.compose.ui.unit.Dp = 4.dp,
    centered: Boolean = false
) {
    Text(
        text = message,
        color = colorResource(R.color.error),
        fontSize = if (centered) 13.sp else 12.sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
    )
}

@Composable
private fun PrimaryAuthButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.lime_primary),
            contentColor = colorResource(R.color.button_text_dark)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 24.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colorResource(R.color.button_text_dark),
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LinkLine(
    prefix: String,
    highlighted: String,
    modifier: Modifier = Modifier
) {
    val text = buildAnnotatedString {
        append(prefix)
        withStyle(
            style = SpanStyle(
                color = colorResource(R.color.lime_primary),
                fontWeight = FontWeight.Medium
            )
        ) {
            append(highlighted)
        }
    }
    Text(
        text = text,
        color = colorResource(R.color.on_surface),
        fontSize = 14.sp,
        modifier = modifier.padding(8.dp)
    )
}
