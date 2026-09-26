package com.example.randomclicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var minIntervalInput: EditText
    private lateinit var maxIntervalInput: EditText
    private lateinit var clickLimitInput: EditText

    private lateinit var totalText: TextView
    private lateinit var target1Text: TextView
    private lateinit var target2Text: TextView
    private lateinit var limitText: TextView
    private lateinit var remainingText: TextView
    private lateinit var statusText: TextView
    private lateinit var progressText: TextView

    private lateinit var prefs: SharedPreferences

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001

        private const val DEFAULT_MIN_INTERVAL = 500L
        private const val DEFAULT_MAX_INTERVAL = 700L
        private const val DEFAULT_CLICK_LIMIT = 0L
    }

    // Colors
    private val blue = Color.rgb(25, 118, 255)
    private val darkBlue = Color.rgb(25, 45, 75)
    private val green = Color.rgb(25, 175, 80)
    private val red = Color.rgb(230, 60, 70)
    private val purple = Color.rgb(105, 75, 210)
    private val pink = Color.rgb(235, 70, 130)
    private val orange = Color.rgb(235, 145, 35)
    private val backgroundColor = Color.rgb(245, 248, 253)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )

        createUI()
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()

        if (::totalText.isInitialized) {
            refreshStatistics()
        }
    }

    // ============================================================
    // CREATE UI
    // ============================================================

    private fun createUI() {

        val scrollView = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(16, 50, 16, 30)
        }

        scrollView.addView(root)

        // ========================================================
        // HEADER
        // ========================================================

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 38, 20, 22)

            background = roundedBackground(
                Color.rgb(25, 115, 240),
                24
            )
        }

        val title = TextView(this).apply {
            text = "Random Auto Clicker"
            textSize = 23f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        header.addView(title)

        val subtitle = TextView(this).apply {
            text = "Randomly click Target 1 or Target 2"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 6, 0, 15)
        }

        header.addView(subtitle)

        val featureRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        featureRow.addView(
            featureBox("●", "Random\nTarget"),
            weightParams()
        )

        featureRow.addView(
            featureBox("◷", "Random\nInterval"),
            weightParams()
        )

        featureRow.addView(
            featureBox("⚡", "Auto Stop\nLimit"),
            weightParams()
        )

        header.addView(featureRow)

        root.addView(
            header,
            marginParams(0, 0, 0, 14)
        )

        // ========================================================
        // STATUS / TARGET CARD
        // ========================================================

        val statusCard = card()

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val readyIcon = TextView(this).apply {
            text = "▶"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = circleBackground(green)
        }

        statusRow.addView(
            readyIcon,
            fixedParams(58, 58, 0, 0, 12, 0)
        )

        val statusInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        statusText = TextView(this).apply {
            text = "READY"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }

        statusInfo.addView(statusText)

        val statusDescription = TextView(this).apply {
            text = "Start and stop from the floating control."
            textSize = 13f
            setTextColor(darkBlue)
        }

        statusInfo.addView(statusDescription)

        statusRow.addView(
            statusInfo,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        statusRow.addView(
            targetView("1", blue, "Target 1"),
            fixedParams(65, 75, 4, 0, 3, 0)
        )

        statusRow.addView(
            targetView("2", pink, "Target 2"),
            fixedParams(65, 75, 3, 0, 0, 0)
        )

        statusCard.addView(statusRow)

        root.addView(
            statusCard,
            marginParams(0, 0, 0, 14)
        )

        // ========================================================
        // QUICK PRESETS
        // ========================================================

        val presetCard = card()

        presetCard.addView(
            sectionTitle("⚡  Quick Presets")
        )

        val presetRow1 = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
}

val presetRow2 = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
}

val fastButton = presetButton(
    "FAST",
    "100 - 200 ms"
)

val normalButton = presetButton(
    "NORMAL",
    "500 - 700 ms"
)

val slowButton = presetButton(
    "SLOW",
    "2500 - 2700 ms"
)

val customButton = presetButton(
    "CUSTOM",
    "Manual"
)

fastButton.setOnClickListener {
    setInterval(100, 200)
}

