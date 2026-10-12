package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ClayAccentLimeDeep
import com.example.ui.theme.ClayBorderHairline
import com.example.ui.theme.ClayInkDeep
import com.example.ui.theme.ClaySurface
import com.example.ui.theme.ClayTextPrimary
import com.example.ui.theme.ClayTextSecondary

@Composable
fun ClayEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ClayBorderHairline, RoundedCornerShape(18.dp))
            .padding(vertical = 36.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clay miniature illustration composition
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(ClaySurface),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    // Soft shadow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x33000000), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.75f),
                            radius = size.width * 0.45f
                        )
                    )
                    // Clay matte prop gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ClayAccentLimeDeep, Color(0xFF9EA810)),
                            center = Offset(size.width * 0.35f, size.height * 0.3f),
                            radius = size.width * 0.45f
                        ),
                        radius = size.width * 0.38f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ClayInkDeep,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = ClayTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = ClayTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (actionTitle != null && onAction != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClayInkDeep,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text(
                        text = actionTitle,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
