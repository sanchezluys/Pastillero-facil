package com.example.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale
import java.util.regex.Pattern

data class ParsedVoiceMedication(
    val name: String,
    val frequencyHours: Int?,
    val rawText: String
)

object VoiceInputHelper {

    fun createSpeechIntent(promptMessage: String = "Diga el nombre del medicamento y cada cuántas horas lo toma"): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, promptMessage)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    /**
     * Parses Spanish voice input like:
     * "Paracetamol cada 8 horas"
     * "Tomar Losartán cada doce horas"
     * "Aspirina cada 6 horas"
     * "Enalapril una vez al día"
     */
    fun parseMedicationVoice(spokenText: String): ParsedVoiceMedication {
        val trimmed = spokenText.trim()
        val lower = trimmed.lowercase(Locale.getDefault())

        var detectedFreq: Int? = null

        // Check for words representing numbers
        val wordsToNumber = mapOf(
            "cuatro" to 4,
            "seis" to 6,
            "ocho" to 8,
            "doce" to 12,
            "veinticuatro" to 24,
            "una" to 24,
            "uno" to 24,
            "un" to 24
        )

        // Pattern: cada (número | palabra) (horas?)
        val cadaPattern = Pattern.compile("cada\\s+(\\d+|cuatro|seis|ocho|doce|veinticuatro)\\s*(horas|hora)?")
        val cadaMatcher = cadaPattern.matcher(lower)

        var cleanName = trimmed

        if (cadaMatcher.find()) {
            val numOrWord = cadaMatcher.group(1) ?: ""
            detectedFreq = numOrWord.toIntOrNull() ?: wordsToNumber[numOrWord]
            // Extract medication name by cutting off the "cada ..." part
            val startIndex = cadaMatcher.start()
            cleanName = trimmed.substring(0, startIndex).trim()
        } else if (lower.contains("una vez al día") || lower.contains("una vez al dia") || lower.contains("diario") || lower.contains("diaria")) {
            detectedFreq = 24
            cleanName = trimmed
                .replace("(?i)una vez al d[ií]a".toRegex(), "")
                .replace("(?i)diario".toRegex(), "")
                .replace("(?i)diaria".toRegex(), "")
                .trim()
        }

        // Clean common prefixes like "tomar", "tomarme", "cargar", "medicina", "pastilla"
        cleanName = cleanName
            .replace("(?i)^tomar(me)?\\s+".toRegex(), "")
            .replace("(?i)^cargar\\s+".toRegex(), "")
            .replace("(?i)^agregar\\s+".toRegex(), "")
            .replace("(?i)^pastilla\\s+(de\\s+)?".toRegex(), "")
            .replace("(?i)^medicamento\\s+(de\\s+)?".toRegex(), "")
            .trim()

        if (cleanName.isEmpty() && trimmed.isNotEmpty()) {
            cleanName = trimmed
        }

        // Capitalize first letter of name
        cleanName = cleanName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        return ParsedVoiceMedication(
            name = cleanName,
            frequencyHours = detectedFreq,
            rawText = trimmed
        )
    }
}
