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

    fun repairDisplay(raw: String): String {
        return raw.split(Regex("\\s+")).joinToString(" ") { word ->
            if (!word.any(::isCyrillic)) return@joinToString word

            word.map { ch ->
                when {
                    ch in APOSTROPHES -> '\''
                    else -> HOMOGLYPHS[ch.lowercaseChar()]?.let { cyr ->
                        if (ch.isUpperCase()) cyr.uppercaseChar()
                        else cyr
                    } ?: ch
                }
            }.joinToString("")
        }
    }

    private fun isCyrillic(ch: Char): Boolean {
        val c = ch.lowercaseChar()
        return c in 'а'..'я' || c == 'і' || c == 'ї' || c == 'є' || c == 'ґ'
    }
}