package app.kumo.beta.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.SweepGradient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ColorPreset(val hex: String, val color: Color)

val KumoColorPresets = listOf(
    ColorPreset("#FF6B35", Color(0xFFFF6B35)), // Kumo Orange
    ColorPreset("#FF0000", Color(0xFFFF0000)), // Red
    ColorPreset("#00FF00", Color(0xFF00FF00)), // Green
    ColorPreset("#0000FF", Color(0xFF0000FF)), // Blue
    ColorPreset("#FFFF00", Color(0xFFFFFF00)), // Yellow
    ColorPreset("#FF00FF", Color(0xFFFF00FF)), // Magenta
    ColorPreset("#00FFFF", Color(0xFF00FFFF)), // Cyan
    ColorPreset("#FFA500", Color(0xFFFFA500)), // Orange
    ColorPreset("#800080", Color(0xFF800080)), // Purple
    ColorPreset("#FFC0CB", Color(0xFFFFC0CB))  // Pink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircularColorPickerDialog(
    initialHex: String = "#FFFFFF",
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    // Parse initial hex into HSV
    LaunchedEffect(initialHex) {
        try {
            val clean = initialHex.removePrefix("#").trim()
            val argb = when (clean.length) {
                6 -> ("FF" + clean).toLong(16).toInt()
                8 -> clean.toLong(16).toInt()
                else -> 0xFFFFFFFF.toInt()
            }
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(argb, hsv)
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        } catch (e: Exception) {
            hue = 0f
            saturation = 1f
            value = 1f
        }
    }

    val currentColorArgb = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val currentColorHex = remember(currentColorArgb) {
        String.format("#%06X", (0xFFFFFF and currentColorArgb))
    }

    var hexInputText by remember(currentColorHex) { mutableStateOf(currentColorHex) }
    var hexErrorText by remember { mutableStateOf<String?>(null) }

    fun validateAndApplyHex(input: String): Boolean {
        val clean = input.removePrefix("#").trim()
        val isValid = (clean.length == 6 || clean.length == 8) && clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (isValid) {
            try {
                val argb = if (clean.length == 6) ("FF" + clean).toLong(16).toInt() else clean.toLong(16).toInt()
                val hsv = FloatArray(3)
                AndroidColor.colorToHSV(argb, hsv)
                hue = hsv[0]
                saturation = hsv[1]
                value = hsv[2]
                hexErrorText = null
                return true
            } catch (e: Exception) {
                hexErrorText = "Invalid format. Use #RRGGBB"
                return false
            }
        } else {
            hexErrorText = "Invalid format. Use #RRGGBB"
            return false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Color Picker",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // COMPONENT 1: CIRCULAR HUE PICKER (220dp)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val wheelBitmap = remember { createColorWheelBitmap(220) }
                    Image(
                        bitmap = wheelBitmap.asImageBitmap(),
                        contentDescription = "Color Wheel",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val x = change.position.x - centerX
                                    val y = change.position.y - centerY
                                    var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f
                                    hue = angle
                                }
                            }
                    )

                    // Indicator Thumb
                    val angleRad = Math.toRadians(hue.toDouble())
                    val radiusDp = 80.0
                    val thumbX = (cos(angleRad) * radiusDp).toFloat()
                    val thumbY = (sin(angleRad) * radiusDp).toFloat()

                    Box(
                        modifier = Modifier
                            .offset(x = thumbX.dp, y = thumbY.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(currentColorArgb))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                Text(
                    text = "Hue: ${hue.toInt()}°",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                // COMPONENT 2: BRIGHTNESS / SATURATION SLIDER
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brightness", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("${(value * 100).toInt()}%", fontSize = 13.sp, color = Color.LightGray)
                    }

                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF444444)
                        )
                    )
                }

                // COMPONENT 3: COLOR PREVIEW BOX
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Preview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentColorArgb))
                            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    )
                }

                // COMPONENT 4: HEX COLOR INPUT FIELD (WITH COPY/PASTE)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("HEX Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hexInputText,
                            onValueChange = { input ->
                                hexInputText = input
                                validateAndApplyHex(input)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = hexErrorText != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0xFF444444),
                                focusedContainerColor = Color(0xFF101010),
                                unfocusedContainerColor = Color(0xFF101010)
                            )
                        )

                        // Copy Button
                        IconButton(
                            onClick = {
                                val clip = ClipData.newPlainText("HEX Color", currentColorHex)
                                clipboardManager.setPrimaryClip(clip)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF262626), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX", tint = Color.White)
                        }

                        // Paste Button
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasted = clip.getItemAt(0).text.toString()
                                    hexInputText = pasted
                                    validateAndApplyHex(pasted)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF262626), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste HEX", tint = Color.White)
                        }
                    }

                    if (hexErrorText != null) {
                        Text(
                            text = hexErrorText!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // COMPONENT 5: COLOR PRESETS (10 PRESET CIRCLES)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Presets", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KumoColorPresets.forEach { preset ->
                            val isSelected = currentColorHex.equals(preset.hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(preset.color)
                                    .border(2.dp, if (isSelected) Color.White else Color(0xFF333333), CircleShape)
                                    .clickable {
                                        hexInputText = preset.hex
                                        validateAndApplyHex(preset.hex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected Preset",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // COMPONENT 6: DIALOG BUTTONS (CANCEL / APPLY)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF262626),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (validateAndApplyHex(hexInputText)) {
                                onColorSelected(currentColorHex)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun createColorWheelBitmap(sizeDp: Int): Bitmap {
    val size = sizeDp * 2
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = size / 2f

    val colors = intArrayOf(
        AndroidColor.RED,
        AndroidColor.YELLOW,
        AndroidColor.GREEN,
        AndroidColor.CYAN,
        AndroidColor.BLUE,
        AndroidColor.MAGENTA,
        AndroidColor.RED
    )
    val sweepGradient = SweepGradient(radius, radius, colors, null)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = sweepGradient
        style = Paint.Style.FILL
    }

    canvas.drawCircle(radius, radius, radius, paint)
    return bitmap
}
