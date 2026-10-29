# Panel Waze

Panel flotante con atajos que se dibuja encima de Waze (Android 9, minSdk 26).

## Cómo compilar

1. Abre esta carpeta con **Android Studio** (File → Open). Él descarga Gradle y el SDK que falten.
2. Build → Build APK(s). El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.
3. Pásalo a la radio (pendrive o adb) e instálalo.

## Compilar sin Android Studio (GitHub Actions, gratis)

1. Crea una cuenta en github.com y un repositorio nuevo (rama `main`).
2. Sube todo el contenido de esta carpeta, incluida la carpeta oculta `.github`.
3. Ve a la pestaña **Actions** → **Build APK** → **Run workflow**.
4. Al terminar (unos 5 minutos), abre la ejecución y descarga **PanelWaze-apk** (viene dentro de un zip con `app-debug.apk`).

## Primer uso en la radio

1. Abre **Panel Waze**.
2. Toca **Conceder permiso de superposición** y activa el permiso.
   Si la radio no tiene esa pantalla, por adb:
   `adb shell appops set cl.wazepanel SYSTEM_ALERT_WINDOW allow`
3. Completa tus destinos y toca **Guardar y mostrar panel**.
4. Abre Waze: el panel queda encima del mapa.

## Tipos de destino

| Escribes | Qué hace |
|---|---|
| `fav:Casa` | Navega a tu favorito de Waze con ese nombre (el nombre debe ser idéntico al guardado en Waze) |
| `-33.4489,-70.6693` | Navega a esas coordenadas |
| `Costanera Center` | Busca esa dirección o lugar y navega |

## Botón «Dividir» (pantalla dividida)

1. En Panel Waze toca **Activar accesibilidad** y enciende el servicio «Panel Waze».
2. Abre una app (por ejemplo YouTube) y toca **Dividir** en el panel.
3. Si la radio soporta pantalla dividida, la app pasa a media pantalla y eliges la segunda (Waze).
   Si no la soporta, aparece el aviso «La radio no permitió dividir la pantalla»: el fabricante la desactivó
   y solo se puede forzar con adb o root.

## Uso del panel

- Botón **WAZE** (arriba): tocar contrae o expande; arrastrar mueve el panel (recuerda la posición).
- Botón **Abrir Waze**: trae Waze al frente.
- Si el panel estaba activo, vuelve a aparecer solo al encender la radio.
