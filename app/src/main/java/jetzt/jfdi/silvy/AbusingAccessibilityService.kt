package jetzt.jfdi.silvy

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_LONG_CLICKED


class AbusingAccessibilityService : AccessibilityService() {
    private fun log(msg: String) {
        Log.d("ABUSER", msg)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo.flags = AccessibilityServiceInfo.FLAG_SEND_MOTION_EVENTS

        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL

        serviceInfo.notificationTimeout = 500

        log("Connected")
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            TYPE_VIEW_CLICKED, TYPE_VIEW_LONG_CLICKED -> {
                event.source?.let {
                    log("$event")

                    val rect = Rect()
                    it.getBoundsInScreen(rect)

                    log(".. at ${rect.toShortString()}")

                    val start = Intent(
                        this, FireworksActivity::class.java
                    )

                    start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    start.putExtra(
                        "rect", rect.toIntArray()
                    )

                    startActivity(
                        start
                    )
                }
            }

            else -> {
            }
        }

    }

    override fun onInterrupt() {
        log("onInterrupt")
    }
}

private fun Rect.toIntArray(): IntArray = mutableListOf(left, top, right, bottom).toIntArray()
