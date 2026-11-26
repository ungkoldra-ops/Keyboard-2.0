package com.example.typewriterkeyboard

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.io.File

private const val LEAD = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
private const val VOW = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
private val TAIL = listOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ",
    "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")
private val CV = mapOf("ㅗㅏ" to "ㅘ", "ㅗㅐ" to "ㅙ", "ㅗㅣ" to "ㅚ", "ㅜㅓ" to "ㅝ", "ㅜㅔ" to "ㅞ", "ㅜㅣ" to "ㅟ", "ㅡㅣ" to "ㅢ")
private val CT = mapOf("ㄱㅅ" to "ㄳ", "ㄴㅈ" to "ㄵ", "ㄴㅎ" to "ㄶ", "ㄹㄱ" to "ㄺ", "ㄹㅁ" to "ㄻ", "ㄹㅂ" to "ㄼ",
    "ㄹㅅ" to "ㄽ", "ㄹㅌ" to "ㄾ", "ㄹㅍ" to "ㄿ", "ㄹㅎ" to "ㅀ", "ㅂㅅ" to "ㅄ")
private val TS = CT.entries.associate { it.value to (it.key.substring(0, 1) to it.key.substring(1)) }
private val KR: Map<Char, Char> = "qwertyuiopasdfghjklzxcvbnm".toList().zip("ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔㅁㄴㅇㄹㅎㅗㅓㅏㅣㅋㅌㅊㅍㅠㅜㅡ".toList()).toMap()
private val KS: Map<Char, Char> = mapOf('q' to 'ㅃ', 'w' to 'ㅉ', 'e' to 'ㄸ', 'r' to 'ㄲ', 't' to 'ㅆ', 'o' to 'ㅒ', 'p' to 'ㅖ')
private val JP = mapOf("," to "、", "." to "。")
private val ROMA: Map<String, String> = HashMap<String, String>().also { m ->
    val V = "aiueo"
    "あいうえお".forEachIndexed { i, c -> m[V[i].toString()] = c.toString() }
    mapOf("k" to "かきくけこ", "s" to "さしすせそ", "t" to "たちつてと", "n" to "なにぬねの", "h" to "はひふへほ",
        "m" to "まみむめも", "r" to "らりるれろ", "g" to "がぎぐげご", "z" to "ざじずぜぞ", "d" to "だぢづでど",
        "b" to "ばびぶべぼ", "p" to "ぱぴぷぺぽ", "w" to "わゐうゑを").forEach { (c, ks) ->
        ks.forEachIndexed { i, ch -> m[c + V[i]] = ch.toString() }
    }
    m["ya"] = "や"; m["yu"] = "ゆ"; m["yo"] = "よ"; m["shi"] = "し"; m["chi"] = "ち"; m["tsu"] = "つ"
    m["fu"] = "ふ"; m["ji"] = "じ"; m["xtu"] = "っ"; m["ltu"] = "っ"; m["nn"] = "ん"
    val sm = "ゃゅょ"; val yv = "auo"
    listOf("k" to 'き', "g" to 'ぎ', "n" to 'に', "h" to 'ひ', "m" to 'み', "r" to 'り', "b" to 'び', "p" to 'ぴ')
        .forEach { (c, k) -> for (j in 0..2) m[c + "y" + yv[j]] = "" + k + sm[j] }
    listOf("sh" to 'し', "ch" to 'ち', "j" to 'じ').forEach { (c, k) -> for (j in 0..2) m[c + yv[j]] = "" + k + sm[j] }
}
private const val EMOJI = "😀 😃 😄 😁 😆 😂 🤣 😊 😇 🙂 😉 😍 🥰 😘 😋 😎 🤩 🥳 😏 😢 😭 😡 😱 😴 🤔 🙄 😬 🤗 🤫 🤯 🥺 😅 😜 🤪 😷 🥵 🥶 " +
    "👍 👎 👏 🙌 🙏 💪 👋 ✌️ 🤞 👌 🤝 👀 🧠 ❤️ 🧡 💛 💚 💙 💜 🖤 💔 💕 🔥 ✨ ⭐ 💯 🎉 🎂 🎁 🎵 🎮 ⚽ 🏆 " +
    "🐶 🐱 🐰 🦊 🐻 🐼 🐸 🐵 🦋 🌸 🌹 🌻 🌈 ☀️ 🌙 ⚡ ❄️ 🌊 🍎 🍌 🍓 🍉 🥭 🍕 🍔 🍟 🍜 🍣 🍚 🍩 🍪 🍦 ☕ 🍺 " +
    "🚗 ✈️ 🚀 🏠 📱 💻 📷 💡 📚 💰 ✅ ❌ ⚠️ ❓ 💬 🇵🇭 🇯🇵 🇰🇷 🇺🇸"

