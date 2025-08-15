package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.WarningYellow

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Data Berhasil ✓",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        },
        text = {
            Text(
                text = "Berhasil menambahkan data ke-NFC",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen) // Warna hijau kustom
                ) {
                    Text("Ya", color = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF333333),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FailureDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gagal ❌",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            }
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed) // Warna merah kustom
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        },
        containerColor = Color(0xFF333333), // Warna latar belakang dialog
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
fun DuplicateScanDialog(
    onDismissRequest: () -> Unit,
    uniqueNo: String
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Peringatan ⚠️",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarningYellow
                )
            }
        },
        text = {
            Text(
                text = "Tag dengan ID '$uniqueNo' sudah pernah di-scan.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningYellow)
                ) {
                    Text("Tutup", color = Color.Black)
                }
            }
        },
        containerColor = Color(0xFF333333),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SuccessScanDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Scan Berhasil ✓",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        },
        text = {
            Text(
                text = "Tag NFC telah berhasil di-scan.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Selesai", color = Color.Black)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp)
    )
}