package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PanenTableRow(
    data: PanenData,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: (Int) -> Unit,
    onLongPress: (PanenData) -> Unit,
    onDetailClick: (PanenData) -> Unit,
    onEditClick: (PanenData) -> Unit
) {
    val displayTime = if (data.tanggalWaktu.length >= 5) {
        data.tanggalWaktu.substring(data.tanggalWaktu.length - 5)
    } else {
        data.tanggalWaktu }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection(data.id)
                    } else {
                        onDetailClick(data)
                    }
                },
                onLongClick = { onLongPress(data) }
            )
            .background(Grey.copy(0.5f))
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection(data.id) },
            )
            Spacer(modifier = Modifier.size(10.dp))
        } else {
            Spacer(modifier = Modifier.size(10.dp))
        }

        TableCellText(text = displayTime, weight = 0.10f)
        TableCellText(text = data.namaPemanen, weight = 0.20f)
        TableCellText(text = data.blok, weight = 0.15f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.15f)

        if (!isSelectionMode) {
            Box(modifier = Modifier.weight(0.10f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEditClick(data) }
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PengirimanTableRow(
    data: PengirimanData,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: (Int) -> Unit,
    onLongPress: (PengirimanData) -> Unit,
    onDetailClick: (PengirimanData) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection(data.id)
                    } else {
                        onDetailClick(data)
                    }
                },
                onLongClick = { onLongPress(data) }
            )
            .background(Grey.copy(0.5f))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection(data.id) },
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }

        TableCellText(text = data.waktuPengiriman, weight = 0.20f)
        TableCellText(text = data.spbNumber, weight = 0.35f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.20f)
    }
}


@Composable
fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.Black,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .width(110.dp)
            .background(OldGrey, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Text(
            text = label,
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}