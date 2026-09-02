package com.ravanbarvar.patientmanager.ui.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.ui.currentApp
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(onResolved: (loggedIn: Boolean) -> Unit) {
    val app = currentApp()
    var scaleTarget by remember { mutableStateOf(0.7f) }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "splash-scale"
    )

    LaunchedEffect(Unit) {
        scaleTarget = 1f
        val loggedIn = app.authRepository.isLoggedIn.first()
        kotlinx.coroutines.delay(350)
        onResolved(loggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo_mark),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
