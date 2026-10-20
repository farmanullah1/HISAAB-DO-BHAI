package com.example.core

import com.example.core.export.CsvWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringWriter

class CsvWriterTest {

    @Test
    fun safeText_prependsSingleQuoteForFormulaCharacters() {
        assertEquals("'=1+1", CsvWriter.safeText("=1+1"))
        assertEquals("'+sum(A1)", CsvWriter.safeText("+sum(A1)"))
        assertEquals("'-diff", CsvWriter.safeText("-diff"))
        assertEquals("'@command", CsvWriter.safeText("@command"))
        assertEquals("Normal Note", CsvWriter.safeText("Normal Note"))
    }

    @Test
    fun escape_commasAndQuotes() {
        assertEquals("\"Chai, Paratha\"", CsvWriter.escape("Chai, Paratha"))
        assertEquals("\"He said \"\"Hi\"\"\"", CsvWriter.escape("He said \"Hi\""))
        assertEquals("Simple", CsvWriter.escape("Simple"))
    }

    @Test
    fun amount_plainDecimalFormatting() {
        assertEquals("1.50", CsvWriter.amount(150L))
        assertEquals("1234.56", CsvWriter.amount(123456L))
        assertEquals("-5.00", CsvWriter.amount(-500L))
        assertEquals("0.00", CsvWriter.amount(0L))
    }

    @Test
    fun writeRow_andWriteBom() {
        val sw = StringWriter()
        CsvWriter.writeBom(sw)
        CsvWriter.writeRow(sw, listOf("Date", "Description", "Amount", "Currency"))
        CsvWriter.writeRow(sw, listOf("2026-10-18", "Groceries", CsvWriter.amount(250000L), "PKR"))

        val content = sw.toString()
        val expected = "\uFEFFDate,Description,Amount,Currency\r\n2026-10-18,Groceries,2500.00,PKR\r\n"
        assertEquals(expected, content)
    }
}
