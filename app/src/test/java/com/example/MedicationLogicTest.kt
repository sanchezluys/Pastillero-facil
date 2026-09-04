package com.example

import com.example.data.MedicationRepository
import com.example.voice.VoiceInputHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationLogicTest {

    @Test
    fun testCalculateTimesEvery8Hours() {
        val times = MedicationRepository.calculateTimes("08:00", 8)
        assertEquals(3, times.size)
        assertEquals(listOf("08:00", "16:00", "00:00"), times)
    }

    @Test
    fun testCalculateTimesEvery12Hours() {
        val times = MedicationRepository.calculateTimes("09:00", 12)
        assertEquals(2, times.size)
        assertEquals(listOf("09:00", "21:00"), times)
    }

    @Test
    fun testCalculateTimesEvery6Hours() {
        val times = MedicationRepository.calculateTimes("06:00", 6)
        assertEquals(4, times.size)
        assertEquals(listOf("06:00", "12:00", "18:00", "00:00"), times)
    }

    @Test
    fun testCalculateTimesOnceADay() {
        val times = MedicationRepository.calculateTimes("10:00", 24)
        assertEquals(1, times.size)
        assertEquals(listOf("10:00"), times)
    }

    @Test
    fun testVoiceInputParsing() {
        val parsed1 = VoiceInputHelper.parseMedicationVoice("Paracetamol cada 8 horas")
        assertEquals("Paracetamol", parsed1.name)
        assertEquals(8, parsed1.frequencyHours)

        val parsed2 = VoiceInputHelper.parseMedicationVoice("Tomar Losartán cada doce horas")
        assertEquals("Losartán", parsed2.name)
        assertEquals(12, parsed2.frequencyHours)

        val parsed3 = VoiceInputHelper.parseMedicationVoice("Aspirina una vez al día")
        assertEquals("Aspirina", parsed3.name)
        assertEquals(24, parsed3.frequencyHours)

        val parsed4 = VoiceInputHelper.parseMedicationVoice("Omeprazol cada 24 horas")
        assertEquals("Omeprazol", parsed4.name)
        assertEquals(24, parsed4.frequencyHours)
    }
}
