package com.example.randomclicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var minIntervalInput: EditText
    private lateinit var maxIntervalInput: EditText
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
        private const val DEFAULT_MIN_INTERVAL = 500L
        private const val DEFAULT_MAX_INTERVAL = 700L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )

        createUI()

        requestNotificationPermission()
    }

    private fun createUI() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 50, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Randomized Alternating Auto Clicker"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val description = TextView(this).apply {
            text =
                "Randomly click Target 1 or Target 2.\n" +
                "Interval will also be random between Min and Max."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }

        root.addView(
            description,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // MINIMUM INTERVAL
        val minLabel = TextView(this).apply {
            text = "Minimum interval (milliseconds)"
            textSize = 16f
        }

        root.addView(
            minLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        minIntervalInput = EditText(this).apply {
            hint = "Example: 500"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            setText(
                prefs.getLong(
                    "min_interval_ms",
                    DEFAULT_MIN_INTERVAL
                ).toString()
            )
        }

        root.addView(
            minIntervalInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // MAXIMUM INTERVAL
        val maxLabel = TextView(this).apply {
            text = "Maximum interval (milliseconds)"
            textSize = 16f
        }

        root.addView(
            maxLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        )

        maxIntervalInput = EditText(this).apply {
            hint = "Example: 700"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            setText(
                prefs.getLong(
                    "max_interval_ms",
                    DEFAULT_MAX_INTERVAL
                ).toString()
            )
        }

        root.addView(
            maxIntervalInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // SAVE BUTTON
        val saveButton = Button(this).apply {
            text = "SAVE INTERVAL"

            setOnClickListener {
                saveIntervals()
            }
        }

        root.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 25
            }
        )

        // ACCESSIBILITY SETTINGS
        val accessibilityButton = Button(this).apply {
            text = "OPEN ACCESSIBILITY SETTINGS"

            setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to open Accessibility settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        root.addView(
            accessibilityButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 15
            }
        )

        // OVERLAY SETTINGS
        val overlayButton = Button(this).apply {
            text = "OPEN OVERLAY SETTINGS"

            setOnClickListener {
                try {
                    val intent =
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                        )

                    intent.data =
                        android.net.Uri.parse(
                            "package:$packageName"
                        )

                    startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Overlay settings unavailable",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        root.addView(
            overlayButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 15
            }
        )

        setContentView(root)
    }

    private fun saveIntervals() {

        val minValue =
            minIntervalInput.text
                .toString()
                .trim()
                .toLongOrNull()

        val maxValue =
            maxIntervalInput.text
                .toString()
                .trim()
                .toLongOrNull()

        if (minValue == null || maxValue == null) {

            Toast.makeText(
                this,
                "Please enter valid numbers",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (minValue < 50L) {

            Toast.makeText(
                this,
                "Minimum interval cannot be below 50 ms",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (maxValue < minValue) {

            Toast.makeText(
                this,
                "Maximum interval must be greater than or equal to Minimum",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        prefs.edit()
            .putLong(
                "min_interval_ms",
                minValue
            )
            .putLong(
                "max_interval_ms",
                maxValue
            )
            .apply()

        Toast.makeText(
            this,
            "Interval saved: $minValue - $maxValue ms",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode ==
            NOTIFICATION_PERMISSION_REQUEST
        ) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(
                    this,
                    "Notifications enabled",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Notification permission is required for START/STOP notification",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