normalButton.setOnClickListener {
    setInterval(500, 700)
}

slowButton.setOnClickListener {
    setInterval(2500, 2700)
}

customButton.setOnClickListener {
    Toast.makeText(
        this,
        "Enter your own interval below",
        Toast.LENGTH_SHORT
    ).show()
}

presetRow1.addView(
    fastButton,
    weightParams()
)

presetRow1.addView(
    normalButton,
    weightParams()
)

presetRow2.addView(
    slowButton,
    weightParams()
)

presetRow2.addView(
    customButton,
    weightParams()
)

presetCard.addView(presetRow1)

presetCard.addView(
    presetRow2,
    marginParams(0, 6, 0, 0)
)

        root.addView(
            presetCard,
            marginParams(0, 0, 0, 14)
        )

        // ========================================================
        // CLICK SETTINGS
        // ========================================================

        val settingsCard = card()

        settingsCard.addView(
            sectionTitle("⚙  Click Settings")
        )

        val minBox = inputBox(
            "Minimum Interval (ms)",
            "500",
            "min"
        )

        val maxBox = inputBox(
            "Maximum Interval (ms)",
            "700",
            "max"
        )

        val limitBox = inputBox(
            "Click Limit",
            "0 = Unlimited",
            "limit"
        )

        minIntervalInput = minBox.second
        maxIntervalInput = maxBox.second
        clickLimitInput = limitBox.second

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        inputRow.addView(
            minBox.first,
            weightParams()
        )

        inputRow.addView(
            maxBox.first,
            weightParams()
        )

        inputRow.addView(
            limitBox.first,
            weightParams()
        )

        settingsCard.addView(inputRow)

        // Load saved values

        minIntervalInput.setText(
            prefs.getLong(
                "min_interval_ms",
                DEFAULT_MIN_INTERVAL
            ).toString()
        )

        maxIntervalInput.setText(
            prefs.getLong(
                "max_interval_ms",
                DEFAULT_MAX_INTERVAL
            ).toString()
        )

        clickLimitInput.setText(
            prefs.getLong(
                "click_limit",
                DEFAULT_CLICK_LIMIT
            ).toString()
        )

        val saveButton = Button(this).apply {
            text = "SAVE SETTINGS"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)

            background = roundedBackground(
                blue,
                18
            )

            setOnClickListener {
                saveSettings()
            }
        }

        settingsCard.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58
            ).apply {
                topMargin = 16
            }
        )

        root.addView(
            settingsCard,
            marginParams(0, 0, 0, 14)
        )

        // ========================================================
        // STATISTICS
        // ========================================================

        val statsCard = card()

        val statsHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        statsHeader.addView(
            sectionTitle("▥  Click Statistics"),
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val resetButton = Button(this).apply {
            text = "RESET"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)

            background = roundedBackground(
                red,
                18
            )

            setOnClickListener {
                resetStatistics()
            }
        }

        statsHeader.addView(
            resetButton,
            fixedParams(95, 45, 0, 0, 0, 0)
        )

        statsCard.addView(statsHeader)

        // First row

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val totalBox = createStatBox(
            "Total",
            "0",
            blue
        )

        val target1Box = createStatBox(
            "Target 1",
            "0",
            purple
        )

        val target2Box = createStatBox(
            "Target 2",
            "0",
            pink
        )

        totalText = totalBox.second
        target1Text = target1Box.second
        target2Text = target2Box.second

        row1.addView(
            totalBox.first,
            weightParams()
        )

        row1.addView(
            target1Box.first,
            weightParams()
        )

        row1.addView(
            target2Box.first,
            weightParams()
        )

        statsCard.addView(row1)

        // Second row

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val limitBoxStat = createStatBox(
            "Limit",
            "Unlimited",
            orange
        )

        val remainingBox = createStatBox(
            "Remaining",
            "Unlimited",
            green
        )

        limitText = limitBoxStat.second
        remainingText = remainingBox.second

        row2.addView(
            limitBoxStat.first,
            weightParams()
        )

        row2.addView(
            remainingBox.first,
            weightParams()
        )

        statsCard.addView(
            row2,
            marginParams(0, 8, 0, 0)
        )

        progressText = TextView(this).apply {
            text = "Progress: 0%"
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            setPadding(0, 15, 0, 5)
        }

        statsCard.addView(progressText)

        root.addView(
            statsCard,
            marginParams(0, 0, 0, 14)
        )

        // ========================================================
        // PERMISSIONS
        // ========================================================

        val permissionCard = card()

        permissionCard.addView(
            sectionTitle("🛡  Permissions")
        )

        val accessibilityButton = Button(this).apply {
            text = "ACCESSIBILITY SETTINGS"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)

            background = roundedBackground(
                blue,
                18
            )

            setOnClickListener {
                openAccessibilitySettings()
            }
        }

        permissionCard.addView(
            accessibilityButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            )
        )

        val overlayButton = Button(this).apply {
            text = "OVERLAY SETTINGS"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)

            background = roundedBackground(
                purple,
                18
            )

            setOnClickListener {
                openOverlaySettings()
            }
        }

        permissionCard.addView(
            overlayButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            ).apply {
                topMargin = 10
            }
        )

        root.addView(permissionCard)

        setContentView(scrollView)

        refreshStatistics()
    }

    // ============================================================
    // SAVE SETTINGS
    // ============================================================

    private fun saveSettings() {

        val min = minIntervalInput.text
            .toString()
            .trim()
            .toLongOrNull()

        val max = maxIntervalInput.text
            .toString()
            .trim()
            .toLongOrNull()

        val limit = clickLimitInput.text
            .toString()
            .trim()
            .toLongOrNull()

        if (min == null ||
            max == null ||
            limit == null
        ) {
            Toast.makeText(
                this,
                "Please enter valid numbers",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (min < 50L) {
            Toast.makeText(
                this,
                "Minimum interval must be at least 50 ms",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (max < min) {
            Toast.makeText(
                this,
                "Maximum cannot be smaller than minimum",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (limit < 0L) {
            Toast.makeText(
                this,
                "Click limit cannot be negative",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        prefs.edit()
            .putLong("min_interval_ms", min)
            .putLong("max_interval_ms", max)
            .putLong("click_limit", limit)
            .apply()

        refreshStatistics()

        Toast.makeText(
            this,
            "Settings saved ✓",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============================================================
    // PRESET
    // ============================================================

    private fun setInterval(
        min: Long,
        max: Long
    ) {

        minIntervalInput.setText(min.toString())
        maxIntervalInput.setText(max.toString())

        prefs.edit()
            .putLong("min_interval_ms", min)
            .putLong("max_interval_ms", max)
            .apply()

        Toast.makeText(
            this,
            "$min - $max ms selected",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============================================================
    // RESET STATISTICS
    // ============================================================

    private fun resetStatistics() {

        getSharedPreferences(
            "counting",
            MODE_PRIVATE
        ).edit()
            .putLong("total_clicks", 0L)
            .putLong("target1_clicks", 0L)
            .putLong("target2_clicks", 0L)
            .apply()

        refreshStatistics()

        Toast.makeText(
            this,
            "Statistics reset ✓",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============================================================
    // REFRESH STATISTICS
    // ============================================================

    private fun refreshStatistics() {

        if (!::totalText.isInitialized) {
            return
        }

        val countPrefs = getSharedPreferences(
            "counting",
            MODE_PRIVATE
        )

        val total = countPrefs.getLong(
            "total_clicks",
            0L
        )

        val target1 = countPrefs.getLong(
            "target1_clicks",
            0L
        )

        val target2 = countPrefs.getLong(
            "target2_clicks",
            0L
        )

        val limit = prefs.getLong(
            "click_limit",
            0L
        ).coerceAtLeast(0L)

        totalText.text = total.toString()
        target1Text.text = target1.toString()
        target2Text.text = target2.toString()

        if (limit == 0L) {

            limitText.text = "Unlimited"
            remainingText.text = "Unlimited"
            progressText.text = "Progress: Unlimited"

        } else {

            limitText.text = limit.toString()

            val remaining = (
                limit - total
            ).coerceAtLeast(0L)

            remainingText.text =
                remaining.toString()

            val percentage =
                ((total.toDouble() /
                        limit.toDouble()) * 100.0)
                    .coerceIn(0.0, 100.0)

            progressText.text =
                "Progress: ${percentage.toInt()}%"
        }

        when {
            limit > 0L && total >= limit -> {
                statusText.text = "LIMIT REACHED"
                statusText.setTextColor(red)
            }

            total > 0L -> {
                statusText.text = "DATA SAVED"
                statusText.setTextColor(green)
            }

            else -> {
                statusText.text = "READY"
                statusText.setTextColor(green)
            }
        }
    }

    // ============================================================
    // ACCESSIBILITY
    // ============================================================

    private fun openAccessibilitySettings() {

        try {

            startActivity(
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                )
            )

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open Accessibility Settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ============================================================
    // OVERLAY
    // ============================================================

    private fun openOverlaySettings() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                try {

                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    )

                    intent.data = Uri.parse(
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
                    this,
                    "Overlay permission already enabled ✓",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            Toast.makeText(
                this,
                "Overlay permission is not required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ============================================================
    // NOTIFICATION PERMISSION
    // ============================================================

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

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

    // ============================================================
    // UI HELPERS
    // ============================================================

    private fun card(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(15, 15, 15, 15)

            background = roundedBackground(
                Color.WHITE,
                22
            )

            elevation = 3f
        }
    }

    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            setPadding(2, 0, 2, 12)
        }
    }

    private fun featureBox(
        icon: String,
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = "$icon\n$text"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)

            background = roundedBackground(
                Color.argb(
                    50,
                    255,
                    255,
                    255
                ),
                14
            )

            setPadding(4, 8, 4, 8)
        }
    }

  private fun presetButton(
    title: String,
    subtitle: String
): Button {

    return Button(this).apply {
        text = "$title\n$subtitle"
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(darkBlue)

        background = roundedBackground(
            Color.rgb(232, 240, 255),
            15
        )

        minHeight = 68
        minimumHeight = 68
    }
}

    // Returns both container and EditText
    private fun inputBox(
        label: String,
        hint: String,
        tagName: String
    ): Pair<LinearLayout, EditText> {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 0, 4, 0)
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
        }

        box.addView(labelView)

        val input = EditText(this).apply {

            inputType =
                InputType.TYPE_CLASS_NUMBER

            this.hint = hint
            this.tag = tagName

            textSize = 16f
            setTextColor(darkBlue)
            setSingleLine(true)

            background = roundedBackground(
                Color.rgb(247, 249, 253),
                12
            )

            setPadding(10, 0, 10, 0)
        }

        box.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                52
            )
        )

        return Pair(box, input)
    }

    private fun targetView(
        number: String,
        color: Int,
        label: String
    ): LinearLayout {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val circle = TextView(this).apply {
            text = number
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = circleBackground(color)
        }

        box.addView(
            circle,
            LinearLayout.LayoutParams(
                48,
                48
            )
        )

        val labelView = TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(darkBlue)
        }

        box.addView(labelView)

        return box
    }

    private fun createStatBox(
        label: String,
        value: String,
        color: Int
    ): Pair<LinearLayout, TextView> {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(5, 10, 5, 10)

            background = roundedBackground(
                Color.rgb(242, 246, 253),
                16
            )
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(darkBlue)
        }

        container.addView(labelView)

        val valueView = TextView(this).apply {
            text = value
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(color)
        }

        container.addView(valueView)

        return Pair(
            container,
            valueView
        )
    }

    // ============================================================
    // DRAWABLE HELPERS
    // ============================================================

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun circleBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    // ============================================================
    // LAYOUT HELPERS
    // ============================================================

    private fun weightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(
                3,
                0,
                3,
                0
            )
        }
    }

    private fun marginParams(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                left,
                top,
                right,
                bottom
            )
        }
    }

    private fun fixedParams(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            width,
            height
        ).apply {
            setMargins(
                left,
                top,
                right,
                bottom
            )
        }
    }
}
