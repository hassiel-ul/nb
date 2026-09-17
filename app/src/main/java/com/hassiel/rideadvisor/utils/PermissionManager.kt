package com.hassiel.rideadvisor.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

/**
 * Centraliza la comprobación y solicitud de los permisos que RideAdvisor
 * necesita (sección 11 del documento de requisitos). No se solicita ningún
 * permiso que no sea estrictamente necesario.
 */
object PermissionManager {

    /**
     * true si el usuario ya habilitó el acceso a notificaciones para esta app
     * (Notification Listener).
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        if (TextUtils.isEmpty(flat)) return false
        val names = flat.split(":")
        return names.any { it.contains(pkgName) }
    }

    /** true si la app tiene permiso para dibujar overlays sobre otras apps. */
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /** true si las notificaciones locales (POST_NOTIFICATIONS) están permitidas. */
    fun areNotificationsEnabled(context: Context): Boolean {
        return androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Abre la pantalla del sistema donde el usuario activa el Notification Listener. */
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Abre la pantalla del sistema donde el usuario activa "Mostrar sobre otras apps". */
    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Abre los ajustes de notificaciones de la app (Android 8+). */
    fun openAppNotificationSettings(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.parse("package:${context.packageName}")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** true si todos los permisos críticos para el funcionamiento están concedidos. */
    fun hasAllCriticalPermissions(context: Context): Boolean {
        return isNotificationListenerEnabled(context) && canDrawOverlays(context)
    }
}
