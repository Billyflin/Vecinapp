package com.vecinapp.data.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Convierte una dirección en texto a coordenadas geográficas
     */
    suspend fun getLocationFromAddress(address: String): Result<GeoPoint> =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // En Android SDK 33+, usar el método con callback
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    var result: Result<GeoPoint>? = null

                    geocoder.getFromLocationName(address, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            result = Result.success(GeoPoint(location.latitude, location.longitude))
                        } else {
                            result = Result.failure(Exception("No se encontró la ubicación"))
                        }
                    }

                    // Esperar a que se complete el callback (en una app real usarías suspendCoroutine)
                    while (result == null) {
                        kotlinx.coroutines.delay(100)
                    }

                    return@withContext result!!
                } else {
                    // Para versiones anteriores
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(address, 1)

                    if (addresses != null && addresses.isNotEmpty()) {
                        val location = addresses[0]
                        return@withContext Result.success(
                            GeoPoint(
                                location.latitude,
                                location.longitude
                            )
                        )
                    } else {
                        return@withContext Result.failure(Exception("No se encontró la ubicación"))
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    /**
     * Convierte coordenadas geográficas a una dirección en texto
     */
    suspend fun getAddressFromLocation(location: GeoPoint): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // En Android SDK 33+, usar el método con callback
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    var result: Result<String>? = null

                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = formatAddress(addresses[0])
                            result = Result.success(address)
                        } else {
                            result = Result.failure(Exception("No se encontró la dirección"))
                        }
                    }

                    // Esperar a que se complete el callback (en una app real usarías suspendCoroutine)
                    while (result == null) {
                        kotlinx.coroutines.delay(100)
                    }

                    return@withContext result!!
                } else {
                    // Para versiones anteriores
                    @Suppress("DEPRECATION")
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = formatAddress(addresses[0])
                        return@withContext Result.success(address)
                    } else {
                        return@withContext Result.failure(Exception("No se encontró la dirección"))
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    /**
     * Formatea un objeto Address a una cadena legible
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        if (address.thoroughfare != null) {
            parts.add(address.thoroughfare)
        }

        if (address.subThoroughfare != null) {
            parts.add(address.subThoroughfare)
        }

        if (address.locality != null) {
            parts.add(address.locality)
        }

        if (address.adminArea != null) {
            parts.add(address.adminArea)
        }

        if (address.postalCode != null) {
            parts.add(address.postalCode)
        }

        if (address.countryName != null) {
            parts.add(address.countryName)
        }

        return parts.joinToString(", ")
    }
}