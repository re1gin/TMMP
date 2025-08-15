package com.teladanprimaagro.tmpp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FullDateCard(title: String, modifier: Modifier = Modifier) {
    val currentDate = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MM")
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    // Menggunakan Card untuk container utama
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(0.7f), RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DateDisplay(
                        label = "Tanggal",
                        value = currentDate.format(dayFormatter)
                    )
                    DateDisplay(
                        label = "Bulan",
                        value = currentDate.format(monthFormatter)
                    )
                    DateDisplay(
                        label = "Tahun",
                        value = currentDate.format(yearFormatter)
                    )
                }
            }
        }
    }
}

@Composable
fun DateDisplay(
    label: String,
    value: String
) {
    // Menggunakan Card dengan border
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        // Menghilangkan warna background agar terlihat transparan
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                // Warna teks angka sesuai gambar
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
