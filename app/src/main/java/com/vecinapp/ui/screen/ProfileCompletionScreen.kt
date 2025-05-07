package com.vecinapp.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Pantalla de completado de perfil de usuario
 * Permite al usuario configurar sus preferencias y datos personales
 */
@Composable
fun ProfileCompletionScreen(
    onComplete: () -> Unit,
    authManager: AuthManager,
    onSeniorChange: suspend (Boolean) -> Unit,
    onFirstTimeChange: suspend (Boolean) -> Unit
) {
    // Constantes
    val TOTAL_STEPS = 5
    val DEFAULT_AGE = 30
    val LOCATION_TIMEOUT = 10000L // 10 segundos
    val LOCATION_DETECTION_TIMEOUT = 15000L // 15 segundos

    // Estado y contexto
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val user = authManager.getCurrentUser()

    // Estados del perfil
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var age by remember { mutableIntStateOf(DEFAULT_AGE) }
    var ageSliderPosition by remember { mutableFloatStateOf(DEFAULT_AGE.toFloat()) }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var photoUri by remember { mutableStateOf<Uri?>(user?.photoUrl?.toString()?.toUri()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) }
    var isLocationDetecting by remember { mutableStateOf(false) }
    var isSenior by rememberSaveable { mutableStateOf(false) }

    // Cálculo de progreso
    val progress = currentStep.toFloat() / TOTAL_STEPS.toFloat()

    // Launcher para seleccionar foto
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    // Actualizar edad cuando se mueve el slider
    LaunchedEffect(ageSliderPosition) {
        age = ageSliderPosition.toInt()
    }

    // Cargar datos del perfil si están disponibles
    LaunchedEffect(user) {
        user?.let { currentUser ->
            try {
                val profile = authManager.getUserProfile(currentUser.uid)
                profile.location?.let { location = it }
                profile.latitude?.let { latitude = it }
                profile.longitude?.let { longitude = it }
                profile.age?.let {
                    age = it
                    ageSliderPosition = it.toFloat()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar perfil: ${e.message}")
            }
        }
    }

    // Función para guardar los datos del perfil
    fun saveProfileData() {
        val currentUser = user ?: run {
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo obtener la información del usuario")
            }
            return
        }

        isLoading = true

        scope.launch {
            try {
                // Subir foto si es nueva
                var finalPhotoUri: Uri? = photoUri
                if (photoUri != null && !photoUri.toString().startsWith("http")) {
                    authManager.uploadProfilePhoto(photoUri!!)
                        .onSuccess { url -> finalPhotoUri = url }
                        .onFailure { throw it }
                }

                // Actualizar perfil con coordenadas
                authManager.updateUserProfile(
                    userId = currentUser.uid,
                    displayName = displayName,
                    photoUri = finalPhotoUri,
                    age = age,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    isComplete = true
                ).onSuccess {
                    isLoading = false
                    onSeniorChange(isSenior)
                    onFirstTimeChange(false)
                    onComplete()
                }.onFailure {
                    throw it
                }
            } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("Error al guardar perfil: ${e.message}")
            }
        }
    }

    // Función para navegar al siguiente paso con validación
    fun navigateToNextStep() {
        when (currentStep) {
            2 -> {
                if (displayName.isBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Por favor ingresa tu nombre")
                    }
                    return
                }
            }
            5 -> {
                saveProfileData()
                return
            }
        }
        currentStep++
    }

    // Función para navegar al paso anterior
    fun navigateToPreviousStep() {
        if (currentStep > 1) currentStep--
    }

    // UI principal
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
                // Encabezado
                ProfileHeader(
                    currentStep = currentStep,
                    totalSteps = TOTAL_STEPS,
                    progress = progress
                )

                // Contenido principal basado en el paso actual
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Paso 1: Modo Senior
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
                            onNext = { navigateToNextStep() }
                        )
                    }

                    // Paso 2: Nombre
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
                            onNext = { navigateToNextStep() },
                            onBack = { navigateToPreviousStep() }
                        )
                    }

                    // Paso 3: Edad
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
                            onNext = { navigateToNextStep() },
                            onBack = { navigateToPreviousStep() }
                        )
                    }

                    // Paso 4: Ubicación
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
                            latitude = latitude,
                            longitude = longitude,
                            onCoordinatesChange = { lat, lng ->
                                latitude = lat
                                longitude = lng
                            },
                            onDetectLocation = {
                                isLocationDetecting = true
                                scope.launch {
                                    try {
                                        withTimeout(LOCATION_DETECTION_TIMEOUT) {
                                            // Este timeout es solo para asegurar que isLocationDetecting se restablezca
                                        }
                                    } catch (e: TimeoutCancellationException) {
                                        // Ignorar, solo queremos asegurar que isLocationDetecting se restablezca
                                    } finally {
                                        isLocationDetecting = false
                                    }
                                }
                            },
                            isDetecting = isLocationDetecting,
                            onNext = { navigateToNextStep() },
                            onBack = { navigateToPreviousStep() },
                            authManager = authManager,
                            locationTimeout = LOCATION_TIMEOUT
                        )
                    }

                    // Paso 5: Foto
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
                            onNext = { navigateToNextStep() },
                            onBack = { navigateToPreviousStep() }
                        )
                    }
                }
            }

            // Overlay de carga
            if (isLoading) {
                LoadingOverlay(message = "Guardando perfil...")
            }

            // Host de Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Componente para el encabezado de la pantalla de perfil
 */
