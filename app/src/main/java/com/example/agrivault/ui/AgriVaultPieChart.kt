package com.example.agrivault.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class CategorySpending(
    val categoryName: String,
    val amount: Double,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgriVaultPieChart(
    spendingData: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    val totalSpending = spendingData.sumOf { it.amount }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(spendingData) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size
                val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
                val radius = canvasSize.minDimension / 2
                val innerRadius = radius * 0.7f

                var startAngle = -90f

                spendingData.forEach { data ->
                    val sweepAngle = ((data.amount / totalSpending) * 360f).toFloat()
                    
                    drawArc(
                        color = data.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = radius - innerRadius)
                    )
                    startAngle += sweepAngle
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "₹${totalSpending.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            spendingData.forEach { data ->
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = data.color)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${data.categoryName} (${((data.amount / totalSpending) * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
