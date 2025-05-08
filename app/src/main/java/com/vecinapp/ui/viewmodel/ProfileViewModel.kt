package com.vecinapp.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.vecinapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado del perfil
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    // Estado de la ubicación
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Initial)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    // Usuario actual
    val currentUser = authRepository.currentUser

    /**
     * Obtener perfil de usuario
     */
    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val profile = authRepository.getUserProfile(userId)
                _profileState.value = ProfileState.UserProfile(profile)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error al cargar perfil: ${e.message}")
            }
        }
    }

    /**
     * Actualizar perfil de usuario
     */
    fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        photoUri: Uri? = null,
        age: Int? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        isComplete: Boolean = false
    ) {
        _profileState.value = ProfileState.Loading

        viewModelScope.launch {
            try {
                // Subir foto si es nueva
                var finalPhotoUri: Uri? = photoUri
                if (photoUri != null && !photoUri.toString().startsWith("http")) {
                    authRepository.uploadProfilePhoto(photoUri)
                        .onSuccess { url -> finalPhotoUri = url }
                        .onFailure { throw it }
                }

                // Actualizar perfil con coordenadas
                authRepository.updateUserProfile(
                    userId = userId,
                    displayName = displayName,
                    photoUri = finalPhotoUri,
                    age = age,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    isComplete = isComplete
                ).onSuccess {
                    _profileState.value = ProfileState.Success
                }.onFailure {
                    throw it
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error al guardar perfil: ${e.message}")
            }
        }
    }

    /**
     * Detectar ubicación actual
     */
    fun detectCurrentLocation(context: Context) {
        _locationState.value = LocationState.Loading

        viewModelScope.launch {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _locationState.value = LocationState.Error("Permisos de ubicación no otorgados.")
                    return@launch
                }

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = withTimeout(10000L) {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                }

                if (location == null) {
                    _locationState.value = LocationState.Error("No se pudo obtener la ubicación. Intenta más tarde.")
                    return@launch
                }

                authRepository.getCityFromLocation(location.latitude, location.longitude)
                    .onSuccess { cityName ->
                        _locationState.value = LocationState.Success(
                            cityName = cityName,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    }
                    .onFailure {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val cityName = addresses?.firstOrNull()?.let {
                                listOfNotNull(it.locality, it.adminArea).joinToString(", ")
                            }.orEmpty()

                            val finalCityName = cityName.ifEmpty {
                                getDefaultCityForRegion(location.latitude, location.longitude)
                            }

                            _locationState.value = LocationState.Success(
                                cityName = finalCityName,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        } catch (e: Exception) {
                            val defaultCity = getDefaultCityForRegion(location.latitude, location.longitude)
                            _locationState.value = LocationState.Success(
                                cityName = defaultCity,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }
                    }
            } catch (e: TimeoutCancellationException) {
                _locationState.value = LocationState.Error("Tiempo de espera agotado. Intenta de nuevo.")
            } catch (e: Exception) {
                _locationState.value = LocationState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Estados posibles para el perfil
    sealed class ProfileState {
        object Initial : ProfileState()
        object Loading : ProfileState()
        object Success : ProfileState()
        data class UserProfile(val profile: AuthRepository.UserProfile) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    // Estados posibles para la ubicación
    sealed class LocationState {
        object Initial : LocationState()
        object Loading : LocationState()
        data class Success(val cityName: String, val latitude: Double, val longitude: Double) : LocationState()
        data class Error(val message: String) : LocationState()
    }

    // Función auxiliar para obtener la ciudad por defecto según coordenadas
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
}