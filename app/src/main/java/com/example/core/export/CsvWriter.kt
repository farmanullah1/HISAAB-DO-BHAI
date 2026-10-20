package com.example.core.export

import java.io.Writer

object CsvWriter {
    private val formulaStarts = setOf('=', '+', '-', '@', '\t', '\r')

    /**
     * Sanitizes user-entered text fields to prevent CSV formula injection in spreadsheet software.
     * Never pass purely numeric amount columns through here.
     */
    fun safeText(value: String): String {
        val v = value.replace("\u0000", "")
        return if (v.isNotEmpty() && v[0] in formulaStarts) "'$v" else v
    }

    /**
     * Standard CSV escaping for commas, quotes, and newlines.
     */
    fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }

    fun writeRow(out: Writer, fields: List<String>) {
        out.write(fields.joinToString(",") { escape(it) })
        out.write("\r\n")
    }

    /**
     * Writes UTF-8 Byte Order Mark (\uFEFF) so Excel opens UTF-8 text (including Urdu) properly.
     */
    fun writeBom(out: Writer) = out.write("\uFEFF")

    /**
     * Formats amounts as plain decimal without thousands separators, suitable for CSV:
     * 150 -> "1.50", 123456 -> "1234.56", -500 -> "-5.00"
     */
    fun amount(paisa: Long): String {
        val neg = paisa < 0
        val abs = if (neg) -paisa else paisa
        return (if (neg) "-" else "") + "${abs / 100}.%02d".format(java.util.Locale.ROOT, (abs % 100).toInt())
    }
}
