package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.Black
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan Berhasil",
                    fontSize = 20.sp,
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
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Tutup", color = Color.Black)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
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
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Failed",
                    tint = DangerRed,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Gagal",
                    fontSize = 20.sp,
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
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
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
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = WarningYellow,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Peringatan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                    Text("Tutup", color = Color.White)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan Berhasil",
                    fontSize = 20.sp,
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
                    Text("Tutup", color = Black)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
        shape = RoundedCornerShape(16.dp)
    )
}