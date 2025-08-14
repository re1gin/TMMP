package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.theme.Red


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuahCounter(label: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(IntrinsicSize.Max)
        ) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Kurang",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = count.toString(),
                onValueChange = { newValue ->
                    val num = newValue.toIntOrNull()
                    if (num != null && num >= 0) {
                        onCountChange(num)
                    } else if (newValue.isBlank()) {
                        onCountChange(0)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DotGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.White,
                ),
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = Color.Black
                )
            }
        }
    }
}