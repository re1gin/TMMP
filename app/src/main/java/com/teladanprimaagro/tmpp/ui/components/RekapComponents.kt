package com.teladanprimaagro.tmpp.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.data.PanenData // Ensure PanenData is defined in your data package
import com.teladanprimaagro.tmpp.data.PengirimanData // Ensure PengirimanData is defined in your data package
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange


@Composable
fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun TableRow(data: PanenData, onDetailClick: (PanenData) -> Unit) {
    Log.d("RekapPanenDebug", "Nama Pemanen: ${data.namaPemanen}, Blok: ${data.blok}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCellText(text = data.tanggalWaktu, weight = 0.2f)
        TableCellText(text = data.namaPemanen, weight = 0.25f) // Kolom Nama Pemanen
        TableCellText(text = data.blok, weight = 0.15f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.2f)

        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { /* TODO: Aksi edit. Anda bisa melewati data.id ke fungsi edit */ }
            )
        }
        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Detail",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDetailClick(data) }
            )
        }
    }
}

@Composable
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun SummaryBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .background(PrimaryOrange, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun PengirimanTableRow(data: PengirimanData, onDetailClick: (PengirimanData) -> Unit, onEditClick: (PengirimanData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Updated to use the correct fields from PengirimanData
        TableCellText(text = data.waktuPengiriman, weight = 0.20f) // Assuming waktuPengiriman is like tanggalWaktu
        TableCellText(text = data.spbNumber, weight = 0.35f) // Using spbNumber as it's a key identifier
        TableCellText(text = data.totalBuah.toString(), weight = 0.20f)

        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEditClick(data) }
            )
        }
        Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Detail",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDetailClick(data) }
            )
        }
    }
}