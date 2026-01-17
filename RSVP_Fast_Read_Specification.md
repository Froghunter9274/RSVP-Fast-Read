# RSVP-Fast-Read Project Specification

## 1. Overview
A privacy-focused Rapid Serial Visual Presentation (RSVP) reading application for Android. The app extracts text from various file formats and displays it one word at a time at high speeds to improve reading efficiency.

## 2. Core Functional Requirements
### 2.1 File Support & Extraction
*   **Formats**: EPUB, PDF, TXT, HTML.
*   **PDF Extraction**: Logical reading order (multi-column support), ignores headers/footers via configurable content margins.
*   **Non-text Elements**: Images, tables, and footnotes are skipped.
*   **Encoding**: Auto-detect with manual override for TXT/HTML.
*   **Language Support**: Manual per-document language selection. Supports LTR, RTL, and CJK word segmentation.

### 2.2 RSVP Reader Engine
*   **Display**: Single word at a time, centered horizontally and vertically.
*   **Optimal Recognition Point (ORP)**: Red-colored character fixed at a specific "focus point" position.
*   **Speed (WPM)**: 100 – 1000 WPM (Default: 300, Increment: 10).
*   **Font Size**: 16sp – 72sp (Default: 32sp, Increment: 4sp).
*   **Fonts**: Sans-Serif, Serif, Monospace, OpenDyslexic. Normalized to single style (no bold/italics).
*   **Delays**: 
    *   2.0x delay for `. ? !`
    *   1.5x delay for `, ;`
    *   Extended delay for paragraph breaks.
*   **Word Handling**: Long words split into two parts with a hyphen.
*   **Warm-up**: Gradual WPM ramp-up upon starting playback.
*   **Countdown**: 3-2-1 timer before playback resumes.

### 2.3 Controls & Navigation
*   **Gestures**:
    *   Left-half vertical swipe: Adjust font size.
    *   Right-half vertical swipe: Adjust WPM.
    *   Center tap: Play/Pause.
*   **Playback Controls**: 10-word Rewind/Skip, progress bar, chapter selection.
*   **Hardware**: Supports Bluetooth media buttons (Play/Pause/Skip).
*   **Zen Mode**: UI elements (buttons, bars) auto-hide during playback.
*   **Search**: Search within document with "jump to phrase" capability.
*   **Bookmarks**: Manual bookmark support.

### 2.4 Library & Persistence
*   **View**: Folder-based categorization, search functionality, cover art display.
*   **Persistence**: Automatically saves last read position (word/file) per document.
*   **Summary**: Post-reading statistics (Total time, Avg WPM, Total words).
*   **Import**: System Storage Access Framework (SAF), clipboard import, and "Share to" intent support.

## 3. User Interface
*   **Theming**: Dark mode by default with a manual light mode toggle.
    *   Dark: White text on dark background.
    *   Light: Dark text on light background.
*   **Display Management**: Keep screen on during playback. Support screen rotation.
*   **Distraction Reduction**: Focus mask (dimmed background) and adjustable background opacity.
*   **Indicators**: Progress shown as both percentage and estimated time remaining.
*   **Navigation**: Dedicated back button to return to Library.

## 4. Privacy & Technical Constraints
*   **Connectivity**: Strictly offline. No internet permissions or connections.
*   **Permissions**: Minimal permissions required (e.g., no unnecessary background or location access).
*   **Libraries**: Only open-source and free-of-charge modules/libraries.
*   **TTS**: Offline-only Text-To-Speech synchronized with RSVP stream.
*   **Data**: No export of statistics or library data (local only).
*   **Normalization**: Strip URLs and specific special characters before streaming.
