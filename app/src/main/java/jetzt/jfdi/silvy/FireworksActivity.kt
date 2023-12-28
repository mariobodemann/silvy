package jetzt.jfdi.silvy

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jetzt.jfdi.silvy.Particle.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


class FireworksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bounds = intent.getIntArrayExtra("rect").toRect()

        if (bounds.isSane()) {
            setContent {
                Fireworks(bounds) {
                    finish()
                }
            }
        } else {
            setContent {
                HowToAccessibilityInfo()
            }
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun Fireworks(from: Rect, finish: () -> Unit) {
    val to = with(LocalDensity.current) {
        val config = LocalConfiguration.current
        val toHeights = 400
        val toWidth = 20
        Rect(
            toWidth / 2,
            (config.screenHeightDp.dp.toPx() / 2f - toHeights / 2f).roundToInt(),
            (config.screenWidthDp.dp.toPx() - toWidth).roundToInt(),
            (config.screenHeightDp.dp.toPx() / 2f + toHeights / 2f).roundToInt()
        )
    }

    var timeLeft by remember { mutableLongStateOf(5000) }

    var particles by remember {
        mutableStateOf(
            generateParticles(from, to)
        )
    }

    LaunchedEffect(key1 = timeLeft) {
        particles = particles.update()

        var last = System.currentTimeMillis()
        while (timeLeft > 0 && particles.isNotEmpty()) {
            delay(16) // JOLO
            particles = particles.update()

            timeLeft -= System.currentTimeMillis() - last
            last = System.currentTimeMillis()
        }

        finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val time = System.currentTimeMillis()
        for (particle in particles) {
            if (particle.isAlive(time)) {
                with(LocalDensity.current) {
                    val x = particle.position.x.toDp()
                    val y = particle.position.y.toDp()

                    val rotation = if (particle is Rocket) particle.rotationInDegrees - 45f else 0f

                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .wrapContentSize()
                    ) {
                        Text(
                            modifier = Modifier.rotate(rotation),
                            color = Color.White,
                            fontSize = 22.sp,

                            text = when (particle) {
                                is Rocket -> "🚀"
                                is Poof -> "🔆"
                                is Star -> "🎆"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HowToAccessibilityInfo() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    fontSize = 54.sp,
                    fontStyle = FontStyle.Italic,
                    text = stringResource(id = R.string.main_show_how_to_title)
                )
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    fontSize = 42.sp,
                    text = stringResource(id = R.string.main_show_how_to_body)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openAccessibilitySettings(context) }
                ) {
                    Text(text = stringResource(id = R.string.main_goto_settings))
                }
            }
        }
    }
}

private fun IntArray?.toRect(): Rect =
    if (this == null || size < 4) {
        Rect()
    } else {
        val (left, top, right, bottom) = this
        Rect(left, top, right, bottom)
    }

private fun Rect.isSane(): Boolean = width() > 0 && height() > 0
