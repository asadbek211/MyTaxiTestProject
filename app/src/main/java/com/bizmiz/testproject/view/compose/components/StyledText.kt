package com.bizmiz.testproject.view.compose.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.bizmiz.testproject.R

@Composable
fun StyledText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.W500,
        overflow = TextOverflow.Ellipsis,
        fontFamily = FontFamily(Font(R.font.lato_regular)),
        textAlign = textAlign
    )
}