package com.ravanbarvar.patientmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ravanbarvar.patientmanager.RavanbarvarApp

@Composable
fun currentApp(): RavanbarvarApp = LocalContext.current.applicationContext as RavanbarvarApp
