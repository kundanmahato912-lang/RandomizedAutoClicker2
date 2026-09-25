# Build this project on Codemagic

This project is configured to build a debug APK without requiring a `gradle/` wrapper folder in the Git repository.

1. Upload the contents of this project to a GitHub repository.
2. Connect the repository to Codemagic.
3. Select the workflow:
   `android-debug`
4. Start the build.
5. Download the APK from the build's Artifacts section.

The build command is:

    gradle assembleDebug --no-daemon

The generated APK is:

    app/build/outputs/apk/debug/app-debug.apk

For personal installation on an Android phone, a debug APK is sufficient. You do not need a Play Store signing setup.

App behavior:
- Two draggable target buttons: 1 and 2.
- Each click randomly chooses exactly one target.
- Click interval is fixed and user-configurable in milliseconds.
- Start/Stop is available from the notification.
