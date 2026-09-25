# Randomized Alternating Auto Clicker

Android app with exactly two floating target buttons.

## Behavior
- Target **1** and Target **2** can be dragged to any screen position while stopped.
- Start/Stop is controlled from the Android notification.
- Every tap independently chooses Target 1 or Target 2 randomly.
- There is **no fixed click pattern**. For example: `1,2,1,1,2,2,2,1,1`.
- Only one target is selected for each click.
- **Click interval is NOT random.** You set one fixed interval yourself.
- Examples: `500 ms` = 0.5 sec, `1000 ms` = 1 sec, `2000 ms` = 2 sec.
- Minimum allowed interval is 50 ms.

## How to set it
Open the app → enter the interval in milliseconds → **Save Fixed Interval** → then start the auto clicker from the notification.

## Requirements
- Android 8.0+ (API 26+)
- Accessibility Service permission
- Display-over-other-apps permission

## Build
Open the project in Android Studio and use **Build > Build APK(s)**.
