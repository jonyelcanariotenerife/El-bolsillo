package com.quantumfinance.bolsillo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.json.JSONObject;

/**
 * Widget nativo de la pantalla de inicio para Bolsillo (FASE 8 de la hoja de ruta).
 *
 * Por qué no lee localStorage directamente: el WebView de Capacitor guarda
 * localStorage en un almacén privado (WebView data dir) al que este código
 * nativo no tiene acceso directo/documentado. En su lugar lee la
 * SharedPreferences que escribe @capacitor/preferences desde el lado JS
 * (ver window.BolsilloServices.widget.sync en index.html), bajo la clave
 * "bolsillo_widget_data", con un JSON como:
 *
 *   {"disponibleAhora":12.34,"gastoHoy":5.0,"restanteTotalMes":120.5,
 *    "presupuestoRealActual":80.0,"gastosSemanaActual":30.0,
 *    "diasRestantesCiclo":9,"actualizadoEn":"2026-08-06T10:00:00.000Z"}
 *
 * IMPORTANTE — verifica esto tras el primer build real:
 * El nombre de fichero de SharedPreferences ("CapacitorStorage") es el que
 * usa @capacitor/preferences en las versiones 6.x. Si tras instalar la app
 * el widget se queda en "Abre Bolsillo", abre Android Studio →
 * View → Tool Windows → Device File Explorer → data/data/<tu.appId>/shared_prefs/
 * y comprueba el nombre real del archivo .xml que aparece ahí; si difiere,
 * cambia PREFS_FILE por ese nombre.
 */
public class BolsilloWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_FILE = "CapacitorStorage";
    private static final String PREFS_KEY = "bolsillo_widget_data";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Público y estático para que BolsilloWidgetPlugin pueda forzar un
     * refresco inmediato desde JS sin esperar al ciclo automático de Android.
     */
    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_bolsillo);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        String raw = prefs.getString(PREFS_KEY, null);

        if (raw == null) {
            views.setTextViewText(R.id.widget_disponible, "Abre Bolsillo");
            views.setTextViewText(R.id.widget_gasto_hoy, "para cargar datos");
            views.setTextViewText(R.id.widget_restante_mes, "");
        } else {
            try {
                JSONObject data = new JSONObject(raw);
                double disponibleAhora = data.optDouble("disponibleAhora", 0);
                double gastoHoy = data.optDouble("gastoHoy", 0);
                double restanteTotalMes = data.optDouble("restanteTotalMes", 0);
                int diasRestantesCiclo = data.optInt("diasRestantesCiclo", -1);

                views.setTextViewText(R.id.widget_disponible, String.format("%.2f€", disponibleAhora));
                views.setTextViewText(R.id.widget_gasto_hoy, String.format("Hoy: %.2f€", gastoHoy));

                String restanteLine = String.format("Mes: %.2f€", restanteTotalMes);
                if (diasRestantesCiclo >= 0) {
                    restanteLine += String.format(" · %dd", diasRestantesCiclo);
                }
                views.setTextViewText(R.id.widget_restante_mes, restanteLine);
            } catch (Exception e) {
                views.setTextViewText(R.id.widget_disponible, "Error de datos");
                views.setTextViewText(R.id.widget_gasto_hoy, "");
                views.setTextViewText(R.id.widget_restante_mes, "");
            }
        }

        // Tocar el widget abre la app.
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