@Composable
private fun ProfileHeader(
    currentStep: Int,
    totalSteps: Int,
    progress: Float
) {
    Text(
        text = "Completa tu perfil",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 16.dp)
    )

    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Text(
        text = "Paso $currentStep de $totalSteps",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

/**
 * Overlay de carga con mensaje personalizable
 */
@Composable
private fun LoadingOverlay(message: String) {
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
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Paso 1: Selección de modo Senior
 */
@Composable
fun SeniorModeStep(
    isSenior: Boolean,
    onSeniorChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    StepCard {
        Text(
            text = "¿Eres un usuario senior?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Selecciona el modo que mejor se adapte a ti",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas en modo horizontal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estándar
            Column(modifier = Modifier.weight(1f)) {
                ModeCard(
                    title = "Estándar",
                    icon = Icons.Default.Person,
                    selected = !isSenior,
                    onClick = { onSeniorChange(false) }
                )
            }

            // Senior
            Column(modifier = Modifier.weight(1f)) {
                ModeCard(
                    title = "Senior",
                    icon = Icons.Default.Elderly,
                    selected = isSenior,
                    onClick = { onSeniorChange(true) }
                )
            }
        }

        // Características del modo senior
        Spacer(modifier = Modifier.height(24.dp))

        if (isSenior) {
            SeniorFeaturesCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Diversity3,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continuar",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Tarjeta de características del modo senior
 */
@Composable
private fun SeniorFeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "El modo Senior ofrece:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            SeniorFeatureItem(
                icon = Icons.Outlined.TextFields,
                text = "Textos más grandes y legibles"
            )

            SeniorFeatureItem(
                icon = Icons.Outlined.TouchApp,
                text = "Botones más amplios y fáciles de tocar"
            )

            SeniorFeatureItem(
                icon = Icons.Outlined.AccessibilityNew,
                text = "Navegación simplificada"
            )
        }
    }
}

/**
 * Tarjeta de modo (Estándar o Senior)
 */
@Composable
fun ModeCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        border = if (!selected) BorderStroke(
            width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Indicador de selección
            if (selected) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Seleccionado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * Elemento de característica del modo senior
 */
@Composable
fun SeniorFeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

/**
 * Paso 2: Ingreso de nombre
 */
@Composable
fun NameStep(
    displayName: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    StepCard {
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

        NavigationButtons(onBack = onBack, onNext = onNext)
    }
}

/**
 * Paso 3: Selección de edad
 */
@Composable
fun AgeStep(
    age: Int,
    sliderPosition: Float,
    onSliderChange: (Float) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    StepCard {
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

        NavigationButtons(onBack = onBack, onNext = onNext)
    }
}

@Composable
fun LocationStep(
    location: String,
    onLocationChange: (String) -> Unit,
    latitude: Double,
    longitude: Double,
    onCoordinatesChange: (Double, Double) -> Unit,
    onDetectLocation: () -> Unit,
    isDetecting: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit,
    authManager: AuthManager,
    locationTimeout: Long = 10000L
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var localIsDetecting by remember { mutableStateOf(false) }
    var showCitySelector by remember { mutableStateOf(false) }

    val popularCities = remember {
        listOf(
            "Santiago", "Temuco", "Concepción", "Valparaíso", "Antofagasta",
            "La Serena", "Puerto Montt", "Arica", "Iquique", "Rancagua",
            "Talca", "Chillán", "Calama", "Osorno", "Valdivia"
        )
    }

    LaunchedEffect(isDetecting) {
        if (!isDetecting) localIsDetecting = false
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            detectCurrentLocation(
                context = context,
                scope = scope,
                locationTimeout = locationTimeout,
                onCoordinatesChange = onCoordinatesChange,
                onLocationChange = onLocationChange,
                authManager = authManager,
                onError = { errorMessage = it },
                onStart = { localIsDetecting = true; errorMessage = null; onDetectLocation() },
                onFinish = { localIsDetecting = false }
            )
        } else {
            errorMessage = "Se requieren permisos de ubicación para detectar tu ciudad automáticamente"
            localIsDetecting = false
        }
    }

    StepCard {
        LocationHeader()
        LocationInput(
            location = location,
            onLocationChange = onLocationChange,
            showCitySelector = showCitySelector,
            onShowSelectorToggle = { showCitySelector = !showCitySelector },
            onDetectLocationClick = {
                if (!localIsDetecting && !isDetecting) {
                    if (!hasLocationPermission(context)) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        detectCurrentLocation(
                            context = context,
                            scope = scope,
                            locationTimeout = locationTimeout,
                            onCoordinatesChange = onCoordinatesChange,
                            onLocationChange = onLocationChange,
                            authManager = authManager,
                            onError = { errorMessage = it },
                            onStart = { localIsDetecting = true; errorMessage = null; onDetectLocation() },
                            onFinish = { localIsDetecting = false }
                        )
                    }
                }
            },
            isDetecting = isDetecting || localIsDetecting
        )

        if (showCitySelector) {
            CitySelectorCard(
                cities = popularCities,
                onCitySelected = {
                    onLocationChange(it)
                    showCitySelector = false
                }
            )
        }

        if (latitude != 0.0 && longitude != 0.0) {
            Text(
                text = "Coordenadas: %.6f, %.6f".format(latitude, longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = "Puedes ingresar tu ubicación manualmente, seleccionar de la lista o usar la detección automática",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (it.contains("permisos", true)) {
                TextButton(onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) { Text("Solicitar permisos") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NavigationButtons(
            onBack = onBack,
            onNext = onNext,
            nextEnabled = !localIsDetecting && !isDetecting && location.isNotBlank()
        )
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun detectCurrentLocation(
    context: Context,
    scope: CoroutineScope,
    locationTimeout: Long,
    onCoordinatesChange: (Double, Double) -> Unit,
    onLocationChange: (String) -> Unit,
    authManager: AuthManager,
    onError: (String) -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit
) {
    onStart()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    scope.launch {
        try {
            withTimeout(locationTimeout) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    onError("Permisos de ubicación no otorgados.")
                    return@withTimeout
                }

                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()

                if (location == null) {
                    onError("No se pudo obtener la ubicación. Intenta más tarde.")
                    return@withTimeout
                }

                onCoordinatesChange(location.latitude, location.longitude)

                authManager.getCityFromLocation(location.latitude, location.longitude)
                    .onSuccess { onLocationChange(it) }
                    .onFailure {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val cityName = addresses?.firstOrNull()?.let {
                                listOfNotNull(it.locality, it.adminArea).joinToString(", ")
                            }.orEmpty()

                            onLocationChange(
                                cityName.ifEmpty { getDefaultCityForRegion(location.latitude, location.longitude) }
                            )
                        } catch (e: Exception) {
                            onLocationChange(getDefaultCityForRegion(location.latitude, location.longitude))
                            onError("No se pudo obtener el nombre de la ciudad")
                        }
                    }
            }
        } catch (e: TimeoutCancellationException) {
            onError("Tiempo de espera agotado. Intenta de nuevo.")
        } catch (e: Exception) {
            onError("Error inesperado: ${e.message}")
        } finally {
            onFinish()
        }
    }
}


private fun getDefaultCityForRegion(lat: Double, lng: Double): String {
    val cities = listOf(
        Triple("Santiago", -33.4489, -70.6693),
        Triple("Temuco", -38.7359, -72.5904),
        Triple("Concepción", -36.8201, -73.0440),
        Triple("Valparaíso", -33.0472, -71.6127),
        Triple("Antofagasta", -23.6509, -70.3975),
        Triple("La Serena", -29.9027, -71.2525),
        Triple("Puerto Montt", -41.4693, -72.9424),
        Triple("Arica", -18.4783, -70.3126),
        Triple("Iquique", -20.2208, -70.1431),
        Triple("Rancagua", -34.1708, -70.7444),
        Triple("Talca", -35.4264, -71.6553),
        Triple("Chillán", -36.6064, -72.1034),
        Triple("Calama", -22.4524, -68.9204),
        Triple("Osorno", -40.5714, -73.1392),
        Triple("Valdivia", -39.8142, -73.2459)
    )

    return cities.minByOrNull { (_, cityLat, cityLng) ->
        sqrt((lat - cityLat).pow(2) + (lng - cityLng).pow(2))
    }?.first ?: "Temuco"
}


@Composable
private fun LocationHeader() {
    Icon(
        imageVector = Icons.Default.LocationCity,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "¿En qué ciudad vives?",
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
}

@Composable
private fun LocationInput(
    location: String,
    onLocationChange: (String) -> Unit,
    showCitySelector: Boolean,
    onShowSelectorToggle: () -> Unit,
    onDetectLocationClick: () -> Unit,
    isDetecting: Boolean
) {
    OutlinedTextField(
        value = location,
        onValueChange = onLocationChange,
        label = { Text("Ciudad") },
        placeholder = { Text("Ej: Temuco, Santiago, etc.") },
        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
        trailingIcon = {
            Row {
                IconButton(onClick = onShowSelectorToggle, enabled = !isDetecting) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar ciudad")
                }
                IconButton(onClick = onDetectLocationClick, enabled = !isDetecting) {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = "Detectar ubicación")
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Tarjeta de selector de ciudades
 */
@Composable
private fun CitySelectorCard(
    cities: List<String>,
    onCitySelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Ciudades populares",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            cities.forEachIndexed { index, city ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCitySelected(city) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (index < cities.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

/**
 * Paso 5: Selección de foto de perfil
 */
@Composable
fun PhotoStep(
    photoUri: Uri?,
    onPickPhoto: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    StepCard {
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

        // Foto de perfil
        ProfilePhotoSelector(photoUri = photoUri, onPickPhoto = onPickPhoto)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onPickPhoto,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = if (photoUri != null) "Cambiar foto" else "Seleccionar foto",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "La foto de perfil es opcional. Puedes añadirla más tarde desde la configuración.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
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

/**
 * Selector de foto de perfil
 */
@Composable
private fun ProfilePhotoSelector(
    photoUri: Uri?,
    onPickPhoto: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onPickPhoto),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay de edición
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
}

/**
 * Componente de tarjeta para cada paso
 */
@Composable
private fun StepCard(
    content: @Composable () -> Unit
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
            content()
        }
    }
}

/**
 * Botones de navegación entre pasos
 */
@Composable
private fun NavigationButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Atrás",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(12.dp),
            enabled = nextEnabled
        ) {
            Text("Continuar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}