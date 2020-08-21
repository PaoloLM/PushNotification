package com.desarrollo.pushnotification.SERVICIO;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.desarrollo.pushnotification.ACTIVIDADES.MainActivity;
import com.desarrollo.pushnotification.CLASES.Configuracion;
import com.desarrollo.pushnotification.UTILIDADES.Notificacion;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private Notificacion notification;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("NEW TOKEN", s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Cuerpo de la Notificacion: " + remoteMessage.getNotification().getBody());

            //TODO: manejo de la notificacion
            manejarNotificacion(remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Carga de data: " + remoteMessage.getData().toString());
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                //TODO: interpretar el JASON
                interpretarMensaje(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Excepsion: " + e.getMessage());
            }
        }
    }

    private void manejarNotificacion(String mensaje) {
        if (!Notificacion.isAppIsInBackground(getApplicationContext())) {
            Intent pushNotification = new Intent(Configuracion.PUSH_NOTIFICATION);
            pushNotification.putExtra("mensaje", mensaje);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            Notificacion notificationUtils = new Notificacion(getApplicationContext());
            notificationUtils.playNotificationAlarm();
        }
    }

    private void interpretarMensaje(JSONObject jsonObject) {
        Log.e(TAG, "push JSON: " + jsonObject.toString());

        try {

            JSONObject datos = jsonObject.getJSONObject("data");
            String titulo = datos.getString("title");
            String mensaje = datos.getString("message");
            boolean isBackground = datos.getBoolean("is_background");
            String urlimagen = datos.getString("image");
            String timeStamp = datos.getString("timestamp");
            JSONObject payload = datos.getJSONObject("payload");

            Log.e(TAG, "titulo: " + titulo);
            Log.e(TAG, "mensaje: " + mensaje);
            Log.e(TAG, "background: " + isBackground);
            Log.e(TAG, "imagen: " + urlimagen);
            Log.e(TAG, "timeStamp: " + timeStamp);
            Log.e(TAG, "payload: " + payload.toString());

            if (!Notificacion.isAppIsInBackground(getApplicationContext())) {
                Intent pushNotification = new Intent(Configuracion.PUSH_NOTIFICATION);
                pushNotification.putExtra("mensaje", mensaje);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                notification = new Notificacion(getApplicationContext());
                notification.playNotificationAlarm();
            } else {
                Intent resultintent = new Intent(getApplicationContext(), MainActivity.class);
                resultintent.putExtra("mensaje", mensaje);
                if (TextUtils.isEmpty(urlimagen)) {
                    notification.showNotificationMessage(titulo, mensaje,timeStamp, resultintent);
                } else {
                    resultintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    notification.showNotificationMessage(titulo,mensaje,timeStamp, resultintent, urlimagen);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
