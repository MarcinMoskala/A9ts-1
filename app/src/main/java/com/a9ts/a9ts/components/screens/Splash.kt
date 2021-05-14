package com.a9ts.a9ts.components.screens

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import com.a9ts.a9ts.components.MyTopBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.navigate

@Composable
fun Splash(navHostController: NavHostController, viewModel: SplashViewModel = viewModel()) {
    val redirectTo by viewModel.redirectTo.observeAsState("")

    if (redirectTo.isNotEmpty()) {
        navHostController.navigate(redirectTo)
    }

    Scaffold(
        topBar = { MyTopBar("Splash screen") },
        content = {
        }
    )
}