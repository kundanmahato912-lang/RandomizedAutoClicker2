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

        private const val CHANNEL_ID = "random_clicker"

        private const val ACTION_TOGGLE =
            "com.example.randomclicker.TOGGLE"
    }

    private lateinit var wm: WindowManager

    private val handler =
        Handler(Looper.getMainLooper())

    private var running = false


    // ---------------------------------------------------------
    // TARGET POSITIONS
    // ---------------------------------------------------------

    private var x1 = 250f
    private var y1 = 500f

    private var x2 = 650f
    private var y2 = 900f


    // ---------------------------------------------------------
    // TARGET VIEWS
    // ---------------------------------------------------------

    private var target1: TextView? = null
    private var target2: TextView? = null


    // ---------------------------------------------------------
    // TARGET LAYOUT PARAMS
    // ---------------------------------------------------------

    private var lp1:
            WindowManager.LayoutParams? = null

    private var lp2:
            WindowManager.LayoutParams? = null


    // ---------------------------------------------------------
    // EMERGENCY START / STOP BUTTON
    // ---------------------------------------------------------

    private var controlButton: TextView? = null

    private var controlButtonParams:
            WindowManager.LayoutParams? = null


    // ---------------------------------------------------------
    // NOTIFICATION RECEIVER
    // ---------------------------------------------------------

    private val receiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (intent?.action == ACTION_TOGGLE) {

                    if (running) {

                        stopRandomClicks()

                    } else {

                        startRandomClicks()
                    }
                }
            }
        }


    // =========================================================
    // ACCESSIBILITY SERVICE CONNECTED
    // =========================================================

    override fun onServiceConnected() {

        super.onServiceConnected()

        wm =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager


        // Notification
        createChannel()

        registerToggleReceiver()


        // Targets
        showTargets()


        // Emergency START / STOP
        showControlButton()

        updateControlButton()


        // Notification
        updateNotification()
    }


    // =========================================================
    // REQUIRED ACCESSIBILITY EVENT
    // =========================================================

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        // This app does not need accessibility events.
    }


    // =========================================================
    // REGISTER NOTIFICATION RECEIVER
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
    // CREATE NOTIFICATION CHANNEL
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
            7,
            notification
        )
    }


    // =========================================================
    // CREATE TARGETS
    // =========================================================

    private fun showTargets() {

        if (target1 != null) return


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
    // CREATE TARGET VIEW
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
    // TARGET WINDOW PARAMETERS
    // =========================================================

    private fun makeParams(
        x: Int,
        y: Int
    ) =

        WindowManager.LayoutParams(

            90,
            90,

            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            PixelFormat.TRANSLUCENT

        ).apply {

            gravity =
                Gravity.TOP or
                        Gravity.START


            this.x = x
            this.y = y
        }


    // =========================================================
    // DRAG TARGET
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


                    // Do not allow dragging
                    // while running.

                    if (running) return false


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
    // EMERGENCY START / STOP BUTTON
    // =========================================================

    private fun showControlButton() {

        if (controlButton != null) return


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
                    20,
                    10,
                    20,
                    10
                )


                // IMPORTANT:
                // This button remains clickable
                // even when targets are click-through.

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
                70,

                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT

            ).apply {

                gravity =
                    Gravity.TOP or
                            Gravity.CENTER_HORIZONTAL


                x = 0

                y = 80
            }


        wm.addView(

            controlButton,

            controlButtonParams
        )
    }


    // =========================================================
    // UPDATE EMERGENCY BUTTON
    // =========================================================

    private fun updateControlButton() {

        controlButton?.let { button ->


            if (running) {

                button.text = "STOP"


                button.setBackgroundColor(
                    0xFFD32F2F.toInt()
                )

            } else {

                button.text = "START"


                button.setBackgroundColor(
                    0xFF2E7D32.toInt()
                )
            }
        }
    }


    // =========================================================
    // MAKE TARGETS CLICK-THROUGH
    // =========================================================

    private fun setClickThrough(
        clickThrough: Boolean
    ) {

        val flag =
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE


        // Target 1

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


        // Target 2

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


        // IMPORTANT:
        // Emergency control button is NOT
        // included here.
        //
        // Therefore it remains clickable.
    }


    // =========================================================
    // START RANDOM CLICKING
    // =========================================================

    fun startRandomClicks() {

        if (running) return


        running = true


        // Targets become click-through.
        setClickThrough(true)


        // Change START -> STOP
        updateControlButton()


        // Update notification.
        updateNotification()


        // Start clicking.
        scheduleNext()
    }


    // =========================================================
    // STOP RANDOM CLICKING
    // =========================================================

    fun stopRandomClicks() {


        // Immediately stop.
        running = false


        // Cancel every pending click.
        handler.removeCallbacksAndMessages(null)


        // Targets become draggable again.
        if (target1 != null) {

            setClickThrough(false)
        }


        // Change STOP -> START
        updateControlButton()


        // Update notification.
        updateNotification()
    }


    // =========================================================
    // RANDOM TARGET + RANDOM INTERVAL
    // =========================================================

    private fun scheduleNext() {


        if (!running) return


        // -----------------------------------------------------
        // RANDOM TARGET
        // -----------------------------------------------------
        //
        // Exactly ONE target is selected.
        //
        // true  = Target 1
        // false = Target 2
        //

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


        // -----------------------------------------------------
        // CLICK ONLY SELECTED TARGET
        // -----------------------------------------------------

        clickAt(
            x,
            y
        )


        // -----------------------------------------------------
        // READ MINIMUM + MAXIMUM INTERVAL
        // -----------------------------------------------------

        val prefs =

            getSharedPreferences(
                "settings",
                MODE_PRIVATE
            )


        val minInterval =

            prefs.getLong(
                "min_interval_ms",
                500L
            ).coerceAtLeast(50L)


        val maxInterval =

            prefs.getLong(
                "max_interval_ms",
                700L
            ).coerceAtLeast(
                minInterval
            )


        // -----------------------------------------------------
        // GENERATE RANDOM INTERVAL
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // NEXT CLICK
        // -----------------------------------------------------

        handler.postDelayed(

            {

                scheduleNext()

            },

            randomDelay
        )
    }


    // =========================================================
    // DISPATCH SINGLE TAP
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

            GestureDescription.StrokeDescription(

                path,

                0,

                35
            )


        dispatchGesture(

            GestureDescription.Builder()

                .addStroke(
                    stroke
                )

                .build(),

            null,

            null
        )
    }


    // =========================================================
    // ACCESSIBILITY INTERRUPT
    // =========================================================

    override fun onInterrupt() {

        // Nothing required here.
    }


    // =========================================================
    // SERVICE DESTROY
    // =========================================================

    override fun onDestroy() {


        // Stop clicking.
        stopRandomClicks()


        // Remove notification receiver.
        runCatching {

            unregisterReceiver(
                receiver
            )
        }


        // Remove Target 1.
        target1?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        // Remove Target 2.
        target2?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        // Remove Emergency button.
        controlButton?.let {

            runCatching {

                wm.removeView(it)
            }
        }


        // Cancel notification.
        getSystemService(
            NotificationManager::class.java
        ).cancel(7)


        super.onDestroy()
    }
}
