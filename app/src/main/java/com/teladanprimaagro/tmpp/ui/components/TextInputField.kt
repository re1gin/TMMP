package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.LightGrey
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        singleLine = true,
        readOnly = readOnly,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.heightIn(min = 55.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        colors = TextFieldDefaults.colors(
            // Warna teks
            focusedTextColor = Black.copy(0.5f),
            unfocusedTextColor = White,
            errorTextColor = MaterialTheme.colorScheme.error,

            // Warna container (background TextField)
            focusedContainerColor = LightGrey,
            unfocusedContainerColor = Grey.copy(0.7f),
            errorContainerColor = MaterialTheme.colorScheme.error,

            // Warna indicator (garis bawah, kita buat transparan)
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            // Warna label
            focusedLabelColor = Black,
            unfocusedLabelColor = MainColor,
            errorLabelColor = MaterialTheme.colorScheme.error,
        )
    )
}