class KeyboardService : InputMethodService() {
    private lateinit var p: SharedPreferences
    private lateinit var root: FrameLayout
    private val h = Handler(Looper.getMainLooper())
    private var shift = false
    private var caps = false
    private var page = 0
    private var lang = "en"
    private var glass = false
    private var rb = ""
    private var lastShift = 0L
    private var bmp: Bitmap? = null
    private var bmpV = -1L

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun kh() = dp(p.getInt("kh", 46))

    override fun onCreate() { super.onCreate(); p = Cfg.prefs(this) }
    override fun onCreateInputView(): View { root = FrameLayout(this); render(); return root }
    override fun onStartInputView(i: EditorInfo?, r: Boolean) {
        super.onStartInputView(i, r)
        if (::root.isInitialized) { page = 0; render() }
    }
    override fun onFinishInputView(f: Boolean) { flush(); super.onFinishInputView(f) }

    private fun render() {
        val lk = Cfg.look(p)
        val custom = p.getString("theme", "classic") == "custom"
        glass = custom && p.getBoolean("gl", false)
        lang = p.getString("lang", "en") ?: "en"
        root.removeAllViews()
        root.setBackgroundColor(lk.panel)
        val f = File(filesDir, "bg.jpg")
        if (custom && f.exists()) {
            val v = p.getLong("bgv", 0)
            if (bmp == null || v != bmpV) { bmp = BitmapFactory.decodeFile(f.path); bmpV = v }
            val iv = ImageView(this)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            iv.setImageBitmap(bmp)
            root.addView(iv, FrameLayout.LayoutParams(-1, -1))
        }
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(3), dp(4), dp(3), dp(6))
        when (page) { 0 -> letters(col); 1 -> symbols(col); else -> emoji(col) }
        root.addView(col, FrameLayout.LayoutParams(-1, -2))
    }

    private fun letters(col: LinearLayout) {
        val digits = "1234567890"
        if (p.getBoolean("nr", false) && !glass) col.addView(rowOf(digits.map { c -> key(c.toString(), 1f) { put(c.toString()) } }))
        col.addView(rowOf("qwertyuiop".mapIndexed { i, c -> letter(c, digits[i]) }))
        col.addView(rowOf(listOf(spacer(.5f)) + "asdfghjkl".map { letter(it, null) } + listOf(spacer(.5f))))
        col.addView(rowOf(listOf(shiftKey()) + "zxcvbnm".map { letter(it, null) } + listOf(key("⌫", 1.5f, rep = true) { del() })))
        val name = Cfg.LANGS.first { it.first == lang }.second
        col.addView(rowOf(listOf(
            key("?123", 1.3f) { flush(); page = 1; render() },
            key("😊", 1f) { flush(); page = 2; render() },
            key("🌐", 1f) { cycleLang() },
            key(",", 1f) { punct(",") },
            key(name, 3f) { flush(); commit(" ") },
            key(".", 1f) { punct(".") },
            enterKey())))
    }

    private fun symbols(col: LinearLayout) {
        listOf("1234567890", "@#\$%&-+()/").forEach { r -> col.addView(rowOf(r.map { c -> key(c.toString(), 1f) { put(c.toString()) } })) }
        col.addView(rowOf("=*\"':;!?".map { c -> key(c.toString(), 1f) { put(c.toString()) } } + listOf(key("⌫", 1.5f, rep = true) { del() })))
        col.addView(rowOf(listOf(
            key("ABC", 1.5f) { page = 0; render() },
            key("😊", 1f) { page = 2; render() },
            key(",", 1f) { punct(",") },
            key("space", 3f) { commit(" ") },
            key(".", 1f) { punct(".") },
            enterKey())))
    }

    private fun emoji(col: LinearLayout) {
        val sv = ScrollView(this)
        val gl = LinearLayout(this)
        gl.orientation = LinearLayout.VERTICAL
        EMOJI.split(" ").chunked(8).forEach { r -> gl.addView(rowOf(r.map { e -> key(e, 1f) { put(e) } })) }
        sv.addView(gl)
        col.addView(sv, LinearLayout.LayoutParams(-1, kh() * 4 + dp(24)))
        col.addView(rowOf(listOf(
            key("ABC", 1.5f) { page = 0; render() },
            key("space", 4f) { commit(" ") },
            key("⌫", 1.5f, rep = true) { del() })))
    }

    private fun rowOf(v: List<View>): LinearLayout {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        v.forEach { r.addView(it) }
        return r
    }

    private fun spacer(w: Float): View = View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, w) }

    private fun letter(c: Char, digit: Char?): View {
        val up = shift || caps
        val t = (if (up) c.uppercaseChar() else c).toString()
        val hint: Char? = if (lang == "ko") (if (shift && KS.containsKey(c)) KS[c] else KR[c]) else if (!p.getBoolean("nr", false) || glass) digit else null
        val cs = SpannableStringBuilder(t)
        if (hint != null) {
            cs.append(hint.toString())
            cs.setSpan(RelativeSizeSpan(.5f), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            cs.setSpan(SuperscriptSpan(), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return key(cs, 1f, onLong = if (digit != null && lang != "ko") ({ put(digit.toString()) }) else null) { type(c) }
    }

    private fun shiftKey(): View = key(if (caps) "⇪" else "⇧", 1.5f, active = shift || caps) {
        val now = SystemClock.uptimeMillis()
        if (now - lastShift < 350) { caps = true; shift = false }
        else if (caps) { caps = false; shift = false }
        else shift = !shift
        lastShift = now
        render()
    }

    private fun enterKey(): View = key(if (glass) "✓" else "⏎", 1.4f) { enter() }

    private fun key(label: CharSequence, w: Float, active: Boolean = false, rep: Boolean = false,
                    onLong: (() -> Unit)? = null, click: () -> Unit): TextView {
        val lk = Cfg.look(p)
        val v = TextView(this)
        v.text = label
        v.gravity = Gravity.CENTER
        v.setTextColor(lk.text)
        v.textSize = if (glass) 22f else 18f
        v.isSingleLine = true
        if (glass) v.setShadowLayer(4f, 0f, 1f, Color.BLACK)
        v.background = bg(lk, active)
        val lp = LinearLayout.LayoutParams(0, kh(), w)
        lp.setMargins(dp(2), dp(3), dp(2), dp(3))
        v.layoutParams = lp
        var fired = false
        val repR = object : Runnable { override fun run() { click(); h.postDelayed(this, 55) } }
        val longR = Runnable { fired = true; v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); onLong?.invoke() }
        v.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true
                    fired = false
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (rep) { click(); h.postDelayed(repR, 400) } else if (onLong != null) h.postDelayed(longR, 400)
                }
                MotionEvent.ACTION_UP -> {
                    v.isPressed = false
                    h.removeCallbacks(repR); h.removeCallbacks(longR)
                    if (!rep && !fired) click()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    h.removeCallbacks(repR); h.removeCallbacks(longR)
                }
            }
            true
        }
        return v
    }

    private fun bg(lk: Look, active: Boolean): Drawable {
        fun shape(c: Int) = GradientDrawable().apply { cornerRadius = dp(7).toFloat(); setColor(c) }
        val a = p.getInt("kp", 100) * 255 / 100
        val keyC = if (glass) Color.TRANSPARENT else Color.argb(a, Color.red(lk.key), Color.green(lk.key), Color.blue(lk.key))
        val acc = Color.argb(if (glass) 120 else 255, Color.red(lk.accent), Color.green(lk.accent), Color.blue(lk.accent))
        val sl = StateListDrawable()
        sl.addState(intArrayOf(android.R.attr.state_pressed), shape(acc))
        sl.addState(intArrayOf(), shape(if (active) lk.accent else keyC))
        return sl
    }

    private fun commit(s: String) { currentInputConnection?.commitText(s, 1) }
    private fun put(s: String) { flush(); commit(s) }
    private fun punct(c: String) { flush(); commit(if (lang == "ja") JP[c] ?: c else c) }

    private fun del() {
        if (rb.isNotEmpty()) { rb = rb.dropLast(1); showComposing() }
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
    }

    private fun enter() {
        flush()
        val ei = currentInputEditorInfo ?: return
        val a = ei.imeOptions and EditorInfo.IME_MASK_ACTION
        if ((ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0 &&
            a != EditorInfo.IME_ACTION_NONE && a != EditorInfo.IME_ACTION_UNSPECIFIED) sendDefaultEditorAction(true)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun cycleLang() {
        flush()
        val i = Cfg.LANGS.indexOfFirst { it.first == lang }
        p.edit().putString("lang", Cfg.LANGS[(i + 1) % Cfg.LANGS.size].first).apply()
        render()
    }

    private fun type(c: Char) {
        val up = shift || caps
        when (lang) {
            "ko" -> hangul((if (shift && KS.containsKey(c)) KS[c] else KR[c])!!)
            "ja" -> if (up) { flush(); commit(c.uppercaseChar().toString()) } else roma(c)
            else -> commit((if (up) c.uppercaseChar() else c).toString())
        }
        if (shift && !caps) { shift = false; render() }
    }

    private fun showComposing() {
        val ic = currentInputConnection ?: return
        if (rb.isEmpty()) { ic.setComposingText("", 1); ic.finishComposingText() } else ic.setComposingText(rb, 1)
    }

    private fun flush() {
        if (rb.isEmpty()) return
        val s = if (rb == "n") "ん" else rb
        rb = ""
        val ic = currentInputConnection ?: return
        ic.beginBatchEdit(); ic.setComposingText("", 1); ic.finishComposingText(); ic.commitText(s, 1); ic.endBatchEdit()
    }

    private fun roma(c: Char) {
        val ic = currentInputConnection ?: return
        rb += c
        ic.beginBatchEdit(); ic.setComposingText("", 1); ic.finishComposingText()
        while (rb.isNotEmpty()) {
            val k = ROMA[rb]
            if (k != null) { ic.commitText(k, 1); rb = ""; break }
            if (rb.length > 1 && rb[0] == rb[1] && "aiueon".indexOf(rb[0]) < 0) { ic.commitText("っ", 1); rb = rb.substring(1); continue }
            if (rb[0] == 'n' && rb.length > 1 && "aiueoyn".indexOf(rb[1]) < 0) { ic.commitText("ん", 1); rb = rb.substring(1); continue }
            if (ROMA.keys.any { it.startsWith(rb) }) break
            ic.commitText(rb.substring(0, 1), 1); rb = rb.substring(1)
        }
        if (rb.isNotEmpty()) ic.setComposingText(rb, 1)
        ic.endBatchEdit()
    }

    private fun mk(l: Int, v: Int, t: Int): Char = (0xAC00 + l * 588 + v * 28 + t).toChar()

    private fun hangul(j: Char) {
        val ic = currentInputConnection ?: return
        val last = ic.getTextBeforeCursor(1, 0)?.firstOrNull()
        val isV = VOW.indexOf(j) >= 0
        var rep: String? = null
        if (last != null && last.code in 0xAC00..0xD7A3) {
            val code = last.code - 0xAC00
            val l = code / 588
            val v = (code % 588) / 28
            val t = code % 28
            if (isV) {
                if (t == 0) {
                    CV["" + VOW[v] + j]?.let { rep = mk(l, VOW.indexOf(it[0]), 0).toString() }
                } else {
                    val tc = TAIL[t]
                    val sp = TS[tc]
                    val keep = sp?.first ?: ""
                    val mv = sp?.second ?: tc
                    rep = "" + mk(l, v, TAIL.indexOf(keep)) + mk(LEAD.indexOf(mv[0]), VOW.indexOf(j), 0)
                }
            } else {
                if (t == 0) {
                    val ti = TAIL.indexOf(j.toString())
                    if (ti > 0) rep = mk(l, v, ti).toString()
                } else {
                    CT[TAIL[t] + j]?.let { rep = mk(l, v, TAIL.indexOf(it)).toString() }
                }
            }
        } else if (last != null && isV && LEAD.indexOf(last) >= 0) {
            rep = mk(LEAD.indexOf(last), VOW.indexOf(j), 0).toString()
        }
        if (rep != null) { ic.deleteSurroundingText(1, 0); ic.commitText(rep, 1) } else ic.commitText(j.toString(), 1)
    }
}
