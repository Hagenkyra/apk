package cl.wazepanel

import android.content.Context

data class Dest(val name: String, val target: String)

/** Guarda destinos, posición del panel y si el panel debe arrancar con la radio. */
object Store {
    private const val PREFS = "waze_panel"
    private const val KEY_DESTS = "dests"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val KEY_ACTIVE = "active"

    val defaults = listOf(
        Dest("Casa", "fav:Casa"),
        Dest("Trabajo", "fav:Trabajo")
    )

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun clean(s: String) = s.replace('\t', ' ').replace('\n', ' ').trim()

    fun load(c: Context): List<Dest> {
        val raw = prefs(c).getString(KEY_DESTS, null) ?: return defaults
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                Dest(parts[0], parts[1])
            } else null
        }
    }

    fun save(c: Context, list: List<Dest>) {
        val raw = list.joinToString("\n") { "${clean(it.name)}\t${clean(it.target)}" }
        prefs(c).edit().putString(KEY_DESTS, raw).apply()
    }

    /** Posición guardada del panel, o null si el usuario aún no lo ha movido. */
    fun position(c: Context): Pair<Int, Int>? {
        val p = prefs(c)
        return if (p.contains(KEY_X) && p.contains(KEY_Y)) p.getInt(KEY_X, 0) to p.getInt(KEY_Y, 0) else null
    }

    fun savePosition(c: Context, x: Int, y: Int) {
        prefs(c).edit().putInt(KEY_X, x).putInt(KEY_Y, y).apply()
    }

    fun isActive(c: Context) = prefs(c).getBoolean(KEY_ACTIVE, false)

    fun setActive(c: Context, active: Boolean) {
        prefs(c).edit().putBoolean(KEY_ACTIVE, active).apply()
    }
}
