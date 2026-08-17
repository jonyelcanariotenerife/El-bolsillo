package com.quantumfinance.bolsillo;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Puente entre el JS de la app (window.Capacitor.Plugins.BolsilloWidget.refresh())
 * y el widget nativo. Fuerza un refresco inmediato de todas las instancias del
 * widget que haya en pantalla, sin esperar al intervalo automático que impone
 * Android (updatePeriodMillis, en la práctica ~30 min mínimo aunque se pida menos).
 *
 * Se registra manualmente en MainActivity.java con registerPlugin(...) porque
 * vive dentro del propio módulo app (no es un paquete npm) — ver README-FASE8.md.
 */
@CapacitorPlugin(name = "BolsilloWidget")
public class BolsilloWidgetPlugin extends Plugin {

    @PluginMethod
    public void refresh(PluginCall call) {
        Context context = getContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, BolsilloWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);

        for (int id : ids) {
            BolsilloWidgetProvider.updateWidget(context, manager, id);
        }

        call.resolve();
    }
}
