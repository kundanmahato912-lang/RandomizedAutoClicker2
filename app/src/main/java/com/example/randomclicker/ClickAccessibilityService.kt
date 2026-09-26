package com.example.randomclicker

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
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import kotlin.random.Random


class ClickAccessibilityService : AccessibilityService() {

    companion object {

        private const val CHANNEL_ID =
            "random_clicker"

        private const val ACTION_TOGGLE =
            "com.example.randomclicker.TOGGLE"

        private const val NOTIFICATION_ID = 7

        private const val MODE_RESET = "RESET"
        private const val MODE_CONTINUE = "CONTINUE"
    }


    // =========================================================
    // WINDOW / HANDLER
    // =========================================================

    private lateinit var wm: WindowManager

    private val handler =
        Handler(Looper.getMainLooper())

    private var running = false


    // =========================================================
    // TARGET POSITIONS
    // =========================================================

    private var x1 = 250f
    private var y1 = 500f

    private var x2 = 650f
    private var y2 = 900f


    // =========================================================
    // TARGET VIEWS
    // =========================================================

    private var target1: TextView? = null
    private var target2: TextView? = null


    // =========================================================
    // TARGET LAYOUT PARAMS
    // =========================================================

    private var lp1:
            WindowManager.LayoutParams? = null

    private var lp2:
            WindowManager.LayoutParams? = null


    // =========================================================
    // CONTROL BUTTON
    // =========================================================

    private var controlButton: TextView? = null

    private var controlButtonParams:
            WindowManager.LayoutParams? = null


    // =========================================================
    // BIGGER COMPACT INFO OVERLAY
    // =========================================================

    private var infoOverlay: TextView? = null

    private var infoOverlayParams:
            WindowManager.LayoutParams? = null


    // =========================================================
    // RESET / CONTINUE BUTTON
    // =========================================================

    private var modeButton: TextView? = null

    private var modeButtonParams:
            WindowManager.LayoutParams? = null


    // =========================================================
    // COUNTERS
    // =========================================================

    private var totalClicks = 0L

    private var target1Clicks = 0L

    private var target2Clicks = 0L


    // =========================================================
    // COUNTING MODE
    // =========================================================

    private var countingMode =
        MODE_RESET


    // =========================================================
    // NOTIFICATION RECEIVER
    // =========================================================

