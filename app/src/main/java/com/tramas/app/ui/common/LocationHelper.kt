package com.tramas.app.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tramas.app.data.GeoPoint
import com.tramas.app.data.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geolocalización con la API de Google (Fused Location Provider).
 * Pide el permiso en tiempo de ejecución, obtiene la ubicación actual, la convierte en dirección (Geocoder) y la
 * guarda como última ubicación de la sesión. Si algo falla entrega null.
 * Debe crearse al construir el Fragment.
 */
class LocationHelper(private val fragment: Fragment, private val onResult: (GeoPoint?) -> Unit) {

    private val permission = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) fetch() else onResult(null)
    }

    /** Punto de entrada: si ya hay permiso obtiene la ubicación; si no, lo solicita primero. */
    fun request() {
        val ctx = fragment.requireContext()
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) fetch() else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /** Obtiene la ubicación actual con el Fused Location Provider y la convierte en dirección. */
    @android.annotation.SuppressLint("MissingPermission")
    private fun fetch() {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val point = try {
                val client = LocationServices.getFusedLocationProviderClient(fragment.requireContext())
                val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await() ?: client.lastLocation.await()
                loc?.let { GeoPoint(it.latitude, it.longitude, reverseGeocode(fragment.requireContext(), it.latitude, it.longitude)) }
            } catch (e: Exception) {
                null
            }
            point?.let { Session.location = it }
            onResult(point)
        }
    }

    /** Convierte coordenadas en una dirección legible (vacía si el dispositivo no tiene servicio de geocodificación). */
    private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext ""
        runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale("es", "CO")).getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0).orEmpty()
        }.getOrDefault("")
    }
}

/** Distancia en km entre dos puntos de la Tierra (fórmula de Haversine). Null si falta alguna coordenada. */
fun distanceKm(from: GeoPoint?, lat: Double?, lng: Double?): Double? {
    if (from == null || lat == null || lng == null) return null
    val r = 6371.0
    val dLat = Math.toRadians(lat - from.lat)
    val dLng = Math.toRadians(lng - from.lng)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(from.lat)) * cos(Math.toRadians(lat)) * sin(dLng / 2) * sin(dLng / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/** Formatea una distancia: "3,2 km" o "Sin ubicación". */
fun Double?.kmText(): String = this?.let { "%.1f km".format(it).replace('.', ',') } ?: "Sin ubicación"
