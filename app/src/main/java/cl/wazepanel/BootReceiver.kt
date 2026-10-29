package cl.wazepanel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Si el panel estaba activado, lo vuelve a mostrar cuando arranca la radio. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED &&
            Store.isActive(context) &&
            Settings.canDrawOverlays(context)
        ) {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        }
    }
}
