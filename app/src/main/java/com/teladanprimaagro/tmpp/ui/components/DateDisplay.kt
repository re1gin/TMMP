package com.teladanprimaagro.tmpp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.ui.theme.Red
import com.teladanprimaagro.tmpp.ui.theme.Yellow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FullDateCard(title: String, modifier: Modifier = Modifier) {
    val currentDate = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MM")
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            )
            .background( //ContainerColor
                brush = Brush.verticalGradient(
                    colors = listOf(Yellow, Red),
                    startY = 0f
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.White.copy(0.8f), RoundedCornerShape(50.dp)), //BoxColor
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Perusahaan",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DateDisplay(
                    label = "Tanggal",
                    value = currentDate.format(dayFormatter),
                    backgroundColor = Color.White.copy(0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                DateDisplay(
                    label = "Bulan",
                    value = currentDate.format(monthFormatter),
                    backgroundColor = Color.White.copy(0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                DateDisplay(
                    label = "Tahun",
                    value = currentDate.format(yearFormatter),
                    backgroundColor = Color.White.copy(0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DateDisplay(
    label: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 20.dp
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = contentColor,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}