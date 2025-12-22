# Notes Category Classifier (Android + Kotlin + TFLite-ready)

A simple Android application where a user can enter a note, press the `+` button, and the note is added to the screen. In the future, each note will be automatically assigned a category (classification) using an on-device machine learning (ML) model via TensorFlow Lite (TFLite).

---

## Features (MVP)

- **Navigation Drawer**: A side menu with placeholder items.
- **Floating Action Button (FAB)**: The `+` button at the bottom right to add a new note to the UI.
- **Two-Column Layout**: Notes are added dynamically into left and right columns.
- **ML Foundation**: The structure is ready for the integration of a text classification model.
``
---

## Tech Stack

- Android Studio
- Kotlin
- XML Layouts (DrawerLayout + ConstraintLayout)
- Material Components (`com.google.android.material`)
- (Planned) TensorFlow Lite (TFLite)

---

## Project Structure (Key Files)

- `app/src/main/java/.../MainActivity.kt` — Handles UI logic, the navigation drawer, and adding notes.
- `app/src/main/res/layout/mainlayout.xml` — Defines the main screen layout (DrawerLayout + ScrollView + FAB).
- `app/src/main/res/menu/drawer_menu.xml` — Contains the menu items for the navigation drawer (e.g., sorting stubs).
- `app/src/main/res/values/strings.xml` — Application strings.

---

## Setup / Run

1.  Open the project in Android Studio.
2.  Ensure you have the Android SDK installed and an emulator or physical device is available.
3.  Click **Run** ▶️.

---

## Dependencies

The Material Components library is required for `NavigationView` and `FloatingActionButton` to work correctly.

### build.gradle (Module: app)
If you are not using a Version Catalog, add the dependency as follows:
```gradle
dependencies {
    implementation("com.google.android.material:material:1.12.0")
}
```
