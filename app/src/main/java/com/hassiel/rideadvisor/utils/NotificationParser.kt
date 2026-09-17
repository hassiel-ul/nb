package com.hassiel.rideadvisor.utils

import java.util.Locale
import java.util.regex.Pattern

/**
 * Analiza el texto de notificaciones de distintas apps de transporte y extrae
 * precio y distancia de forma tolerante a diferentes formatos.
 *
 * Importante (sección 18/19 del documento de requisitos):
 * - NO se inventa información: si un dato no aparece en el texto, se devuelve null.
 * - El parser nunca lanza excepciones hacia el llamador; cualquier fallo interno
 *   se traduce en un resultado parcial o vacío.
 * - Es adaptable: no asume que todas las apps usan el mismo formato de notificación.
 */
object NotificationParser {

    // Precio: "$450", "RD$450", "US$450", "450 pesos", "450 RD$", "$ 450.50", "$1,250"
    private val PRICE_PATTERNS = listOf(
        Pattern.compile("(?:RD\\$|US\\$|\\$)\\s?([0-9][0-9.,]*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([0-9][0-9.,]*)\\s?(?:pesos|RD\\$|USD|\\$)", Pattern.CASE_INSENSITIVE)
    )

    // Distancia: "8.5 km", "8,5 km", "8 km", "8.5km", "8.5 mi"
    private val DISTANCE_PATTERNS = listOf(
        Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s?(km|kilometros|kilómetros)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s?(mi|miles|millas)", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Intenta determinar la app de origen a partir del paquete Android y,
     * como respaldo, del propio texto de la notificación.
     */
    fun resolveAppName(packageName: String, text: String): String {
        Constants.SUPPORTED_PACKAGES[packageName]?.let { return it }

        val lower = text.lowercase(Locale.getDefault())
        return when {
            lower.contains("uber") -> "Uber"
            lower.contains("indrive") || lower.contains("in drive") -> "inDrive"
            lower.contains("didi") -> "DiDi"
            else -> "Desconocida"
        }
    }

    /**
     * Extrae el precio del texto de la notificación. Devuelve null si no se
     * encuentra ningún patrón reconocible (nunca inventa un valor).
     */
    fun extractPrice(text: String): Double? {
        for (pattern in PRICE_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                parseNumber(raw)?.let { return it }
            }
        }
        return null
    }

    /**
     * Extrae la distancia (en km) del texto de la notificación. Si el valor
     * viene en millas, se convierte a km. Devuelve null si no se encuentra.
     */
    fun extractDistance(text: String): Double? {
        for (pattern in DISTANCE_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                val unit = matcher.group(2)?.lowercase(Locale.getDefault()) ?: ""
                val value = parseNumber(raw) ?: continue
                return if (unit.startsWith("mi")) value * 1.60934 else value
            }
        }
        return null
    }

    /**
     * Convierte un string numérico con formatos mixtos ("1,250.50", "8,5", "450")
     * a Double de forma segura. Devuelve null si no se puede interpretar.
     */
    private fun parseNumber(raw: String): Double? {
        return try {
            var cleaned = raw.trim()
            val hasComma = cleaned.contains(',')
            val hasDot = cleaned.contains('.')

            cleaned = when {
                hasComma && hasDot -> cleaned.replace(",", "")
                hasComma && !hasDot -> {
                    // Ambiguo: "1,250" (miles) vs "8,5" (decimal). Si hay más de 2
                    // dígitos tras la coma lo tratamos como separador de miles.
                    val parts = cleaned.split(",")
                    if (parts.last().length == 3) cleaned.replace(",", "")
                    else cleaned.replace(",", ".")
                }
                else -> cleaned
            }
            cleaned.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Genera una clave estable a partir del contenido para detectar duplicados
     * cuando el sistema reenvía la misma notificación.
     */
    fun buildDedupeKey(packageName: String, text: String): String {
        return "$packageName:${text.trim().hashCode()}"
    }
}
