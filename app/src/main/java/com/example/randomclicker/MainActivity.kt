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

    // ============================================================
    // COLORS
    // ============================================================

    private val blue = Color.rgb(25, 118, 255)
    private val darkBlue = Color.rgb(18, 45, 90)
    private val green = Color.rgb(24, 180, 82)
    private val red = Color.rgb(235, 65, 75)
    private val purple = Color.rgb(105, 70, 220)
    private val pink = Color.rgb(235, 65, 130)
    private val orange = Color.rgb(245, 155, 40)
    private val background = Color.rgb(245, 248, 253)

    // ============================================================
    // ON CREATE
    // ============================================================

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
    // MAIN UI
    // ============================================================

    private fun createUI() {

        val scrollView = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            setPadding(16, 16, 16, 30)
        }

        scrollView.addView(root)

        // --------------------------------------------------------
        // HEADER
        // --------------------------------------------------------

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            background = roundedBackground(
                Color.rgb(20, 115, 245),
                24
            )
        }

        val title = TextView(this).apply {
            text = "Random Auto Clicker"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        header.addView(title)

        val subtitle = TextView(this).apply {
            text = "Randomly click Target 1 or Target 2"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 7, 0, 16)
        }

        header.addView(subtitle)

        val featureRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        featureRow.addView(
            featureBox("●", "Random\nTarget"),
            equalWeightParams()
        )

        featureRow.addView(
            featureBox("◷", "Random\nInterval"),
            equalWeightParams()
        )

        featureRow.addView(
            featureBox("⚡", "Auto Stop\nLimit"),
            equalWeightParams()
        )

        header.addView(featureRow)

        root.addView(
            header,
            marginParams(0, 0, 0, 14)
        )

        // --------------------------------------------------------
        // STATUS CARD
        // --------------------------------------------------------

        val statusCard = card()

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val readyCircle = TextView(this).apply {
            text = "▶"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = circleBackground(green)
        }

        statusRow.addView(
            readyCircle,
            fixedParams(62, 62, 0, 0, 16, 0)
        )

        val statusInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        statusText = TextView(this).apply {
            text = "READY"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }

        statusInfo.addView(statusText)

        val statusDescription = TextView(this).apply {
            text = "Set your settings and start from the overlay button."
            textSize = 14f
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

        // Targets

        val target1 = targetView(
            "1",
            blue,
            "Target 1"
        )

        val target2 = targetView(
            "2",
            pink,
            "Target 2"
        )

        statusRow.addView(
            target1,
            fixedParams(80, 85, 8, 0, 4, 0)
        )

        statusRow.addView(
            target2,
            fixedParams(80, 85, 4, 0, 0, 0)
        )

        statusCard.addView(statusRow)

        root.addView(
            statusCard,
            marginParams(0, 0, 0, 14)
        )

        // --------------------------------------------------------
        // QUICK PRESETS
        // --------------------------------------------------------

        val presetCard = card()

        presetCard.addView(
            sectionTitle("⚡  Quick Presets")
        )

        val presetRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val fastButton = presetButton(
            "FAST",
            "100 – 200 ms"
        )

        val normalButton = presetButton(
            "NORMAL",
            "500 – 700 ms"
        )

        val slowButton = presetButton(
            "SLOW",
            "2500 – 2700 ms"
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

        presetRow.addView(
            fastButton,
            equalWeightParams()
        )

        presetRow.addView(
            normalButton,
            equalWeightParams()
        )

        presetRow.addView(
            slowButton,
            equalWeightParams()
        )

        presetRow.addView(
            customButton,
            equalWeightParams()
        )

        presetCard.addView(presetRow)

        root.addView(
            presetCard,
            marginParams(0, 0, 0, 14)
        )

        // --------------------------------------------------------
        // SETTINGS CARD
        // --------------------------------------------------------

        val settingsCard = card()

        settingsCard.addView(
            sectionTitle("⚙  Click Settings")
        )

        val settingsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        settingsRow.addView(
            inputBox(
                "Minimum Interval (ms)",
                "500"
            ),
            equalWeightParams()
        )

        settingsRow.addView(
            inputBox(
                "Maximum Interval (ms)",
                "700"
            ),
            equalWeightParams()
        )

        settingsRow.addView(
            inputBox(
                "Click Limit",
                "0 = Unlimited"
            ),
            equalWeightParams()
        )

        settingsCard.addView(settingsRow)

        // Find the three EditTexts
        minIntervalInput =
            settingsRow.getChildAt(0)
                .findViewWithTag("min") as EditText

        maxIntervalInput =
            settingsRow.getChildAt(1)
                .findViewWithTag("max") as EditText

        clickLimitInput =
            settingsRow.getChildAt(2)
                .findViewWithTag("limit") as EditText

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
            text = "💾   SAVE SETTINGS"
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
                topMargin = 18
            }
        )

        root.addView(
            settingsCard,
            marginParams(0, 0, 0, 14)
        )

        // --------------------------------------------------------
        // STATISTICS CARD
        // --------------------------------------------------------

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
            text = "↻ RESET"
            textSize = 13f
            setTextColor(Color.WHITE)
            background = roundedBackground(red, 20)

            setOnClickListener {
                resetStatistics()
            }
        }

        statsHeader.addView(
            resetButton,
            fixedParams(115, 48, 0, 0, 0, 0)
        )

        statsCard.addView(statsHeader)

        // Statistics boxes

        val statsRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        totalText = statBox(
            "Total",
            "0",
            blue
        )

        target1Text = statBox(
            "Target 1",
            "0",
            purple
        )

        target2Text = statBox(
            "Target 2",
            "0",
            pink
        )

        statsRow1.addView(
            totalText.parentAsView(),
            equalWeightParams()
        )

        statsRow1.addView(
            target1Text.parentAsView(),
            equalWeightParams()
        )

        statsRow1.addView(
            target2Text.parentAsView(),
            equalWeightParams()
        )

        statsCard.addView(statsRow1)

        val statsRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        limitText = statBox(
            "Limit",
            "Unlimited",
            orange
        )

        remainingText = statBox(
            "Remaining",
            "Unlimited",
            green
        )

        statsRow2.addView(
            limitText.parentAsView(),
            equalWeightParams()
        )

        statsRow2.addView(
            remainingText.parentAsView(),
            equalWeightParams()
        )

        statsCard.addView(
            statsRow2,
            marginParams(0, 8, 0, 0)
        )

        // Progress

        progressText = TextView(this).apply {
            text = "Progress: 0%"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            gravity = Gravity.CENTER
            setPadding(0, 15, 0, 5)
        }

        statsCard.addView(progressText)

        root.addView(
            statsCard,
            marginParams(0, 0, 0, 14)
        )

        // --------------------------------------------------------
        // PERMISSIONS CARD
        // --------------------------------------------------------

        val permissionCard = card()

        permissionCard.addView(
            sectionTitle("🛡  Permissions")
        )

        val accessibilityButton = Button(this).apply {
            text = "⚙  ACCESSIBILITY SETTINGS"
            textSize = 15f
            setTextColor(Color.WHITE)
            background = roundedBackground(
                blue,
                18
            )

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
                        "Unable to open settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
            text = "▱  OVERLAY SETTINGS"
            textSize = 15f
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

        if (min == null || max == null || limit == null) {
            Toast.makeText(
                this,
                "Please enter valid numbers",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (min < 50) {
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

        if (limit < 0) {
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

        Toast.makeText(
            this,
            "Settings saved ✓",
            Toast.LENGTH_SHORT
        ).show()

        refreshStatistics()
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
            "$min – $max ms selected",
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

        val countPrefs =
            getSharedPreferences(
                "counting",
                MODE_PRIVATE
            )

        val total =
            countPrefs.getLong(
                "total_clicks",
                0L
            )

        val target1 =
            countPrefs.getLong(
                "target1_clicks",
                0L
            )

        val target2 =
            countPrefs.getLong(
                "target2_clicks",
                0L
            )

        val limit =
            prefs.getLong(
                "click_limit",
                0L
            )

        totalText.text = total.toString()
        target1Text.text = target1.toString()
        target2Text.text = target2.toString()

        if (limit <= 0) {

            limitText.text = "Unlimited"
            remainingText.text = "Unlimited"
            progressText.text = "Progress: Unlimited"

        } else {

            limitText.text = limit.toString()

            val remaining =
                (limit - total)
                    .coerceAtLeast(0L)

            remainingText.text =
                remaining.toString()

            val percentage =
                ((total.toDouble() / limit.toDouble()) * 100)
                    .coerceIn(0.0, 100.0)

            progressText.text =
                "Progress: ${percentage.toInt()}%"
        }

        if (limit > 0 && total >= limit) {
            statusText.text = "LIMIT REACHED"
            statusText.setTextColor(red)
        } else if (total > 0) {
            statusText.text = "DATA SAVED"
            statusText.setTextColor(green)
        } else {
            statusText.text = "READY"
            statusText.setTextColor(green)
        }
    }

    // ============================================================
    // OVERLAY SETTINGS
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

    // ============================================================
    // UI HELPERS
    // ============================================================

    private fun card(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = roundedBackground(
                Color.WHITE,
                22
            )
            elevation = 4f
        }
    }

    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            setPadding(2, 0, 2, 14)
        }
    }

    private fun featureBox(
        icon: String,
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = "$icon\n$text"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedBackground(
                Color.argb(55, 255, 255, 255),
                15
            )
            setPadding(5, 8, 5, 8)
        }
    }

    private fun presetButton(
        title: String,
        subtitle: String
    ): Button {

        return Button(this).apply {
            text = "$title\n$subtitle"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            background = roundedBackground(
                Color.rgb(232, 240, 255),
                15
            )
            minimumHeight = 65
        }
    }

    private fun inputBox(
        label: String,
        hint: String
    ): LinearLayout {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 0, 5, 0)
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
        }

        box.addView(labelView)

        val input = EditText(this).apply {
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER

            this.hint = hint
            textSize = 17f
            setTextColor(darkBlue)
            setSingleLine(true)

            background = roundedBackground(
                Color.rgb(248, 250, 255),
                12
            )

            setPadding(10, 5, 10, 5)

            tag = when {
                label.startsWith("Minimum") -> "min"
                label.startsWith("Maximum") -> "max"
                else -> "limit"
            }
        }

        box.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            )
        )

        return box
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
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = circleBackground(color)
        }

        box.addView(
            circle,
            LinearLayout.LayoutParams(
                50,
                50
            )
        )

        val text = TextView(this).apply {
            this.text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkBlue)
            gravity = Gravity.CENTER
        }

        box.addView(text)

        return box
    }

    private fun statBox(
        label: String,
        value: String,
        color: Int
    ): TextView {

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
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(darkBlue)
        }

        container.addView(labelView)

        val valueView = TextView(this).apply {
            text = value
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(color)
        }

        container.addView(valueView)

        return valueView
    }

    // Get parent container of TextView
    private fun TextView.parentAsView(): View {

        return this.parent as View
    }

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

    private fun equalWeightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(4, 0, 4, 0)
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
