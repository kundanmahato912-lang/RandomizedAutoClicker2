package com.example.randomclicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        box.addView(TextView(this).apply {
            text = "Randomized Alternating Auto Clicker"
            textSize = 22f
            gravity = Gravity.CENTER
        })

        box.addView(TextView(this).apply {
            text = "\nहर click के बीच का समय fixed रहेगा।"
            textSize = 16f
        })

        val intervalInput = EditText(this).apply {
            hint = "Interval (milliseconds)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getLong("interval_ms", 500L).toString())
            textSize = 18f
        }
        box.addView(intervalInput)

        box.addView(TextView(this).apply {
            text = "उदाहरण: 500 ms = 0.5 सेकंड\n1000 ms = 1 सेकंड\n2000 ms = 2 सेकंड"
            textSize = 14f
        })

        box.addView(Button(this).apply {
            text = "Save Fixed Interval"
            setOnClickListener {
                val value = intervalInput.text.toString().toLongOrNull()
                if (value == null || value < 50L) {
                    Toast.makeText(this@MainActivity, "कम से कम 50 ms डालें", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putLong("interval_ms", value).apply()
                    Toast.makeText(
                        this@MainActivity,
                        "Fixed interval: $value ms",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        box.addView(TextView(this).apply {
            text = "\n1. Accessibility Service allow करें.\n" +
                    "2. Floating buttons की permission allow करें.\n" +
                    "3. दो floating targets (1 और 2) दिखाई देंगे.\n" +
                    "4. Stop रहने पर उन्हें अपनी जगह drag करें.\n" +
                    "5. Notification से START करें.\n" +
                    "6. हर click पर 1 या 2 random होगा, लेकिन clicks के बीच का समय fixed रहेगा."
            textSize = 15f
        })

        box.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        box.addView(Button(this).apply {
            text = "Allow Floating Buttons"
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        })

        setContentView(box)
    }
}
