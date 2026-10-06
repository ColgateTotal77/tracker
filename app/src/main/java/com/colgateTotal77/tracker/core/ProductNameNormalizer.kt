package com.colgateTotal77.tracker.core

object ProductNameNormalizer {

    private val HOMOGLYPHS: Map<Char, Char> = mapOf(
        'a' to 'а',
        'c' to 'с',
        'e' to 'е',
        'i' to 'і',
        'o' to 'о',
        'p' to 'р',
        'x' to 'х',
        'y' to 'у',
    )

    private val APOSTROPHES: Set<Char> = setOf('\'', '`', '´', '‘', '’', 'ʼ', '＇')

    fun normalize(raw: String): String {
        val lowered = raw.lowercase()
        val sb = StringBuilder(lowered.length)
        for (ch in lowered) {
            when {
                ch.isWhitespace() -> {}
                ch in APOSTROPHES -> sb.append('\'')
                else -> sb.append(HOMOGLYPHS[ch] ?: ch)
            }
        }
        return sb.toString()
    }

    fun repairDisplay(raw: String): String = raw.map { ch ->
        when {
            ch in APOSTROPHES -> '\''
            else -> HOMOGLYPHS[ch.lowercaseChar()]?.let { cyr ->
                if (ch.isUpperCase()) cyr.uppercaseChar() else cyr
            } ?: ch
        }
    }.joinToString("")
}