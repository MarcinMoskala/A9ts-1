package com.a9ts.a9ts.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)


val inputShape = Shapes.small.copy( //askmarcin where should I put this?
    bottomEnd = ZeroCornerSize,
    topEnd = ZeroCornerSize,
    bottomStart = ZeroCornerSize,
    topStart = ZeroCornerSize
)