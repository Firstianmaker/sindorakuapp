# SINDORAKU

SINDORAKU is an Android application for recognizing a limited vocabulary of BISINDO (Bahasa Isyarat Indonesia) gestures through the device camera and exploring word meanings and gesture examples. The interface is in Indonesian, and recognition runs on the device using bundled MediaPipe and TensorFlow Lite models.

The current implementation supports **15 words**. It collects recognized words into a text sequence; it does not implement unrestricted sign-language translation or grammatical sentence generation.

## Features

- Live camera recognition with hand bounding-box overlays.
- Front and rear camera switching.
- Prediction stabilization before a word is added to the displayed text.
- Gesture-release detection between words and a reset control to clear the result.
- A searchable vocabulary list with category filters.
- Word detail pages with meanings, local WebP gesture examples, and related words.
- Home screen with BISINDO information and usage tips.
- Local inference and bundled vocabulary assets, with no backend configuration required.

## Supported vocabulary

| Category | Words |
| --- | --- |
| Umum | Ada, Kapan, Masalah |
| Permintaan | Bantuan, Boleh, Butuh, Tolong |
| Orang | Kamu, Saya, Siapa |
| Ekspresi | Maaf, Terimakasih |
| Aktivitas | Makan, Mau, Pergi |

The model output order is defined in [`labels.txt`](app/src/main/assets/labels.txt). Display labels, meanings, and categories are defined in [`BisindoVocabulary.kt`](app/src/main/java/com/example/sindoraku/ui/vocabulary/BisindoVocabulary.kt).

## Technology and configuration

| Component | Configuration in this repository |
| --- | --- |
| Language | Kotlin 1.9.24 |
| Interface | Jetpack Compose, Material 3, Compose BOM 2024.02.01 |
| Navigation | Navigation Compose 2.7.6 |
| Camera | CameraX 1.3.3 |
| Hand landmarks | MediaPipe Tasks Vision 0.10.14 |
| Model inference | TensorFlow Lite 2.16.1, Support 0.4.4, Select TF Ops 2.16.1 |
| Image loading | Coil 2.6.0 |
| Android Gradle Plugin | 8.3.2 |
| Gradle wrapper | 8.10.2 |
| Minimum Android version | Android 7.0 / API 24 |
| Compile / target SDK | 35 / 35 |
| Packaged native ABIs | `arm64-v8a`, `armeabi-v7a` |
| Application ID | `com.example.sindoraku` |
| App version | 1.0 (`versionCode = 1`) |

