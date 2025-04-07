package com.example.qrscannerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.BasicTextField
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qrscannerapp.ui.theme.QRScannerAppTheme
import com.journeyapps.barcodescanner.CaptureActivity
import java.io.OutputStream
import java.net.Socket
import android.content.pm.ActivityInfo
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var ipAddress by mutableStateOf("192.168.0.0")

    private val qrScannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanResult = result.data?.getStringExtra("SCAN_RESULT")
                scanResult?.let {
                    Log.d("QR", "Zeskanowano: $it")
                    sendToPC(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        ipAddress = sharedPreferences.getString("saved_ip", "192.168.0.0") ?: "192.168.0.0"

        setContent {
            QRScannerScreen(
                onScanClick = { scanQRCode() },
                initialIp = ipAddress,
                onIpSave = { newIp ->
                    ipAddress = newIp
                    saveIpAddress(newIp)
                }
            )
        }
    }

    private fun saveIpAddress(ip: String) {
        sharedPreferences.edit() { putString("saved_ip", ip) }
    }

    private fun scanQRCode() {
        val intent = Intent(this, CaptureActivity::class.java)
        intent.putExtra("SCAN_ORIENTATION", "PORTRAIT")
        intent.putExtra("SCAN_PROMPT", "")
        qrScannerLauncher.launch(intent)
    }

    private fun sendToPC(qrCode: String) {
        Thread {
            try {
                val socket = Socket(ipAddress, 5000)
                val output: OutputStream = socket.getOutputStream()
                output.write(qrCode.toByteArray())
                output.flush()
                output.close()
                socket.close()
            } catch (e: Exception) {
                Log.e("Error", e.toString())
            }
        }.start()
    }
}

@Composable
fun QRScannerScreen(
    onScanClick: () -> Unit,
    initialIp: String,
    onIpSave: (String) -> Unit
) {
    val qrResult by remember { mutableStateOf("Kliknij przycisk, aby zeskanować kod QR") }
    var ipPart1 by remember { mutableStateOf(initialIp.split(".")[0]) }
    var ipPart2 by remember { mutableStateOf(initialIp.split(".")[1]) }
    var ipPart3 by remember { mutableStateOf(initialIp.split(".")[2]) }
    var ipPart4 by remember { mutableStateOf(initialIp.split(".")[3]) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Adres IP komputera:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            BasicTextField(
                value = ipPart1,
                onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() } && it.toIntOrNull()?.let { it <= 255 } != null) ipPart1 = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
            Text(".")
            BasicTextField(
                value = ipPart2,
                onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() } && it.toIntOrNull()?.let { it <= 255 } != null) ipPart2 = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
            Text(".")
            BasicTextField(
                value = ipPart3,
                onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() } && it.toIntOrNull()?.let { it <= 255 } != null) ipPart3 = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
            Text(".")
            BasicTextField(
                value = ipPart4,
                onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() } && it.toIntOrNull()?.let { it <= 255 } != null) ipPart4 = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val newIp = "$ipPart1.$ipPart2.$ipPart3.$ipPart4"
                onIpSave(newIp)
            }) {
                Text("Zapisz")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = qrResult, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onScanClick) {
            Text("Skanuj kod QR")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QRScannerPreview() {
    QRScannerAppTheme {
        QRScannerScreen(
            onScanClick = {},
            initialIp = "192.168.0.0",
            onIpSave = {}
        )
    }
}