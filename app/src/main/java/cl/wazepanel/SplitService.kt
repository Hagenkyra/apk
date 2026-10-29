package cl.wazepanel

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Su único trabajo es pedirle al sistema que active o desactive la pantalla dividida
 * (GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN). Si el fabricante de la radio la desactivó,
 * el sistema simplemente no hará nada.
 */
class SplitService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: SplitService? = null

        fun toggle(): Boolean =
            instance?.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN) ?: false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}
