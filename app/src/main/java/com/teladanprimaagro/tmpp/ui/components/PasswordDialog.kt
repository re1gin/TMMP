package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit, correctPassword: String) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = LightGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Padding pada Card
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Masukkan Sandi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Masukkan sandi untuk melanjutkan.",
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Text Field Section
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordError = false
                    },
                    label = {
                        Text(
                            "Sandi",
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    isError = isPasswordError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = White,
                        errorContainerColor = Color.White,
                        focusedBorderColor = Black,
                        unfocusedBorderColor = OldGrey,
                        errorBorderColor = DangerRed,
                        focusedLabelColor = Black,
                        unfocusedLabelColor = OldGrey,
                        errorLabelColor = DangerRed,
                        cursorColor = MainColor
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    ),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val tintColor = if (isPasswordError) DangerRed else OldGrey
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = "Toggle password visibility",
                                tint = tintColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isPasswordError) {
                    Text(
                        text = "Password salah. Silakan coba lagi.",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OldGrey)
                    ) {
                        Text(
                            text = "Batal",
                            color = White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (password == correctPassword) {
                                onConfirm(password)
                            } else {
                                isPasswordError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainColor)
                    ) {
                        Text(
                            text = "Konfirmasi",
                            color = Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}