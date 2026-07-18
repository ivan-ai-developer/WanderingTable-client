package ru.gohasoft.wanderingtable.core.uikit.components.field

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Purple900, RoundedCornerShape(WanderingTableRadius.m))
            .padding(WanderingTableSpacing.m),
    ) {
        Text(
            text = label.uppercase(),
            color = Color(0xFFC6BEE8),
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 0.55.sp,
        )
        Box(
            modifier = Modifier
                .padding(top = WanderingTableSpacing.s)
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(WanderingTableRadius.s))
                .border(1.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(WanderingTableRadius.s))
                .padding(horizontal = WanderingTableSpacing.m),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.55f),
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                singleLine = true,
            )
        }
    }
}

@Preview(name = "Dark")
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun AuthTextFieldPreview() {
    WanderingTableTheme {
        var value by remember { mutableStateOf("") }
        Box(modifier = Modifier.background(Purple900).padding(WanderingTableSpacing.m)) {
            AuthTextField(label = "Email", value = value, onValueChange = { value = it }, placeholder = "you@example.com")
        }
    }
}
