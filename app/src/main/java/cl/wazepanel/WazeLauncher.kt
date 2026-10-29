package cl.wazepanel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Abre Waze y arranca la navegación usando sus enlaces profundos (waze://). */
object WazeLauncher {
    private const val WAZE_PACKAGE = "com.waze"
    private val COORDS = Regex("""^-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?$""")

    /**
     * - "fav:Casa"        -> favorito guardado en Waze con ese nombre
     * - "-33.45,-70.66"   -> coordenadas
     * - cualquier otro    -> búsqueda de dirección o lugar
     */
    fun uriFor(target: String): String {
        val t = target.trim()
        return when {
            t.startsWith("fav:", ignoreCase = true) ->
                "waze://?favorite=${Uri.encode(t.substring(4).trim())}&navigate=yes"
            COORDS.matches(t) ->
                "waze://?ll=${t.replace(" ", "")}&navigate=yes"
            else ->
                "waze://?q=${Uri.encode(t)}&navigate=yes"
        }
    }

    fun navigate(c: Context, target: String) = open(c, uriFor(target))

    fun openWaze(c: Context) {
        val intent = c.packageManager.getLaunchIntentForPackage(WAZE_PACKAGE)
        if (intent == null) {
            Toast.makeText(c, "Waze no está instalado", Toast.LENGTH_LONG).show()
            return
        }
        c.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun open(c: Context, uri: String) {
        val base = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            c.startActivity(Intent(base).setPackage(WAZE_PACKAGE))
        } catch (e: ActivityNotFoundException) {
            try {
                c.startActivity(base)
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(c, "Waze no está instalado", Toast.LENGTH_LONG).show()
            }
        }
    }
}