    private val receiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (
                    intent?.action ==
                    ACTION_TOGGLE
                ) {

                    if (running) {

                        stopRandomClicks()

                    } else {

                        startRandomClicks()
                    }
                }
            }
        }


    // =========================================================
    // SERVICE CONNECTED
    // =========================================================

    override fun onServiceConnected() {

        super.onServiceConnected()

        wm =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        loadData()

        createChannel()

        registerToggleReceiver()

        showTargets()

        showControlButton()

        showInfoOverlay()

        showModeButton()

        updateAllOverlay()

        updateNotification()
    }


    // =========================================================
    // ACCESSIBILITY EVENT
    // =========================================================

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // Accessibility events are not required.
    }


    // =========================================================
    // REGISTER RECEIVER
    // =========================================================

    private fun registerToggleReceiver() {

        val filter =
            IntentFilter(ACTION_TOGGLE)

        if (Build.VERSION.SDK_INT >= 33) {

            registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {

            @Suppress("DEPRECATION")

            registerReceiver(
                receiver,
                filter
            )
        }
    }


    // =========================================================
    // NOTIFICATION CHANNEL
    // =========================================================

    private fun createChannel() {

        val nm =
            getSystemService(
                NotificationManager::class.java
            )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Random Auto Clicker",
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }


    // =========================================================
    // NOTIFICATION ACTION
    // =========================================================

    private fun notificationAction():
            PendingIntent {

        val intent =
            Intent(ACTION_TOGGLE)
                .setPackage(packageName)

        return PendingIntent.getBroadcast(
            this,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }


    // =========================================================
    // UPDATE NOTIFICATION
    // =========================================================

    private fun updateNotification() {

        val nm =
            getSystemService(
                NotificationManager::class.java
            )

        val actionText =
            if (running) {
                "STOP"
            } else {
                "START"
            }

        val notification =
            Notification.Builder(
                this,
                CHANNEL_ID
            )

                .setSmallIcon(
                    android.R.drawable.ic_media_play
                )

                .setContentTitle(
                    "Randomized Auto Clicker"
                )

                .setContentText(

                    if (running) {

                        "Running • random target & interval"

                    } else {

                        "Stopped • ready to start"
                    }
                )

                .setOngoing(true)

                .addAction(
                    Notification.Action.Builder(
                        null,
                        actionText,
                        notificationAction()
                    ).build()
                )

                .build()

        nm.notify(
            NOTIFICATION_ID,
            notification
        )
    }


    // =========================================================
    // SHOW TARGETS
    // =========================================================

    private fun showTargets() {

        if (target1 != null) {
            return
        }

        target1 =
            makeTarget("1")

        target2 =
            makeTarget("2")

        lp1 =
            makeParams(
                x1.toInt(),
                y1.toInt()
            )

        lp2 =
            makeParams(
                x2.toInt(),
                y2.toInt()
            )

        addDraggable(
            target1!!,
            true,
            lp1!!
        )

        addDraggable(
            target2!!,
            false,
            lp2!!
        )
    }


    // =========================================================
    // MAKE TARGET
    // =========================================================

    private fun makeTarget(
        label: String
    ) =
        TextView(this).apply {

            text = label

            textSize = 18f

            gravity =
                Gravity.CENTER

            setTextColor(
                0xFFFFFFFF.toInt()
            )

            setBackgroundColor(

                if (label == "1") {

                    0xFF6750A4.toInt()

                } else {

                    0xFFE91E63.toInt()
                }
            )

            alpha = 0.86f
        }


    // =========================================================
    // TARGET PARAMS
    // =========================================================

    private fun makeParams(
        x: Int,
        y: Int
    ) =
        WindowManager.LayoutParams(

            90,
            90,

            WindowManager.LayoutParams
                .TYPE_ACCESSIBILITY_OVERLAY,

            WindowManager.LayoutParams
                .FLAG_NOT_FOCUSABLE,

            PixelFormat.TRANSLUCENT

        ).apply {

            gravity =
                Gravity.TOP or
                        Gravity.START

            this.x = x
            this.y = y
        }


    // =========================================================
    // DRAG TARGETS
    // =========================================================

    private fun addDraggable(
        v: TextView,
        first: Boolean,
        p: WindowManager.LayoutParams
    ) {

        v.setOnTouchListener(

            object : View.OnTouchListener {

                var downX = 0f
                var downY = 0f

                var startX = 0
                var startY = 0

                override fun onTouch(
                    view: View,
                    e: MotionEvent
                ): Boolean {

                    if (running) {
                        return false
                    }

                    when (e.actionMasked) {

                        MotionEvent.ACTION_DOWN -> {

                            downX =
                                e.rawX

                            downY =
                                e.rawY

                            startX =
                                p.x

                            startY =
                                p.y

                            return true
                        }


                        MotionEvent.ACTION_MOVE -> {

                            p.x =
                                startX +
                                        (
                                                e.rawX -
                                                        downX
                                                ).toInt()

                            p.y =
                                startY +
                                        (
                                                e.rawY -
                                                        downY
                                                ).toInt()

                            wm.updateViewLayout(
                                view,
                                p
                            )


                            if (first) {

                                x1 =
                                    p.x.toFloat()

                                y1 =
                                    p.y.toFloat()

                            } else {

                                x2 =
                                    p.x.toFloat()

                                y2 =
                                    p.y.toFloat()
                            }

                            return true
                        }


                        MotionEvent.ACTION_UP -> {

                            return true
                        }
                    }

                    return false
                }
            }
        )

        wm.addView(
            v,
            p
        )
    }


    // =========================================================
    // START / STOP BUTTON
    // =========================================================

    private fun showControlButton() {

        if (controlButton != null) {
            return
        }

        controlButton =
            TextView(this).apply {

                text = "START"

                textSize = 16f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    0xFFFFFFFF.toInt()
                )

                setBackgroundColor(
                    0xFF2E7D32.toInt()
                )

                setPadding(
                    16,
                    8,
                    16,
                    8
                )

                setOnClickListener {

                    if (running) {

                        stopRandomClicks()

                    } else {

                        startRandomClicks()
                    }
                }
            }


        controlButtonParams =
            WindowManager.LayoutParams(

                150,
                65,

                WindowManager.LayoutParams
                    .TYPE_ACCESSIBILITY_OVERLAY,

                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT

            ).apply {

                gravity =
                    Gravity.TOP or
                            Gravity.CENTER_HORIZONTAL

                x = 0

                y = 65
            }


        wm.addView(
            controlButton,
            controlButtonParams
        )
    }


    // =========================================================
    // UPDATE START / STOP BUTTON
    // =========================================================

    private fun updateControlButton() {

        controlButton?.let { button ->

            if (running) {

                button.text =
                    "STOP"

                button.setBackgroundColor(
                    0xFFD32F2F.toInt()
                )

            } else {

                button.text =
                    "START"

                button.setBackgroundColor(
                    0xFF2E7D32.toInt()
                )
            }
        }
    }


    // =========================================================
    // SHOW INFO OVERLAY
    // =========================================================

    private fun showInfoOverlay() {

        if (infoOverlay != null) {
            return
        }

        infoOverlay =
            TextView(this).apply {

                text =
                    "T:0   ①:0   ②:0\n" +
                    "Limit:Unlimited   Left:∞"

                textSize = 12f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    0xFFFFFFFF.toInt()
                )

                setBackgroundColor(
                    0xCC000000.toInt()
                )

                setPadding(
                    8,
                    4,
                    8,
                    4
                )
            }


        infoOverlayParams =
            WindowManager.LayoutParams(

                250,
                62,

                WindowManager.LayoutParams
                    .TYPE_ACCESSIBILITY_OVERLAY,

                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT

            ).apply {

                gravity =
                    Gravity.TOP or
                            Gravity.CENTER_HORIZONTAL

                x = 0

                y = 135
            }


        wm.addView(
            infoOverlay,
            infoOverlayParams
        )
    }


    // =========================================================
    // UPDATE INFO OVERLAY
    // =========================================================

    private fun updateInfoOverlay() {

        val limit =
            getClickLimit()

        val remaining =
            if (limit <= 0L) {

                "∞"

            } else {

                (limit - totalClicks)
                    .coerceAtLeast(0L)
                    .toString()
            }


        val modeText =
            if (
                countingMode ==
                MODE_RESET
            ) {

                "RESET (R)"

            } else {

                "CONTINUE (C)"
            }


        infoOverlay?.text =
            "T:$totalClicks   ①:$target1Clicks   ②:$target2Clicks\n" +
            "Limit:${if (limit <= 0L) "Unlimited" else limit}   " +
            "Left:$remaining\n" +
            "Mode: $modeText"
    }


    // =========================================================
    // SHOW R / C BUTTON
    // =========================================================

    private fun showModeButton() {

        if (modeButton != null) {
            return
        }

        modeButton =
            TextView(this).apply {

                text = "R"

                textSize = 12f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    0xFFFFFFFF.toInt()
                )

                setBackgroundColor(
                    0xFF555555.toInt()
                )

                setPadding(
                    4,
                    2,
                    4,
                    2
                )

                setOnClickListener {

                    countingMode =
                        if (
                            countingMode ==
                            MODE_RESET
                        ) {

                            MODE_CONTINUE

                        } else {

                            MODE_RESET
                        }

                    saveCountingMode()

                    updateModeButton()

                    updateInfoOverlay()
                }
            }


        modeButtonParams =
            WindowManager.LayoutParams(

                45,
                32,

                WindowManager.LayoutParams
                    .TYPE_ACCESSIBILITY_OVERLAY,

                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT

            ).apply {

                gravity =
                    Gravity.TOP or
                            Gravity.CENTER_HORIZONTAL

                x = 105

                y = 135
            }


        wm.addView(
            modeButton,
            modeButtonParams
        )
    }


    // =========================================================
    // UPDATE R / C BUTTON
    // =========================================================

    private fun updateModeButton() {

        modeButton?.text =

            if (
                countingMode ==
                MODE_RESET
            ) {

                "R"

            } else {

                "C"
            }
    }


    // =========================================================
    // CLICK THROUGH
    // =========================================================

    private fun setClickThrough(
        clickThrough: Boolean
    ) {

        val flag =
            WindowManager.LayoutParams
                .FLAG_NOT_TOUCHABLE


        lp1?.let {

            it.flags =
                if (clickThrough) {

                    it.flags or flag

                } else {

                    it.flags and flag.inv()
                }


            target1?.let { view ->

                wm.updateViewLayout(
                    view,
                    it
                )
            }
        }


        lp2?.let {

            it.flags =
                if (clickThrough) {

                    it.flags or flag

                } else {

                    it.flags and flag.inv()
                }


            target2?.let { view ->

                wm.updateViewLayout(
                    view,
                    it
                )
            }
        }
    }


    // =========================================================
    // GET CLICK LIMIT
    // =========================================================

    private fun getClickLimit(): Long {

        return getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )
            .getLong(
                "click_limit",
                0L
            )
            .coerceAtLeast(0L)
    }


    // =========================================================
    // START
    // =========================================================

    fun startRandomClicks() {

        if (running) {
            return
        }


        /*
         * RESET mode:
         * START -> count becomes zero.
         */

        if (
            countingMode ==
            MODE_RESET
        ) {

            totalClicks = 0L

            target1Clicks = 0L

            target2Clicks = 0L

            saveCountingData()
        }


        val limit =
            getClickLimit()


        /*
         * If previous count has already
         * reached the limit, don't start.
         */

        if (
            limit > 0L &&
            totalClicks >= limit
        ) {

            running = false

            updateAllOverlay()

            updateNotification()

            return
        }


        running = true


        setClickThrough(true)

        updateAllOverlay()

        updateNotification()

        scheduleNext()
    }


    // =========================================================
    // STOP
    // =========================================================

    fun stopRandomClicks() {

        running = false


        /*
         * Cancel all pending callbacks.
         */

        handler.removeCallbacksAndMessages(
            null
        )


        if (target1 != null) {

            setClickThrough(false)
        }


        saveCountingData()

        updateAllOverlay()

        updateNotification()
    }


    // =========================================================
    // RANDOM CLICK LOOP
    // =========================================================

    private fun scheduleNext() {

        if (!running) {
            return
        }


        val limit =
            getClickLimit()


        /*
         * Safety check before another click.
         */

        if (
            limit > 0L &&
            totalClicks >= limit
        ) {

            stopRandomClicks()

            return
        }


        /*
         * Select exactly ONE target.
         */

        val chooseFirst =
            Random.nextBoolean()


        val x =
            if (chooseFirst) {

                x1 + 45f

            } else {

                x2 + 45f
            }


        val y =
            if (chooseFirst) {

                y1 + 45f

            } else {

                y2 + 45f
            }


        /*
         * Count this click.
         */

        totalClicks++


        if (chooseFirst) {

            target1Clicks++

        } else {

            target2Clicks++
        }


        saveCountingData()

        updateAllOverlay()


        /*
         * Perform exactly one tap.
         */

        clickAt(
            x,
            y
        )


        /*
         * If limit reached by this click,
         * STOP immediately.
         */

        if (
            limit > 0L &&
            totalClicks >= limit
        ) {

            stopRandomClicks()

            return
        }


        // =====================================================
        // RANDOM MIN-MAX INTERVAL
        // =====================================================

        val prefs =
            getSharedPreferences(
                "settings",
                MODE_PRIVATE
            )


        val minInterval =
            prefs.getLong(
                "min_interval_ms",
                500L
            )
                .coerceAtLeast(50L)


        val maxInterval =
            prefs.getLong(
                "max_interval_ms",
                700L
            )
                .coerceAtLeast(
                    minInterval
                )


        val randomDelay =

            if (
                minInterval ==
                maxInterval
            ) {

                minInterval

            } else {

                Random.nextLong(
                    minInterval,
                    maxInterval + 1
                )
            }


        /*
         * Schedule next click.
         */

        handler.postDelayed(
            {

                scheduleNext()

            },
            randomDelay
        )
    }


    // =========================================================
    // SINGLE TAP
    // =========================================================

    private fun clickAt(
        x: Float,
        y: Float
    ) {

        val path =
            Path().apply {

                moveTo(
                    x,
                    y
                )
            }


        val stroke =
            GestureDescription
                .StrokeDescription(
                    path,
                    0,
                    35
                )


        dispatchGesture(

            GestureDescription
                .Builder()
                .addStroke(stroke)
                .build(),

            null,

            null
        )
    }


    // =========================================================
    // SAVE COUNT DATA
    // =========================================================

    private fun saveCountingData() {

        getSharedPreferences(
            "counting",
            MODE_PRIVATE
        )
            .edit()

            .putLong(
                "total_clicks",
                totalClicks
            )

            .putLong(
                "target1_clicks",
                target1Clicks
            )

            .putLong(
                "target2_clicks",
                target2Clicks
            )

            .apply()
    }


    // =========================================================
    // LOAD DATA
    // =========================================================

    private fun loadData() {

        val prefs =
            getSharedPreferences(
                "counting",
                MODE_PRIVATE
            )


        totalClicks =
            prefs.getLong(
                "total_clicks",
                0L
            )


        target1Clicks =
            prefs.getLong(
                "target1_clicks",
                0L
            )


        target2Clicks =
            prefs.getLong(
                "target2_clicks",
                0L
            )


        countingMode =
            prefs.getString(
                "counting_mode",
                MODE_RESET
            ) ?: MODE_RESET
    }


    // =========================================================
    // SAVE MODE
    // =========================================================

    private fun saveCountingMode() {

        getSharedPreferences(
            "counting",
            MODE_PRIVATE
        )
            .edit()

            .putString(
                "counting_mode",
                countingMode
            )

            .apply()
    }


    // =========================================================
    // UPDATE EVERYTHING
    // =========================================================

    private fun updateAllOverlay() {

        updateControlButton()

        updateInfoOverlay()

        updateModeButton()
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onInterrupt() {
        // Nothing required.
    }


    override fun onDestroy() {

        stopRandomClicks()


        runCatching {

            unregisterReceiver(
                receiver
            )
        }


        target1?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        target2?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        controlButton?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        infoOverlay?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        modeButton?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        getSystemService(
            NotificationManager::class.java
        )
            .cancel(
                NOTIFICATION_ID
            )


        super.onDestroy()
    }
}
