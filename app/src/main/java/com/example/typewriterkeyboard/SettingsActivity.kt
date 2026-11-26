package com.example.typewriterkeyboard

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.io.File

class SettingsActivity : Activity() {
    private val PICK = 1
    private lateinit var p: SharedPreferences
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        p = Cfg.prefs(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }
        fun label(t: String) { col.addView(TextView(this).apply { text = t; setPadding(0, dp(14), 0, dp(2)) }) }
        fun button(t: String, a: () -> Unit) {
            col.addView(Button(this).apply { text = t; isAllCaps = false; setOnClickListener { a() } })
        }
        fun spinner(title: String, key: String, items: List<Pair<String, String>>, def: String) {
            label(title)
            val s = Spinner(this)
            s.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items.map { it.second })
            s.setSelection(items.indexOfFirst { it.first == p.getString(key, def) }.coerceAtLeast(0))
            s.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, id: Long) {
                    p.edit().putString(key, items[i].first).apply()
                }
                override fun onNothingSelected(a: AdapterView<*>?) {}
            }
            col.addView(s)
        }
        fun seek(title: String, key: String, min: Int, max: Int, def: Int) {
            label(title)
            val s = SeekBar(this)
            s.max = max - min
            s.progress = p.getInt(key, def) - min
            s.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) { p.edit().putInt(key, v + min).apply() }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
            col.addView(s)
        }
        fun check(title: String, key: String) {
            col.addView(CheckBox(this).apply {
                text = title
                isChecked = p.getBoolean(key, false)
                setOnCheckedChangeListener { _, v -> p.edit().putBoolean(key, v).apply() }
            })
        }
        fun hex(title: String, key: String, hint: String) {
            label(title)
            val e = EditText(this)
            e.hint = hint
            e.setSingleLine()
            e.setText(p.getString(key, ""))
            e.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { p.edit().putString(key, s.toString().trim()).apply() }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
            col.addView(e)
        }

        col.addView(TextView(this).apply { text = "Typewriter Keyboard"; textSize = 22f })
        button("1. Enable the keyboard") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        button("2. Switch to this keyboard") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
        col.addView(EditText(this).apply { hint = "Tap here to test the keyboard" })

        spinner("Language", "lang", Cfg.LANGS, "en")
        spinner("Theme", "theme", Cfg.THEMES, "classic")
        seek("Key height (dp)", "kh", 32, 72, 46)
        seek("Key opacity (%)", "kp", 20, 100, 100)
        check("Show number row", "nr")
        check("Transparent keys (photo style, Custom theme only)", "gl")
        label("Custom theme colors (hex, for example #2B3A42). Leave empty for defaults.")
        hex("Keyboard", "cp", "#2B3A42")
        hex("Keys", "ck", "#F7F9FA")
        hex("Key text (use #FFFFFF with transparent keys)", "cl", "#1D2A30")
        hex("Highlight", "ca", "#E0A526")
        button("Choose keyboard picture") {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), PICK)
        }
        button("Remove keyboard picture") { File(filesDir, "bg.jpg").delete() }

        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onActivityResult(rc: Int, res: Int, d: Intent?) {
        super.onActivityResult(rc, res, d)
        val uri = d?.data
        if (rc != PICK || res != RESULT_OK || uri == null) return
        try {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
            var s = 1
            while (o.outWidth / s > 1400 || o.outHeight / s > 1400) s *= 2
            val bmp = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = s })
            }
            File(filesDir, "bg.jpg").outputStream().use { bmp?.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            p.edit().putLong("bgv", System.currentTimeMillis()).apply()
            Toast.makeText(this, "Picture saved. Set Theme to Custom to use it.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not load that picture.", Toast.LENGTH_LONG).show()
        }
    }
}
