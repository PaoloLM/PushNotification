package com.desarrollo.pushnotification.ACTIVIDADES;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.desarrollo.pushnotification.CLASES.Configuracion;
import com.desarrollo.pushnotification.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    TextView txtRegId, txtMensaje;
    BroadcastReceiver broadcastReceiver;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRegId = findViewById(R.id.reg_id);
        txtMensaje = findViewById(R.id.txt_mensaje);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String nuevoTOKEN = instanceIdResult.getToken();
                Log.e("NEWTOKE: ", nuevoTOKEN);
                //TODO: Tenemos el token
                almacenarPreferencia(nuevoTOKEN);
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Configuracion.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Configuracion.TOPIC_GLOBAL);
                    //TODO: MOSTRAR ID
                    mostrarFirebaseId();

                } else if (intent.getAction().equals(Configuracion.PUSH_NOTIFICATION)) {
                    String mensaje = intent.getStringExtra("mensaje");
                    Toast.makeText(getApplicationContext(), "Notificacion Push: " + mensaje, Toast.LENGTH_SHORT).show();
                    txtMensaje.setText(mensaje);
                }
            }
        };

        mostrarFirebaseId();
    }

    private void  almacenarPreferencia(String token) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Configuracion.SHARED_PREF, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("REGID", token);
        editor.commit();
    }

    private void mostrarFirebaseId() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Configuracion.SHARED_PREF, 0);
        String regId = sharedPreferences.getString("REGID", null);
        Log.e(TAG, "Firebase Id: " + regId);
        if (!TextUtils.isEmpty(regId)) {
            txtRegId.setText("Firebase ID: " + regId);
        } else {
            txtRegId.setText("No existe una respuesta de Firebase aún");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver,
                        new IntentFilter(Configuracion.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver,
                        new IntentFilter(Configuracion.PUSH_NOTIFICATION));
        //TODO: clearNotification
        clearNotification();
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}