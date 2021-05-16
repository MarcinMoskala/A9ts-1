package com.a9ts.a9ts.components.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.a9ts.a9ts.components.MyTopBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import com.a9ts.a9ts.tools.Route

@Composable
fun Splash(navHostController: NavHostController, viewModel: SplashViewModel = viewModel()) {
    val redirectTo by viewModel.redirectTo.observeAsState("")

    if (redirectTo.isNotEmpty()) {
        navHostController.navigate(redirectTo) {
            popUpTo(Route.SPLASH) { inclusive = true }
        }

    }

    Box(Modifier.fillMaxSize()) {
        Text("Loading...", modifier = Modifier.align(Alignment.Center))
    }
}