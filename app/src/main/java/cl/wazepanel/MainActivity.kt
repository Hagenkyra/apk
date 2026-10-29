package cl.wazepanel

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Pantalla de configuración: permiso, lista de destinos y botones para mostrar u ocultar el panel. */
class MainActivity : Activity() {

    private companion object {
        const val MAX_DESTS = 6
    }

    private val nameFields = mutableListOf<EditText>()
    private val targetFields = mutableListOf<EditText>()
    private lateinit var status: TextView

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        col.addView(TextView(this).apply {
            text = "Panel Waze"
            textSize = 26f
        })

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 8.dp(), 0, 8.dp())
        }
        col.addView(status)

        col.addView(Button(this).apply {
            text = "1. Conceder permiso de superposición"
            setOnClickListener { askOverlayPermission() }
        })

        col.addView(Button(this).apply {
            text = "Activar accesibilidad (para el botón Dividir)"
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "Esta radio no tiene ajustes de accesibilidad", Toast.LENGTH_LONG).show()
                }
            }
        })

        col.addView(TextView(this).apply {
            text = "Destinos. En «Destino» escribe: fav:Casa (favorito de Waze), " +
                "lat,lng (por ejemplo -33.4489,-70.6693) o una dirección o lugar."
            textSize = 14f
            setPadding(0, 12.dp(), 0, 8.dp())
        })

        val saved = Store.load(this)
        for (i in 0 until MAX_DESTS) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val name = EditText(this).apply {
                hint = "Nombre"
                setSingleLine()
                setText(saved.getOrNull(i)?.name ?: "")
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val target = EditText(this).apply {
                hint = "Destino"
                setSingleLine()
                setText(saved.getOrNull(i)?.target ?: "")
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            }
            nameFields.add(name)
            targetFields.add(target)
            row.addView(name)
            row.addView(target)
            col.addView(row)
        }

        col.addView(Button(this).apply {
            text = "2. Guardar y mostrar panel"
            setOnClickListener { saveAndShow() }
        })

        col.addView(Button(this).apply {
            text = "Ocultar panel"
            setOnClickListener {
                Store.setActive(this@MainActivity, false)
                stopService(Intent(this@MainActivity, OverlayService::class.java))
            }
        })

        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onResume() {
        super.onResume()
        status.text = if (Settings.canDrawOverlays(this)) {
            "Permiso de superposición: concedido"
        } else {
            "Permiso de superposición: falta concederlo"
        }
    }

    private fun askOverlayPermission() {
        try {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Esta radio no tiene esa pantalla. Por adb: adb shell appops set " +
                    "$packageName SYSTEM_ALERT_WINDOW allow",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveAndShow() {
        val list = nameFields.indices.mapNotNull { i ->
            val n = nameFields[i].text.toString().trim()
            val t = targetFields[i].text.toString().trim()
            if (n.isNotEmpty() && t.isNotEmpty()) Dest(n, t) else null
        }
        Store.save(this, list)

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Primero concede el permiso de superposición", Toast.LENGTH_LONG).show()
            return
        }

        Store.setActive(this, true)
        startForegroundService(
            Intent(this, OverlayService::class.java).setAction(OverlayService.ACTION_REFRESH)
        )
        Toast.makeText(this, "Panel activado. Abre Waze.", Toast.LENGTH_SHORT).show()
    }
}
