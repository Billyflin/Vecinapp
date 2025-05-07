package com.vecinapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de verificación de código OTP para autenticación por teléfono
 */
@Composable
fun OtpVerificationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    verificationId: String,
    forceResendingToken: PhoneAuthProvider.ForceResendingToken?,
    onVerified: () -> Unit,
    onResend: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observar el estado de autenticación
    val authState by viewModel.authState.collectAsState()

    // Efecto para manejar cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Loading -> isLoading = true
            is AuthViewModel.AuthState.Success -> {
                isLoading = false
                onVerified()
            }
            is AuthViewModel.AuthState.Error -> {
                isLoading = false
                onError((authState as AuthViewModel.AuthState.Error).message)
            }
            else -> isLoading = false
        }
    }

    // Auto-focus the OTP field
    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure the UI is ready
        focusRequester.requestFocus()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    "Verificación de código",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                Icons.Default.LockOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Ingresa el código de verificación que enviamos a tu teléfono",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // OTP Input
            BasicTextField(
                value = otpValue,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otpValue = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { index ->
                            val char = otpValue.getOrNull(index)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (char != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char?.toString() ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Hacemos invisible el campo de texto real pero permitimos la entrada
                    Box(modifier = Modifier.size(0.dp)) {
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Button(
                    onClick = {
                        if (otpValue.length == 6) {
                            keyboardController?.hide()
                            viewModel.verifyPhoneNumberWithCode(verificationId, otpValue)
                        } else {
                            onError("Por favor ingresa el código de 6 dígitos completo")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = otpValue.length == 6
                ) {
                    Text(
                        text = "Verificar",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "¿No recibiste el código?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onResend) {
                        Text("Reenviar")
                    }
                }
            }
        }
    }
}
@Composable
fun RegisterPhoneScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onVerified: () -> Unit,
    onCancel: () -> Unit
) {
    // Estados para manejar el flujo de verificación
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observar el estado de verificación del teléfono
    val phoneVerificationState by viewModel.phoneVerificationState.collectAsState()

    // Efecto para manejar cambios en el estado de verificación
    LaunchedEffect(phoneVerificationState) {
        when (phoneVerificationState) {
            is AuthViewModel.PhoneVerificationState.CodeSent -> {
                val state = phoneVerificationState as AuthViewModel.PhoneVerificationState.CodeSent
                verificationId = state.verificationId
                resendToken = state.token
            }
            is AuthViewModel.PhoneVerificationState.Error -> {
                errorMessage = (phoneVerificationState as AuthViewModel.PhoneVerificationState.Error).message
            }
            else -> {}
        }
    }

    // Observar el estado de autenticación
    val authState by viewModel.authState.collectAsState()

    // Efecto para manejar cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                onVerified()
            }
            is AuthViewModel.AuthState.Error -> {
                errorMessage = (authState as AuthViewModel.AuthState.Error).message
            }
            else -> {}
        }
    }

    // Mostrar la pantalla correspondiente según el estado
    if (verificationId == null) {
        RegisterScreenMobile(
            viewModel = viewModel,
            forceResendingToken = resendToken,
            onVerificationSent = { id, token ->
                verificationId = id
                resendToken = token
            },
            onError = { error ->
                errorMessage = error
            },
            onCancel = onCancel
        )
    } else {
        OtpVerificationScreen(
            viewModel = viewModel,
            verificationId = verificationId!!,
            forceResendingToken = resendToken,
            onVerified = onVerified,
            onResend = {
                verificationId = null
            },
            onError = { error ->
                errorMessage = error
            },
            onCancel = onCancel
        )
    }

    // Mostrar errores si los hay
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostrar un snackbar o toast con el error
            // ...

            // Limpiar el error después de mostrarlo
            delay(3000)
            errorMessage = null
        }
    }
}
@Composable
fun RegisterScreenMobile(
    viewModel: AuthViewModel = hiltViewModel(),
    forceResendingToken: PhoneAuthProvider.ForceResendingToken?,
    onVerificationSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val activity = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observar el estado de verificación del teléfono
    val phoneVerificationState by viewModel.phoneVerificationState.collectAsState()

    // Efecto para manejar cambios en el estado de verificación
    LaunchedEffect(phoneVerificationState) {
        when (phoneVerificationState) {
            is AuthViewModel.PhoneVerificationState.Loading -> isLoading = true
            is AuthViewModel.PhoneVerificationState.CodeSent -> {
                isLoading = false
                val state = phoneVerificationState as AuthViewModel.PhoneVerificationState.CodeSent
                onVerificationSent(state.verificationId, state.token)
            }
            is AuthViewModel.PhoneVerificationState.Error -> {
                isLoading = false
                onError((phoneVerificationState as AuthViewModel.PhoneVerificationState.Error).message)
            }
            else -> isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    "Ingresar con teléfono",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Ingresa tu número de teléfono para recibir un código de verificación",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Número de teléfono") },
                placeholder = { Text("+56 9 1234 5678") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            keyboardController?.hide()

                            if (forceResendingToken != null) {
                                viewModel.resendVerificationCode(
                                    phoneNumber = phoneNumber,
                                    token = forceResendingToken,
                                    activity = activity
                                )
                            } else {
                                viewModel.startPhoneVerification(
                                    phoneNumber = phoneNumber,
                                    activity = activity
                                )
                            }
                        } else {
                            onError("Por favor ingresa un número de teléfono válido")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (forceResendingToken != null) "Reenviar código" else "Enviar código",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
            }
        }
    }
}