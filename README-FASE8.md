# 🟣 FASE 8 — Widget de pantalla de inicio (Android nativo)

## Qué hace
Un widget real en el launcher de Android que muestra:
- **Disponible ahora** (semana actual)
- **Gasto de hoy**
- **Restante del mes** + días que quedan del ciclo de 28 días

Se actualiza:
1. Automáticamente cada ~30 min (mínimo real que permite Android), y
2. **Al instante** cada vez que abres la app y cambian los números (gracias
   al plugin nativo que añade este pack).

## Por qué no es "solo JS"
El `manifest.json` con propiedad `widgets` que menciona la hoja de ruta es
una propuesta experimental pensada para el *Widgets Board de Windows 11*;
Chrome Android no la soporta. Un widget real de Android necesita:
- Un `AppWidgetProvider` nativo (Kotlin/Java) que pinta el `RemoteViews`.
- Una fuente de datos que el código nativo pueda leer — por eso el lado JS
  ahora también guarda las cifras en `@capacitor/preferences` (que en Android
  usa `SharedPreferences`), no solo en `localStorage` (el WebView no expone
  `localStorage` al código nativo).

## Ya hice en tu `index.html`
- `window.BolsilloServices.widget.sync(payload)` → guarda el JSON de las
  cifras en Preferences y pide un refresco inmediato al plugin nativo.
- Un `useEffect` en el componente `App` que llama a `widget.sync(...)` cada
  vez que cambian `disponibleAhora`, `gastoHoy`, `restanteTotalMes`,
  `presupuestoRealActual`, `gastosSemanaActual` o `data.budgetReset`.
- Todo protegido con `Capacitor.isNativePlatform()`: en el navegador no hace
  nada, así que no rompe tu flujo de desarrollo web normal.

## Lo que tienes que hacer tú (una vez, sobre tu proyecto real)

### 1. Generar el proyecto Android si aún no existe
```bash
npm install
npx cap add android
```

### 2. Copiar los archivos nuevos (se pueden copiar tal cual, son archivos nuevos)
Copia estas carpetas/archivos de este pack dentro de tu proyecto, respetando
las mismas rutas:

```
fase8-widget-android/app/src/main/java/com/quantumfinance/bolsillo/BolsilloWidgetProvider.java
fase8-widget-android/app/src/main/java/com/quantumfinance/bolsillo/BolsilloWidgetPlugin.java
fase8-widget-android/app/src/main/res/layout/widget_bolsillo.xml
fase8-widget-android/app/src/main/res/drawable/widget_background.xml
fase8-widget-android/app/src/main/res/xml/bolsillo_widget_info.xml
```
→ van dentro de `android/` en tu proyecto (mismas subcarpetas).

**Nota sobre el paquete:** todos estos archivos usan el paquete
`com.quantumfinance.bolsillo`, el mismo `appId` que dejé en tu
`capacitor.config.ts`. Si cambiaste el `appId`, tienes que:
- Mover los `.java` a la carpeta `java/<tu/paquete/nuevo>/`.
- Cambiar la primera línea `package com.quantumfinance.bolsillo;` en ambos
  `.java` por tu paquete real.

### 3. Fusionar (a mano) los dos archivos que SÍ ya existen en tu proyecto

**`android/app/src/main/AndroidManifest.xml`**
Pega el contenido de `AndroidManifest.snippet.xml` dentro de tu etiqueta
`<application>...</application>` ya existente (al lado de tu `<activity>`).

**`android/app/src/main/java/.../MainActivity.java`**
Añade el `registerPlugin(BolsilloWidgetPlugin.class);` como se ve en
`MainActivity.java.ejemplo`. Si ya registras otros plugins propios ahí,
simplemente añade esa línea junto a las que ya tengas.

### 4. Sincronizar y compilar
```bash
npx cap sync android
npx cap open android
```
Desde Android Studio, ejecuta la app en un emulador o móvil real (▶️ Run).

### 5. Probarlo
1. Abre la app una vez (para que se generen datos en Preferences).
2. Ve a la pantalla de inicio de Android → mantén pulsado en un hueco vacío →
   **Widgets** → busca "Bolsillo" → arrástralo a la pantalla.
3. Debería mostrar tus cifras reales. Si aparece "Abre Bolsillo / para cargar
   datos", abre la app y vuelve a mirar el widget (o espera unos segundos,
   `refresh()` se dispara nada más cambiar los datos).

## Solución de problemas

**El widget se queda siempre en "Abre Bolsillo"**
Casi seguro es el nombre del fichero de SharedPreferences. Con la app
corriendo en un emulador/dispositivo: Android Studio → *View → Tool Windows
→ Device File Explorer* → `data/data/com.quantumfinance.bolsillo/shared_prefs/`.
Mira qué `.xml` hay ahí y compáralo con `PREFS_FILE = "CapacitorStorage"` en
`BolsilloWidgetProvider.java`. Si el nombre real es otro, cámbialo ahí.

**El widget no aparece en la lista de "Widgets" del launcher**
Revisa que el `<receiver>` esté dentro de `<application>` (no fuera) y que
`android:exported="true"` esté puesto — a partir de Android 12 es
obligatorio declararlo explícitamente o el sistema ignora el componente.

**Compila pero el texto no se actualiza en caliente**
Confirma que `registerPlugin(BolsilloWidgetPlugin.class)` está **antes** de
`super.onCreate(savedInstanceState)` en `MainActivity.java` — si va después,
el plugin no queda disponible para el JS a tiempo.

---

Cuando lo tengas compilando, dime cómo se ve y afinamos: tamaño de letra,
qué métricas mostrar, si quieres una barra de progreso del presupuesto
semanal dentro del propio widget (RemoteViews soporta `ProgressBar`), etc.
