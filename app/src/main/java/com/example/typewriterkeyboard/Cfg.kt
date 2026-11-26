package com.example.typewriterkeyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

data class Look(val panel: Int, val key: Int, val text: Int, val accent: Int)

object Cfg {
    fun prefs(c: Context): SharedPreferences = c.getSharedPreferences("kb", Context.MODE_PRIVATE)
    fun col(s: String?, d: Int): Int = try { Color.parseColor(s) } catch (e: Exception) { d }

    val LANGS = listOf("en" to "English", "tl" to "Filipino", "ja" to "日本語 Japanese", "ko" to "한국어 Korean")
    val THEMES = listOf("classic" to "Classic", "midnight" to "Midnight", "sakura" to "Sakura",
        "forest" to "Forest", "ocean" to "Ocean", "custom" to "Custom (colors + picture)")

    fun look(p: SharedPreferences): Look = when (p.getString("theme", "classic")) {
        "midnight" -> Look(0xFF0B1020.toInt(), 0xFF1F2A44.toInt(), 0xFFE6ECFF.toInt(), 0xFF5EEAD4.toInt())
        "sakura" -> Look(0xFFE58FA5.toInt(), 0xFFFFF0F3.toInt(), 0xFF5B2333.toInt(), 0xFFFFD166.toInt())
        "forest" -> Look(0xFF1F3D2B.toInt(), 0xFFE8F1E4.toInt(), 0xFF1B2F22.toInt(), 0xFFF2C14E.toInt())
        "ocean" -> Look(0xFF12486B.toInt(), 0xFFE8F6FF.toInt(), 0xFF0B2F47.toInt(), 0xFFFFB703.toInt())
        "custom" -> {
            val glass = p.getBoolean("gl", false)
            Look(
                col(p.getString("cp", null), 0xFF2B3A42.toInt()),
                col(p.getString("ck", null), 0xFFF7F9FA.toInt()),
                col(p.getString("cl", null), if (glass) Color.WHITE else 0xFF1D2A30.toInt()),
                col(p.getString("ca", null), 0xFFE0A526.toInt())
            )
        }
        else -> Look(0xFF2B3A42.toInt(), 0xFFF7F9FA.toInt(), 0xFF1D2A30.toInt(), 0xFFE0A526.toInt())
    }
}
