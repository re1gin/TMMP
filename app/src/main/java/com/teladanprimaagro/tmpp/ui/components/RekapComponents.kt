package com.teladanprimaagro.tmpp.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.data.PengirimanData

// Menggunakan warna dari tema
val primaryColor: Color @Composable get() = MaterialTheme.colorScheme.primary
val onPrimaryColor: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
val onSurfaceColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface

@Composable
fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        color = onPrimaryColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun PanenTableRow(
    data: PanenData,
    onDetailClick: (PanenData) -> Unit,
    onEditClick: (PanenData) -> Unit
) {
    Log.d("RekapPanenDebug", "Nama Pemanen: ${data.namaPemanen}, Blok: ${data.blok}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCellText(text = data.tanggalWaktu, weight = 0.2f)
        TableCellText(text = data.namaPemanen, weight = 0.25f)
        TableCellText(text = data.blok, weight = 0.15f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.2f)
        ActionIcons(
            onEditClick = { onEditClick(data) },
            onDetailClick = { onDetailClick(data) }
        )
    }
}

@Composable
fun PengirimanTableRow(
    data: PengirimanData,
    onDetailClick: (PengirimanData) -> Unit,
    onEditClick: (PengirimanData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCellText(text = data.waktuPengiriman, weight = 0.20f)
        TableCellText(text = data.spbNumber, weight = 0.35f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.20f)
        ActionIcons(
            onEditClick = { onEditClick(data) },
            onDetailClick = { onDetailClick(data) }
        )
    }
}

@Composable
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = onSurfaceColor,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun RowScope.ActionIcons(
    onEditClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(0.1f)
            .clickable { onEditClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
    Box(
        modifier = Modifier
            .weight(0.1f)
            .clickable { onDetailClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Detail",
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SummaryBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}