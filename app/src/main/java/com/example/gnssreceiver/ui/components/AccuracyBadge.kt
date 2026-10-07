package com.example.gnssreceiver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gnssreceiver.util.CoordinateFormatter

@Composable
fun AccuracyBadge(
    accuracyMeters: Float?,
    modifier: Modifier = Modifier
) {
    val level = CoordinateFormatter.getAccuracyLevel(accuracyMeters)
    val accuracyText = CoordinateFormatter.formatAccuracy(accuracyMeters)

    Surface(
        modifier = modifier,
        color = level.color.copy(alpha = 0.15f),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(level.color, shape = CircleShape)
            )
            Text(
                text = " $accuracyText • ${level.label.substringBefore(" ")}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = level.color,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
