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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qrscannerapp.ui.theme.QRScannerAppTheme
import com.journeyapps.barcodescanner.CaptureActivity
import java.io.OutputStream
import java.net.Socket
import android.content.pm.ActivityInfo
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
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

    private var ipAddress by mutableStateOf("192.168.177.43") // Domyślne IP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            QRScannerScreen(
                onScanClick = { scanQRCode() },
                onIpChange = { ipAddress = it },
                ipAddress = ipAddress
            )
        }
    }

    private fun scanQRCode() {
        val intent = Intent(this, CaptureActivity::class.java)
        intent.putExtra("SCAN_ORIENTATION", "PORTRAIT")  // Wymuszenie pionowej orientacji
        intent.putExtra("SCAN_PROMPT", "")  // Ustawienie pustego tekstu, aby wyłączyć komunikat
        qrScannerLauncher.launch(intent)
    }

    private fun sendToPC(qrCode: String) {
        Thread {
            try {
                val socket = Socket(ipAddress, 5000) // Używa dynamicznie zmienionego IP
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
fun QRScannerScreen(onScanClick: () -> Unit, onIpChange: (String) -> Unit, ipAddress: String) {
    val qrResult by remember { mutableStateOf("Kliknij przycisk, aby zeskanować kod QR") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Adres IP komputera:")
        BasicTextField(
            value = ipAddress,
            onValueChange = { onIpChange(it) },
            modifier = Modifier.padding(8.dp)
        )
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
            onIpChange = {},
            ipAddress = "192.168.177.43"
        )
    }
}
