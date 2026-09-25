package com.example.randomclicker

import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class ClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val CHANNEL_ID = "random_clicker"
        private const val ACTION_TOGGLE = "com.example.randomclicker.TOGGLE"
    }

    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private var x1 = 250f
    private var y1 = 500f
    private var x2 = 650f
    private var y2 = 900f

    private var target1: TextView? = null
    private var target2: TextView? = null
    private var lp1: WindowManager.LayoutParams? = null
    private var lp2: WindowManager.LayoutParams? = null

    private val defaultIntervalMs = 500L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_TOGGLE) {
                if (running) stopRandomClicks() else startRandomClicks()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        registerToggleReceiver()
        showTargets()
        updateNotification()
    }

    private fun registerToggleReceiver() {
        val filter = IntentFilter(ACTION_TOGGLE)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Random Auto Clicker", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun notificationAction(): PendingIntent {
        val intent = Intent(ACTION_TOGGLE).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this, 100, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        val actionText = if (running) "STOP" else "START"
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Randomized Auto Clicker")
            .setContentText(if (running) "Running • random target selection" else "Stopped • drag targets to position")
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, actionText, notificationAction()).build())
            .build()
        nm.notify(7, notification)
    }

    private fun showTargets() {
        if (target1 != null) return
        target1 = makeTarget("1")
        target2 = makeTarget("2")
        lp1 = makeParams(x1.toInt(), y1.toInt())
        lp2 = makeParams(x2.toInt(), y2.toInt())
        addDraggable(target1!!, true, lp1!!)
        addDraggable(target2!!, false, lp2!!)
    }

    private fun makeTarget(label: String) = TextView(this).apply {
        text = label
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundColor(if (label == "1") 0xFF6750A4.toInt() else 0xFFE91E63.toInt())
        alpha = 0.86f
    }

    private fun makeParams(x: Int, y: Int) =
        WindowManager.LayoutParams(
            90, 90,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

    private fun addDraggable(v: TextView, first: Boolean, p: WindowManager.LayoutParams) {
        v.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            var startX = 0
            var startY = 0

            override fun onTouch(view: View, e: MotionEvent): Boolean {
                if (running) return false
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.rawX; downY = e.rawY
                        startX = p.x; startY = p.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        p.x = startX + (e.rawX - downX).toInt()
                        p.y = startY + (e.rawY - downY).toInt()
                        wm.updateViewLayout(view, p)
                        if (first) { x1 = p.x.toFloat(); y1 = p.y.toFloat() }
                        else { x2 = p.x.toFloat(); y2 = p.y.toFloat() }
                        return true
                    }
                    MotionEvent.ACTION_UP -> return true
                }
                return false
            }
        })
        wm.addView(v, p)
    }

    private fun setClickThrough(clickThrough: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        lp1?.let {
            it.flags = if (clickThrough) it.flags or flag else it.flags and flag.inv()
            target1?.let { view -> wm.updateViewLayout(view, it) }
        }
        lp2?.let {
            it.flags = if (clickThrough) it.flags or flag else it.flags and flag.inv()
            target2?.let { view -> wm.updateViewLayout(view, it) }
        }
    }

    fun startRandomClicks() {
        if (running) return
        running = true
        setClickThrough(true)
        updateNotification()
        scheduleNext()
    }

    fun stopRandomClicks() {
        running = false
        handler.removeCallbacksAndMessages(null)
        if (target1 != null) setClickThrough(false)
        updateNotification()
    }

    private fun scheduleNext() {
        if (!running) return

        val chooseFirst = Random.nextBoolean()
        val x = if (chooseFirst) x1 + 45f else x2 + 45f
        val y = if (chooseFirst) y1 + 45f else y2 + 45f

        clickAt(x, y)

        val delay = getSharedPreferences("settings", MODE_PRIVATE)
            .getLong("interval_ms", defaultIntervalMs)
            .coerceAtLeast(50L)

        handler.postDelayed({ scheduleNext() }, delay)
    }

    private fun clickAt(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 35)
        dispatchGesture(
            GestureDescription.Builder().addStroke(stroke).build(),
            null, null
        )
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        stopRandomClicks()
        runCatching { unregisterReceiver(receiver) }
        target1?.let { runCatching { wm.removeView(it) } }
        target2?.let { runCatching { wm.removeView(it) } }
        getSystemService(NotificationManager::class.java).cancel(7)
        super.onDestroy()
    }
}
