package com.anafthdev.musicompose.ui.scan_music

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anafthdev.musicompose.ui.theme.Purple200
import com.anafthdev.musicompose.ui.theme.white
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScanMusicProgress(
    angle: Float = 0f,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(80.dp)
            .drawBehind {
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 30f)
                )

                drawArc(
                    startAngle = -90f,
                    sweepAngle = angle,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round),
                    color = Purple200
                )
            }
    )
}

@Preview
@Composable
private fun ScanMusicProgressPreview() {
    ScanMusicProgress()
}





@Preview
@Composable
fun ScanMusicProgressMarker(
    modifier: Modifier = Modifier,
    angle: Int = 0,
    active: Boolean = false
) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                val theta = (angle - 90) * PI.toFloat() / 180f
                val startRadius = size.width / 2 * .7f
                val endRadius = size.width / 2 * .8f
                val startPos = Offset(cos(theta) * startRadius, sin(theta) * startRadius)
                val endPos = Offset(cos(theta) * endRadius, sin(theta) * endRadius)
                drawLine(
                    color = if (active) Purple200 else white.copy(alpha = 0.8f),
                    start = center + startPos,
                    end = center + endPos,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
    )
}
