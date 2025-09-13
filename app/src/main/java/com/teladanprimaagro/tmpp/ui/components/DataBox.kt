package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.White

@Composable
fun DataBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(White, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontSize = 14.sp, color = Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Black)
    }
}