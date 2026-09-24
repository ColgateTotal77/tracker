package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun DateTimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Date & Time (DD/MM/YYYY HH:MM)"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            if (isValidPartialDateTime(digitsOnly)) onValueChange(digitsOnly)
        },
        isError = isError,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = DateTimeVisualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

private fun isValidPartialDateTime(input: String): Boolean {
    if (input.length > 12) return false

    if (input.isNotEmpty()) {
        val firstDigit = input[0].digitToInt()
        if (firstDigit > 3) return false
    }
    if (input.length >= 2) {
        val day = input.substring(0, 2).toInt()
        if (day !in 1..31) return false
    }

    if (input.length >= 3) {
        val firstDigit = input[2].digitToInt()
        if (firstDigit > 1) return false
    }
    if (input.length >= 4) {
        val month = input.substring(2, 4).toInt()
        if (month !in 1..12) return false
    }

    if (input.length >= 9) {
        val firstDigit = input[8].digitToInt()
        if (firstDigit > 2) return false
    }
    if (input.length >= 10) {
        val hour = input.substring(8, 10).toInt()
        if (hour !in 0..23) return false
    }

    if (input.length >= 11) {
        val firstDigit = input[10].digitToInt()
        if (firstDigit > 5) return false
    }

    return true
}

private object DateTimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(12)
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/"
            if (i == 7) out += " "
            if (i == 9) out += ":"
        }

        val offsetMapping = object : OffsetMapping {
            val origToTrans = listOf(0, 1, 3, 4, 6, 7, 8, 9, 11, 12, 14, 15, 16)
            val transToOrig = listOf(0, 1, 2, 2, 3, 4, 4, 5, 6, 7, 8, 8, 9, 10, 10, 11, 12)

            override fun originalToTransformed(offset: Int) = origToTrans.getOrElse(offset) { 16 }
            override fun transformedToOriginal(offset: Int) = transToOrig.getOrElse(offset) { 12 }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}