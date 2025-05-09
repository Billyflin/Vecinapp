package com.vecinapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.vecinapp.auth.AuthManager
import com.vecinapp.ui.screen.profilecompletion.AgeStep
import com.vecinapp.ui.screen.profilecompletion.NameStep
import com.vecinapp.ui.screen.profilecompletion.PhotoStep
import com.vecinapp.ui.screen.profilecompletion.SeniorModeStep
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@Composable
fun ProfileCompletionScreen(
    onComplete: () -> Unit,
    authManager: AuthManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Current user
    val user by authManager.currentUser.collectAsState(null)

    // Profile data states
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var age by remember { mutableIntStateOf(30) } // Default to 30
    var ageSliderPosition by remember { mutableFloatStateOf(30f) }
    var location by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(user?.photoUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var currentStep by rememberSaveable { mutableIntStateOf(1) } // 1: Senior Mode, 2: Name, 3: Age, 4: Location, 5: Photo
    var isLocationDetecting by rememberSaveable { mutableStateOf(false) }
    var isSenior by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(user) {
        Log.d("ProfileCompletionScreen", "User: $user")
        user?.let { u ->
            if (displayName.isBlank()) displayName = u.displayName.orEmpty()
            if (photoUri == null) photoUri = u.photoUrl
            // aquí puedes copiar más campos si los necesitas
        }
    }
    // Progress tracking
    val totalSteps = 5 // Now including senior mode step
    val progress = currentStep.toFloat() / totalSteps.toFloat()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    // Update age when slider moves
    LaunchedEffect(ageSliderPosition) {
        age = ageSliderPosition.toInt()
    }

    // Function to save profile data
    fun saveProfileData() {
        scope.launch {
            isLoading = true
            try {

                val remoteUri: Uri? = photoUri?.let { localUri ->
                    authManager.uploadProfilePhoto(localUri).getOrThrow()
                }

                /* ---------- GUARDAR PERFIL COMPLETO ---------- */
                val result = authManager.updateUserProfile(
                    userId = user!!.uid,
                    displayName = displayName,
                    photoUri = remoteUri,         // << URL remota
                    age = age,
                    location = location,
                    latitude = null,
                    longitude = null,
                    isProfileComplete = true,
                    isSenior = isSenior
                )

                // Upload profile data including photo if available
                if (result.isSuccess) {
                    onComplete()
                } else {
                    snackbarHostState.showSnackbar("Error: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Function to detect user's location
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun detectUserLocation() {
        scope.launch {
            isLocationDetecting = true

            // Cliente de ubicación
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            // Chequeo de permisos
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Falta permiso, salir temprano
                isLocationDetecting = false
                return@launch
            }

            try {
                // Obtener la última ubicación disponible
                val loc = fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    )
                    .await()
                    ?: throw IllegalStateException("Ubicación nula")

                // Geocodificar a dirección
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    loc.latitude,
                    loc.longitude,
                    1
                )

                // Construir cadena de localidad
                if (addresses != null) {
                    location = addresses
                        .firstOrNull()
                        ?.let { addr ->
                            listOfNotNull(addr.locality, addr.adminArea)
                                .joinToString(", ")
                        }
                        .orEmpty()
                }

            } catch (e: Exception) {
                // Mostrar snackbar de error
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "No se pudo detectar tu ubicación: ${e.message}"
                    )
                }
            } finally {
                isLocationDetecting = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Completa tu perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = "Paso $currentStep de $totalSteps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Main content based on current step
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Senior Mode Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 1,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        SeniorModeStep(
                            isSenior = isSenior,
                            onSeniorChange = { isSenior = it },
                            onNext = { currentStep = 2 }
                        )
                    }

                    // Name Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 2,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(targetOffsetX = { if (currentStep > 2) -it else it })
                    ) {
                        NameStep(
                            displayName = displayName,
                            onNameChange = { displayName = it },
                            onNext = {
                                if (displayName.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Por favor ingresa tu nombre")
                                    }
                                } else {
                                    currentStep = 3
                                }
                            },
                            onBack = { currentStep = 1 }
                        )
                    }

                    // Age Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 3,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(targetOffsetX = { if (currentStep > 3) -it else it })
                    ) {
                        AgeStep(
                            age = age,
                            sliderPosition = ageSliderPosition,
                            onSliderChange = { ageSliderPosition = it },
                            onNext = { currentStep = 4 },
                            onBack = { currentStep = 2 }
                        )
                    }

                    // Location Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 4,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(targetOffsetX = { if (currentStep > 4) -it else it })
                    ) {
                        LocationStep(
                            location = location,
                            onLocationChange = { location = it },
                            onDetectLocation = {
                                if (!isLocationDetecting) {
                                    detectUserLocation()
                                }
                            },
                            isDetecting = isLocationDetecting,
                            onNext = { currentStep = 5 },
                            onBack = { currentStep = 3 }
                        )
                    }

                    // Photo Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 5,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        PhotoStep(
                            photoUri = photoUri,
                            onPickPhoto = { photoPickerLauncher.launch("image/*") },
                            onNext = { saveProfileData() },
                            onBack = { currentStep = 4 }
                        )
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Guardando perfil...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Snackbar host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}


@Composable
fun LocationStep(
    location: String,
    onLocationChange: (String) -> Unit,
    onDetectLocation: () -> Unit,
    isDetecting: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Dónde vives?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Esta información nos ayuda a mostrarte eventos cercanos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("Ciudad o comuna") },
                placeholder = { Text("Ej: Santiago Centro") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = onDetectLocation,
                        enabled = !isDetecting
                    ) {
                        if (isDetecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Detectar ubicación",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Puedes ingresar tu ubicación manualmente o usar la detección automática",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isDetecting
                ) {
                    Text("Continuar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
