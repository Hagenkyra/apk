package cl.wazepanel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlin.math.abs

/** Dibuja el panel flotante con atajos encima de Waze. */
class OverlayService : Service() {

    companion object {
        const val ACTION_REFRESH = "cl.wazepanel.REFRESH"
        private const val CHANNEL = "panel"
    }

    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null
    private var collapsed = false

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Panel Waze", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setContentTitle("Panel Waze activo")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .build()
        startForeground(1, notification)

        buildPanel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_REFRESH) buildPanel()
        return START_STICKY
    }

    override fun onDestroy() {
        removePanel()
        super.onDestroy()
    }

    private fun removePanel() {
        panel?.let {
            try {
                wm.removeView(it)
            } catch (e: Exception) {
            }
        }
        panel = null
    }

    private fun buildPanel() {
        removePanel()

        // Por defecto arriba a la derecha: la zona izquierda de esta radio
        // (clima, cámara 360, radio FM) y la barra inferior de A/C quedan libres.
        val saved = Store.position(this)
        val x = saved?.first ?: (resources.displayMetrics.widthPixels - 150.dp())
        val y = saved?.second ?: 90.dp()
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }
        params = lp

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(6.dp(), 6.dp(), 6.dp(), 6.dp())
            background = GradientDrawable().apply {
                setColor(0xD9202124.toInt())
                cornerRadius = 14.dp().toFloat()
            }
        }

        val items = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (collapsed) View.GONE else View.VISIBLE
        }

        val handle = makeButton(if (collapsed) "W" else "WAZE", 0xFF455A64.toInt())
        enableDrag(handle) {
            collapsed = !collapsed
            items.visibility = if (collapsed) View.GONE else View.VISIBLE
            handle.text = if (collapsed) "W" else "WAZE"
            params?.let { p -> panel?.let { wm.updateViewLayout(it, p) } }
        }
        box.addView(handle)

        Store.load(this).forEach { d ->
            items.addView(makeButton(d.name) { WazeLauncher.navigate(this, d.target) })
        }
        items.addView(makeButton("Abrir Waze", 0xFF2E7D32.toInt()) { WazeLauncher.openWaze(this) })
        items.addView(makeButton("Dividir", 0xFF6A1B9A.toInt()) { splitScreen() })
        box.addView(items)

        panel = box
        wm.addView(box, lp)
    }

    /** Pide al sistema la pantalla dividida; si el servicio de accesibilidad no está activo, abre sus ajustes. */
    private fun splitScreen() {
        if (SplitService.instance == null) {
            Toast.makeText(this, "Activa «Panel Waze» en Accesibilidad", Toast.LENGTH_LONG).show()
            try {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: Exception) {
            }
            return
        }
        if (!SplitService.toggle()) {
            Toast.makeText(this, "La radio no permitió dividir la pantalla", Toast.LENGTH_LONG).show()
        }
    }

    private fun makeButton(
        label: String,
        color: Int = 0xFF1E88E5.toInt(),
        onClick: (() -> Unit)? = null
    ): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 16f
        setTextColor(Color.WHITE)
        minWidth = 96.dp()
        minimumWidth = 96.dp()
        minHeight = 52.dp()
        minimumHeight = 52.dp()
        background = GradientDrawable().apply {
            setColor(color)
            cornerRadius = 10.dp().toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 3.dp(), 0, 3.dp()) }
        onClick?.let { action -> setOnClickListener { action() } }
    }

    /** Arrastrar con el botón superior; un toque corto contrae o expande el panel. */
    private fun enableDrag(handle: View, onTap: () -> Unit) {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        handle.setOnTouchListener { _, e ->
            val lp = params ?: return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x
                    startY = lp.y
                    touchX = e.rawX
                    touchY = e.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - touchX).toInt()
                    val dy = (e.rawY - touchY).toInt()
                    if (abs(dx) > slop || abs(dy) > slop) moved = true
                    if (moved) {
                        lp.x = startX + dx
                        lp.y = startY + dy
                        panel?.let { wm.updateViewLayout(it, lp) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) Store.savePosition(this, lp.x, lp.y) else onTap()
                    true
                }
                else -> false
            }
        }
    }
}
