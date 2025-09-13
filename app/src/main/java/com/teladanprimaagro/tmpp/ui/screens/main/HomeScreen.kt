package com.teladanprimaagro.tmpp.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.screens.driver.DriverContent
import com.teladanprimaagro.tmpp.ui.screens.harvester.HarvesterContent
import com.teladanprimaagro.tmpp.ui.theme.MainBackground


@Composable
fun HomeScreen(
    navController: NavController,
    userRole: UserRole,
    paddingValues: PaddingValues,
) {
    var contentVisible by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MainBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (userRole) {
                UserRole.HARVESTER -> HarvesterContent(navController = navController)
                UserRole.DRIVER -> DriverContent(navController = navController)
            }
        }
    }
}

