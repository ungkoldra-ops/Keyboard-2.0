# Typewriter Keyboard (Android)

A real Android keyboard (input method) that works in any app.

## Build and install
1. Install Android Studio (Koala or newer).
2. File > Open, choose this TypewriterKeyboard folder, and let Gradle sync. If it asks for a Gradle location, accept the suggestion.
3. Connect your phone (USB debugging on) or start an emulator, then press Run.
4. On the phone, open the "Typewriter Keyboard" app:
   - Tap "1. Enable the keyboard" and switch on Typewriter Keyboard.
   - Tap "2. Switch to this keyboard" and select it.
5. Change theme, language, key height, number row, transparent keys and your own keyboard picture in the same app.

## Features
QWERTY layout for every language, Shift (double tap for Caps), long press the top row for digits,
?123 symbols page, emoji page, globe key to switch English / Filipino / Japanese (romaji to hiragana) /
Korean (letters combine into syllables), themes, custom colors, keyboard picture with transparent keys.

## Not included yet
Speech to text, emoji search and GIF stickers (these need extra permissions or libraries).
No app icon is set, so Android shows a default one.

## Build an APK without Android Studio
1. Create a free GitHub account and a new repository.
2. Upload everything in this folder (including the hidden .github folder) to the repository.
3. Open the repository's Actions tab, run "Build APK" (it also runs on every upload).
4. When it finishes, open the run and download the "TypewriterKeyboard-apk" file. Unzip it to get app-debug.apk.
5. Copy app-debug.apk to your phone, open it, and allow "install unknown apps" when asked.
