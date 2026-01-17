# RSVP Fast Read

A privacy-focused Rapid Serial Visual Presentation (RSVP) reading application for Android. Improve your reading efficiency by processing text one word at a time at high speeds.

## Features

### 🚀 High-Performance Reading
*   **Speed Control**: Read from 100 to 1000 words per minute (WPM).
*   **Optimal Recognition Point (ORP)**: Centered visual anchor (adjustable color) to minimize eye movement.
*   **Bionic Reading Mode**: Bold word prefixes for faster anchoring.
*   **Smart Punctuation**: Natural delays for periods, commas, and paragraph breaks.
*   **Warm-up & Countdown**: Gradual speed ramp-up and 3-2-1 timer for focus.

### 📖 Document Support
*   **Formative Support**: PDF, EPUB, HTML, and TXT files.
*   **Logical Extraction**: logical reading order for PDFs, ignoring headers/footers.
*   **CJK Support**: Specialized word segmentation for Chinese, Japanese, and Korean.
*   **Import Options**: System file picker (SAF), direct Clipboard import, and "Share to" support from other apps.

### 🛠 Advanced Navigation
*   **Split View Reader**: Simultaneous RSVP reading and scrollable context view.
*   **Tap-to-Resume**: Pause the reader to scroll through the full text and tap any word to resume.
*   **Chapters & Bookmarks**: Navigate by document structure or save your own manual spots.
*   **Search**: Real-time search within any document to jump to specific phrases.

### 📊 Productivity & UI
*   **Reading Goals**: Track daily words read and minutes spent locally.
*   **Focus Timer**: Built-in session timer to prevent eye strain.
*   **Immersive Mode**: Automatically hides system bars during reading for zero distraction.
*   **Customization**: Dark/Light mode, font size (16-72sp), and font family selection (including OpenDyslexic support).

## Privacy & Freedom

*   **100% Offline**: Zero internet permissions. Your data never leaves your device.
*   **No Analytics**: No trackers, no beacons, no reporting.
*   **Open Source**: Built exclusively using free and open-source components.

## Building from Source

This project uses the standard Gradle build system.

1.  Clone the repository.
2.  Open in Android Studio or use the command line:
    ```bash
    ./gradlew assembleDebug
    ```
3.  The APK will be generated in `app/build/outputs/apk/debug/`.

## License

Copyright (C) 2023 Unchained

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the [LICENSE](LICENSE) file for details.
