package com.example.randomclicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
    private lateinit var clickLimitInput: EditText
    private lateinit var statsText: TextView
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001

        private const val DEFAULT_MIN_INTERVAL = 500L
        private const val DEFAULT_MAX_INTERVAL = 700L
        private const val DEFAULT_CLICK_LIMIT = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        createUI()
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()

        if (::statsText.isInitialized) {
            refreshStatistics()
        }
    }

    private fun createUI() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
        }

        // =========================
        // TITLE
        // =========================

        val title = TextView(this).apply {
            text = "Randomized Alternating Auto Clicker"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // =========================
        // DESCRIPTION
        // =========================

        val description = TextView(this).apply {
            text = """
                Two floating targets: ① and ②
                
                Each click randomly selects ONE target.
                The delay between clicks is randomized
                between the minimum and maximum interval.
            """.trimIndent()

            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 25)
        }

        root.addView(
            description,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // =========================
        // MINIMUM INTERVAL
        // =========================

        val minLabel = TextView(this).apply {
            text = "Minimum Interval (ms)"
            textSize = 16f
        }

        root.addView(minLabel)

        minIntervalInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Example: 500"

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
            ).apply {
                bottomMargin = 15
            }
        )

        // =========================
        // MAXIMUM INTERVAL
        // =========================

        val maxLabel = TextView(this).apply {
            text = "Maximum Interval (ms)"
            textSize = 16f
        }

        root.addView(maxLabel)

        maxIntervalInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Example: 700"

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
            ).apply {
                bottomMargin = 15
            }
        )

        // =========================
        // CLICK LIMIT
        // =========================

        val limitLabel = TextView(this).apply {
            text = "Click Limit (0 = Unlimited)"
            textSize = 16f
        }

        root.addView(limitLabel)

        clickLimitInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Example: 500   |   0 = Unlimited"

            setText(
                prefs.getLong(
                    "click_limit",
                    DEFAULT_CLICK_LIMIT
                ).toString()
            )
        }

        root.addView(
            clickLimitInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        )

        // =========================
        // SAVE SETTINGS BUTTON
        // =========================

        val saveButton = Button(this).apply {
            text = "SAVE SETTINGS"

            setOnClickListener {
                saveSettings()
            }
        }

        root.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // =========================
        // STATISTICS TITLE
        // =========================

        val statsTitle = TextView(this).apply {
            text = "CLICK STATISTICS"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 15)
        }

        root.addView(
            statsTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // =========================
        // STATISTICS
        // =========================

        statsText = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }

        root.addView(
            statsText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // =========================
        // ACCESSIBILITY SETTINGS
        // =========================

        val accessibilityButton = Button(this).apply {
            text = "OPEN ACCESSIBILITY SETTINGS"

            setOnClickListener {
                try {
                    startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to open Accessibility Settings",
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
                topMargin = 20
            }
        )

        // =========================
        // OVERLAY SETTINGS
        // =========================

        val overlayButton = Button(this).apply {
            text = "OPEN OVERLAY SETTINGS"

            setOnClickListener {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (!Settings.canDrawOverlays(this@MainActivity)) {

                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            )

                            intent.data =
                                android.net.Uri.parse(
                                    "package:$packageName"
                                )

                            startActivity(intent)

                        } catch (e: Exception) {

                            startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                                )
                            )
                        }

                    } else {

                        Toast.makeText(
                            this@MainActivity,
                            "Overlay permission already enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "Overlay permission is not required on this Android version",
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
            )
        )

        setContentView(root)

        // Initial statistics
        refreshStatistics()
    }

    // ============================================================
    // SAVE SETTINGS
    // ============================================================

    private fun saveSettings() {

        val minValue = minIntervalInput.text
            .toString()
            .trim()
            .toLongOrNull()

        val maxValue = maxIntervalInput.text
            .toString()
            .trim()
            .toLongOrNull()

        val clickLimitValue = clickLimitInput.text
            .toString()
            .trim()
            .toLongOrNull()

        // Check minimum interval
        if (minValue == null) {
            Toast.makeText(
                this,
                "Enter a valid minimum interval",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Check maximum interval
        if (maxValue == null) {
            Toast.makeText(
                this,
                "Enter a valid maximum interval",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Check click limit
        if (clickLimitValue == null) {
            Toast.makeText(
                this,
                "Enter a valid click limit",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Minimum interval validation
        if (minValue < 50L) {
            Toast.makeText(
                this,
                "Minimum interval must be at least 50 ms",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Maximum cannot be smaller than minimum
        if (maxValue < minValue) {
            Toast.makeText(
                this,
                "Maximum interval cannot be smaller than minimum interval",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Click limit cannot be negative
        if (clickLimitValue < 0L) {
            Toast.makeText(
                this,
                "Click limit cannot be negative",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Save everything
        prefs.edit()
            .putLong("min_interval_ms", minValue)
            .putLong("max_interval_ms", maxValue)
            .putLong("click_limit", clickLimitValue)
            .apply()

        refreshStatistics()

        val limitText =
            if (clickLimitValue == 0L) {
                "Unlimited"
            } else {
                clickLimitValue.toString()
            }

        Toast.makeText(
            this,
            "Settings saved\n$minValue - $maxValue ms\nLimit: $limitText",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============================================================
    // REFRESH STATISTICS
    // ============================================================

    private fun refreshStatistics() {

        if (!::statsText.isInitialized) {
            return
        }

        // Counting data is saved by ClickAccessibilityService
        val countPrefs =
            getSharedPreferences("counting", MODE_PRIVATE)

        val totalClicks =
            countPrefs.getLong("total_clicks", 0L)

        val target1Clicks =
            countPrefs.getLong("target1_clicks", 0L)

        val target2Clicks =
            countPrefs.getLong("target2_clicks", 0L)

        val clickLimit =
            prefs.getLong(
                "click_limit",
                DEFAULT_CLICK_LIMIT
            ).coerceAtLeast(0L)

        val remaining =
            if (clickLimit == 0L) {
                "Unlimited"
            } else {
                (clickLimit - totalClicks)
                    .coerceAtLeast(0L)
                    .toString()
            }

        val status = when {
            clickLimit > 0L && totalClicks >= clickLimit ->
                "LIMIT REACHED"

            totalClicks > 0L ->
                "DATA SAVED"

            else ->
                "READY"
        }

        val limitText =
            if (clickLimit == 0L) {
                "Unlimited"
            } else {
                clickLimit.toString()
            }

        statsText.text = """
            Total Clicks: $totalClicks
            
            Target ① Clicks: $target1Clicks
            
            Target ② Clicks: $target2Clicks
            
            Click Limit: $limitText
            
            Remaining: $remaining
            
            Status: $status
        """.trimIndent()
    }

    // ============================================================
    // NOTIFICATION PERMISSION
    // ============================================================

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
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

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(
                    this,
                    "Notification permission enabled",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Notification permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
