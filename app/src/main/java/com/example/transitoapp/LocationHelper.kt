package com.example.transitoapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Obtiene la ubicación actual del dispositivo
 * Esta función maneja correctamente la naturaleza asíncrona de la API de ubicación
 */
@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Primero intentamos obtener la última ubicación conocida (más rápido)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            // Si hay una ubicación reciente, la usamos
            onLocationReceived(location)
        } else {
            // Si no hay ubicación reciente, solicitamos una nueva
            getCurrentLocationFresh(context, fusedLocationClient, onLocationReceived)
        }
    }.addOnFailureListener {
        // Si falla, intentamos obtener una ubicación fresca
        getCurrentLocationFresh(context, fusedLocationClient, onLocationReceived)
    }
}

/**
 * Obtiene una ubicación GPS actualizada (no en caché)
 */
@SuppressLint("MissingPermission")
private fun getCurrentLocationFresh(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        onLocationReceived(location)
    }.addOnFailureListener {
        onLocationReceived(null)
    }
}