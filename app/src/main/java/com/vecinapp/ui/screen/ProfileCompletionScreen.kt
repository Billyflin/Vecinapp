package com.vecinapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.vecinapp.auth.AuthManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@Composable
fun ProfileCompletionScreen(
    onComplete: () -> Unit, authManager: AuthManager, onSeniorChange: suspend (Boolean) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(false) {
       onSeniorChange(false)
    }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Current user
    val user by remember { mutableStateOf(authManager.getCurrentUser()) }

    // Profile data states
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var age by remember { mutableIntStateOf(30) } // Default to 30
    var ageSliderPosition by remember { mutableFloatStateOf(30f) }
    var location by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(user?.photoUrl?.toString()?.toUri()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) } // 1: Name, 2: Age, 3: Location, 4: Photo
    var isLocationDetecting by remember { mutableStateOf(false) }

    // Progress tracking
    val totalSteps = 4 // Now including location step
    val progress = currentStep.toFloat() / totalSteps.toFloat()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    // Update age when slider moves
    LaunchedEffect(ageSliderPosition) {
        age = ageSliderPosition.toInt()
    }



    @RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    fun detectUserLocation() {
        scope.launch {
            isLocationDetecting = true

            // Cliente de ubicación
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            // Chequeo de permisos
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Falta permiso, salir temprano
                isLocationDetecting = false
                return@launch
            }

            try {
                // Obtener la última ubicación disponible
                val loc = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
                    ).await() ?: throw IllegalStateException("Ubicación nula")

                // Geocodificar a dirección
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    loc.latitude, loc.longitude, 1
                )

                // Construir cadena de localidad
                if (addresses != null) {
                    location = addresses.firstOrNull()?.let { addr ->
                        listOfNotNull(addr.locality, addr.adminArea).joinToString(", ")
                    }.orEmpty()
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
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
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
                    // Name Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 1,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            targetOffsetX = { -it })
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
                                    currentStep = 2
                                }
                            })
                    }

                    // Age Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 2,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            targetOffsetX = { if (currentStep > 2) -it else it })
                    ) {
                        AgeStep(
                            age = age,
                            sliderPosition = ageSliderPosition,
                            onSliderChange = { ageSliderPosition = it },
                            onNext = { currentStep = 3 },
                            onBack = { currentStep = 1 })
                    }

                    // Location Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 3,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            targetOffsetX = { if (currentStep > 3) -it else it })
                    ) {
                        LocationStep(
                            location = location,
                            onLocationChange = { location = it },
                            onDetectLocation = @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                                if (!isLocationDetecting) {
                                    detectUserLocation()
                                }
                            },
                            isDetecting = isLocationDetecting,
                            onNext = { currentStep = 4 },
                            onBack = { currentStep = 2 })
                    }

                    // Photo Step
                    this@Column.AnimatedVisibility(
                        visible = currentStep == 4,
                        enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            initialOffsetX = { it }),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            targetOffsetX = { -it })
                    ) {
                        PhotoStep(
                            photoUri = photoUri,
                            onPickPhoto = { photoPickerLauncher.launch("image/*") },
                            onNext = {
                                // Save all profile data
                                saveProfileData(
                                    authManager = authManager,
                                    displayName = displayName,
                                    age = age,
                                    location = location,
                                    photoUri = photoUri,
                                    onLoading = { isLoading = it },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    },
                                    onSuccess = onComplete
                                )
                            },
                            onBack = { currentStep = 3 })
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
                        ), shape = RoundedCornerShape(16.dp)
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
fun NameStep(
    displayName: String, onNameChange: (String) -> Unit, onNext: () -> Unit
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
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Cómo te llamas?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Este nombre será visible para otros usuarios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = onNameChange,
                label = { Text("Nombre completo") },
                placeholder = { Text("Ej: María González") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = displayName.isBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continuar")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun AgeStep(
    age: Int,
    sliderPosition: Float,
    onSliderChange: (Float) -> Unit,
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
                imageVector = Icons.Default.Cake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Cuál es tu edad?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Esta información nos ayuda a personalizar tu experiencia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$age años",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = sliderPosition,
                onValueChange = onSliderChange,
                valueRange = 18f..100f,
                steps = 82,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "18",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onNext, shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
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
                        onClick = onDetectLocation, enabled = !isDetecting
                    ) {
                        if (isDetecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), strokeWidth = 2.dp
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onNext, shape = RoundedCornerShape(12.dp), enabled = !isDetecting
                ) {
                    Text("Continuar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun PhotoStep(
    photoUri: Uri?, onPickPhoto: () -> Unit, onNext: () -> Unit, onBack: () -> Unit
) {
    val context = LocalContext.current

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
            Text(
                text = "Foto de perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Añade una foto para personalizar tu perfil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile photo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 4.dp, brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ), shape = CircleShape
                    )
                    .clickable(onClick = onPickPhoto), contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(photoUri).crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Edit overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Añadir foto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onPickPhoto, modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (photoUri != null) "Cambiar foto" else "Seleccionar foto",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "La foto de perfil es opcional. Puedes añadirla más tarde desde la configuración.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Finalizar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        }
    }
}

private fun saveProfileData(
    authManager: AuthManager,
    displayName: String,
    age: Int,
    location: String,
    photoUri: Uri?,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    onLoading(true)

    val user = authManager.getCurrentUser()
    if (user == null) {
        onLoading(false)
        onError("No se pudo obtener la información del usuario")
        return
    }

    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    scope.launch {
        try {
            // Upload photo if provided
            var photoUrl: Uri? = null
            if (photoUri != null && !photoUri.toString().startsWith("http")) {
                authManager.uploadProfilePhoto(photoUri).onSuccess { url -> photoUrl = url }
                    .onFailure { throw it }
            }

            // Update profile with all data
            authManager.updateUserProfile(
                userId = user.uid,
                displayName = displayName,
                photoUri = photoUrl,
                age = age,
                location = location,
                isComplete = true // Mark profile as complete
            ).onSuccess {
                onLoading(false)
                onSuccess()
            }.onFailure {
                throw it
            }
        } catch (e: Exception) {
            onLoading(false)
            onError("Error al guardar el perfil: ${e.message}")
        }
    }
}