These values describe the checked-in configuration, not a verified build compatibility matrix. In particular, the project uses compile SDK 35 with AGP 8.3.2, while the [AGP 8.3 documentation](https://developer.android.com/build/releases/agp-8-3-0-release-notes) lists API 34 as its maximum supported API level. This combination can produce a compatibility warning and should be reviewed when maintaining the build.

## Getting started

### Requirements

- Android Studio with support for this project's Gradle configuration.
- JDK 17 to run Gradle. The Java/Kotlin compilation target is 11, which is separate from the Gradle runtime requirement.
- Android SDK Platform 35 and the SDK Build Tools requested during Gradle sync.
- An ARM Android device running API 24 or newer, with a camera. The current ABI filters do not package x86/x86_64 native libraries.
- Internet access for the initial Gradle and dependency downloads.

### Open and run

1. Clone the repository:

   ```bash
   git clone https://github.com/Firstianmaker/sindorakuapp.git
   cd sindorakuapp
   ```

2. Open the repository root in Android Studio, selecting the folder containing `settings.gradle.kts`.
3. Configure the Android SDK location when prompted and select JDK 17 as the Gradle JDK.
4. Let Gradle sync finish and install any requested SDK packages.
5. Connect a compatible Android device with USB debugging enabled, or use a compatible ARM emulator with camera support.
6. Select the `app` run configuration and run the application.
7. Grant camera permission when opening detection.

The SDK path belongs in the local `local.properties` file and should not be committed. The models and gesture examples are already tracked in `app/src/main/assets/`; no API key or model download step is configured by the application.

### Build a debug APK

From the project root, on Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
sh ./gradlew assembleDebug
```

The standard debug output path is `app/build/outputs/apk/debug/app-debug.apk`. A distributable release requires a separate signing setup; no release signing configuration is defined in the app build file.

## Using the app

1. Open the vocabulary list to browse or search for one of the supported words.
2. Open a word to view its meaning and gesture example.
3. Open the camera detection screen and allow camera access.
4. Keep your hands visible and perform a supported gesture. The app needs a sequence of sampled frames before it can produce a stable result.
5. After a word is added, move your hands out of view until the app is ready for the next gesture.
6. Repeat to collect more words, or use the reset button to clear the text and detection state.

Use sufficient lighting and keep the hands inside the camera view. Recognition quality and processing speed depend on the gesture, framing, and device.

## Recognition pipeline

```text
CameraX camera frames
  -> MediaPipe hand detection and landmarks
  -> Masked hand region + landmark coordinates
  -> TensorFlow Lite image feature extractor
  -> Combined image and landmark features
  -> Temporal gesture classifier
  -> Prediction voting and UI stabilization
  -> Recognized words displayed as text
```

The implementation is split between `HandLandmarkerHelper` and `BisindoClassifier`:

1. Camera analysis keeps the latest frame, and the helper samples at a configured minimum interval of 200 ms.
2. MediaPipe detects up to two hands. Their 21 landmarks per hand supply 84 x/y values; unused hand slots remain zero-filled.
3. The helper creates a masked hand region. The classifier resizes it to 224 × 224 RGB pixels and normalizes pixel values to `[-1, 1]`.
4. `feature_extractor.tflite` produces 256 image features, which are joined with the 84 landmark values to form 340 features per frame.
5. `bisindo_model.tflite` receives a sequence shaped `[1, 20, 340]`. After the first full sequence, inference is scheduled every two processed frames.
6. The classifier requires at least three agreeing predictions within a rolling window of up to five predictions. The camera UI then requires two consecutive matching callbacks before appending a word.
7. The UI waits for three no-hand callbacks before accepting the next gesture.

These are code settings, not measured latency or accuracy results. The classifier's confidence threshold is currently `0.0`; voting stabilizes the output but does not provide reliable rejection of gestures outside the supported vocabulary.

### Bundled model assets

| File | Purpose |
| --- | --- |
| `hand_landmarker.task` | MediaPipe hand detection and landmark model |
| `feature_extractor.tflite` | Image feature extraction |
| `bisindo_model.tflite` | Classification of temporal feature sequences |
| `labels.txt` | Class labels in model output order |
| `gestures/<word-id>.webp` | Vocabulary gesture examples |

`BisindoClassifier` validates the TensorFlow Lite input/output shapes and float32 types at initialization. When replacing models, preserve the expected dimensions and label ordering, or update the classifier accordingly. Adding a vocabulary entry alone does not train the model to recognize a new word.

## Project structure

```text
app/
  build.gradle.kts                 App configuration and dependencies
  src/main/
    AndroidManifest.xml           App entry point and camera permission
    assets/                       Models, labels, and gesture examples
    java/com/example/sindoraku/
      MainActivity.kt             Activity entry point
      ml/                         Landmark processing and classification
      navigation/                 Compose screen routes
      ui/components/              Shared UI and camera preview
      ui/screens/                 Splash, home, detection, vocabulary, detail
      ui/theme/                   Colors, typography, and design tokens
      ui/vocabulary/              Word metadata and filtering
    res/                          Fonts, icons, strings, and Android resources
  src/test/                       Example JVM unit test
  src/androidTest/                Example instrumentation test
gradle/
  libs.versions.toml               Version catalog
  wrapper/                        Gradle wrapper files
build.gradle.kts                  Root plugin configuration
settings.gradle.kts               Modules and dependency repositories
DESIGN.md                         Design reference notes
```

The `HandLandmarkerHelper` class currently lives in the file `app/src/main/java/com/example/sindoraku/ml/HandLandmarkerHelper (2).kt`.

## Permissions and data handling

The manifest declares camera permission. Inference reads the bundled models and processes camera frames locally; no Internet permission or server upload implementation is present. Debug frame saving is disabled by `DEBUG_SAVE_FRAMES = false` in the helper. Performance logging remains in the code under the Logcat tag `Performa`.

The accumulated text is held in Compose state; the current implementation does not provide a persistent detection history or export feature.

## Verification and limitations

The repository contains Android Studio example tests for arithmetic and the application package name. They do not evaluate gesture recognition or the camera workflow.

To run the existing local checks on Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

With a compatible device connected, run the instrumentation test with:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

For manual verification, check camera permission handling, both camera directions, hand overlays, word accumulation, gesture release, reset, vocabulary filtering, and gesture example playback. Animated WebP playback should be checked across the supported Android versions because the detail screen selects different decoders below and above API 28.

Training notebooks, dataset descriptions, evaluation reports, and thesis documents are not included in the inspected project files. Model architecture details beyond the runtime interfaces, dataset provenance, accuracy, and measured performance therefore remain undocumented here. This README was prepared from source inspection; the build and device tests were not executed as part of writing it.
