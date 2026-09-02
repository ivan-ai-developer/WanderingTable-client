package ru.gohasoft.wanderingtable.core.uikit.components.field

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/**
 * [LabeledTextField]'s multi-line sibling, for the "Note" boxes. It grows past [minHeight] as the
 * text does rather than scrolling inside a fixed box.
 */
@Composable
fun MultilineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minHeight: Dp = 80.dp,
) {
    val extended = MaterialTheme.extendedColors
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = extended.label,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 0.55.sp,
        )
        Box(
            modifier = Modifier
                .padding(top = WanderingTableSpacing.s)
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .background(extended.tint, RoundedCornerShape(WanderingTableRadius.m))
                .border(2.dp, extended.tintBorder, RoundedCornerShape(WanderingTableRadius.m))
                // Before the padding, so tapping anywhere in the box focuses the field.
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { focusRequester.requestFocus() }
                .padding(horizontal = WanderingTableSpacing.m, vertical = WanderingTableSpacing.s),
            contentAlignment = Alignment.TopStart,
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = extended.caption,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                )
            }
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                ),
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MultilineTextFieldPreview() {
    WanderingTableTheme {
        var value by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
        ) {
            MultilineTextField(
                label = "Note (optional)",
                value = value,
                onValueChange = { value = it },
                placeholder = "First time playing this one, happy to learn the rules together.",
            )
        }
    }
}
