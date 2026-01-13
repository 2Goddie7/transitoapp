package com.example.transitoapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AccidentFormScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccidentFormScreen() {
    val context = LocalContext.current

    var tipoAccidente by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var nombreConductor by remember { mutableStateOf("") }
    var cedulaConductor by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var ubicacionGPS by remember { mutableStateOf("") }

    var fotoUri by remember { mutableStateOf<File?>(null) }
    var fotoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Estado para permisos múltiples
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val scrollState = rememberScrollState()

    // Launcher para abrir CameraActivity
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra("fotoUri")?.let { uriString ->
                fotoUri = File(android.net.Uri.parse(uriString).path ?: "")
                fotoBitmap = android.graphics.BitmapFactory.decodeFile(fotoUri?.absolutePath)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Registro de Accidente de Tránsito",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tipo de accidente con opciones
        var expandedTipo by remember { mutableStateOf(false) }
        val tiposAccidente = listOf("Choque", "Colisión", "Atropello")

        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo }
        ) {
            TextField(
                value = tipoAccidente,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de accidente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                tiposAccidente.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            tipoAccidente = tipo
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha del siniestro (DD/MM/AAAA)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula del auto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = nombreConductor,
            onValueChange = { nombreConductor = it },
            label = { Text("Nombre conductor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = cedulaConductor,
            onValueChange = { cedulaConductor = it },
            label = { Text("Cédula conductor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(Modifier.height(16.dp))

        // Mostrar ubicación GPS si está disponible
        if (ubicacionGPS.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "Ubicación GPS: $ubicacionGPS",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Botón para tomar foto
        Button(
            onClick = {
                if (permissionsState.permissions.first { it.permission == Manifest.permission.CAMERA }.status.isGranted) {
                    val intent = android.content.Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tomar Foto")
        }

        Spacer(Modifier.height(16.dp))

        fotoBitmap?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Foto del accidente",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botón guardar
        Button(
            onClick = {
                // Validar campos obligatorios
                if (tipoAccidente.isEmpty() || fecha.isEmpty() || matricula.isEmpty() ||
                    nombreConductor.isEmpty() || cedulaConductor.isEmpty()
                ) {
                    Toast.makeText(context, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Obtener ubicación si permisos concedidos
                if (permissionsState.permissions.any {
                        it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
                    }) {
                    getCurrentLocation(context) { location ->
                        if (location != null) {
                            ubicacionGPS = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                            Toast.makeText(
                                context,
                                "Ubicación obtenida: $ubicacionGPS",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Vibrar 5 segundos
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))

                            // Aquí puedes guardar datos en base de datos
                            Toast.makeText(context, "Accidente registrado correctamente!", Toast.LENGTH_LONG).show()

                            // Limpiar formulario
                            tipoAccidente = ""
                            fecha = ""
                            matricula = ""
                            nombreConductor = ""
                            cedulaConductor = ""
                            observaciones = ""
                            ubicacionGPS = ""
                            fotoUri = null
                            fotoBitmap = null
                        } else {
                            Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                    Toast.makeText(context, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Guardar Accidente")
        }

        Spacer(Modifier.height(16.dp))

        // Información sobre permisos
        if (!permissionsState.allPermissionsGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "⚠️ Algunos permisos no han sido concedidos. La aplicación necesita acceso a la cámara y ubicación para funcionar correctamente.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}