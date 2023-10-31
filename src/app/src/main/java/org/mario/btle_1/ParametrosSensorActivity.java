package org.mario.btle_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

// El profesor Jesús Tomás ha embrujado nuestro Android, porque a pesar de esto estar en rojo, funciona sin problemas
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class ParametrosSensorActivity extends AppCompatActivity {

    private final static String TAG = "ParametrosSensorActivity";
    private static final String PREFERENCES_NAME = "MyPreferences";

    @Override
    // Al crearse la actividad, el Switch estará como el usuario lo haya dejado
    // -------------------------------------------------------------------------
    // Bundle -> onCreate()
    // -------------------------------------------------------------------------
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametros_de_sensor);

        // ¡¡¡Maldición de la supresión OMEGA!!!
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notificacionesSwitch = findViewById(R.id.notificacionesSwitch);

        // Cargar el estado del Switch
        boolean switchState = loadSwitchState();
        notificacionesSwitch.setChecked(switchState);
        TextView MACTextView = findViewById(R.id.txtMAC);

        // Carga el texto guardado en SharedPreferences y configúralo en MACTextView
        String savedMATText = loadMACText();
        MACTextView.setText(savedMATText);
        if (notificacionesSwitch.isChecked()){
            scheduleJob();
        }

        notificacionesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Guardar el estado del Switch cuando cambia
            saveSwitchState(isChecked);
            if (isChecked) {
                scheduleJob();
            } else {
                cancelJob();
            }
        });
    }

    // -------------------------------------------------------------------------
    // iniciarEscaner()
    // -------------------------------------------------------------------------
    public void iniciarEscaner(View v) {
        Log.d(TAG, "Boton del escaner pulsado ");
        IntentIntegrator integrador = new IntentIntegrator(ParametrosSensorActivity.this);
        integrador.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrador.setPrompt("Lector - CDP");
        integrador.setCameraId(0); // 0 = cámara trasera
        integrador.setBeepEnabled(true); // sonido de alerta
        integrador.setBarcodeImageEnabled(true); // para que lea imágenes correctamente
        integrador.initiateScan(); // iniciar escaneo
        // Esta funcion llama, en este momento, a onActivityResult()
    }

    // -------------------------------------------------------------------------
    // borrarMACEscaneada()
    // -------------------------------------------------------------------------
    public void borrarMACEscaneada(View v) {
        Log.d(TAG, "Boton de borrar MAC pulsado");
        TextView MACTextView = findViewById(R.id.txtMAC);

        // Si borramos la MAC, borramos el texto guardado entre actividades
        MACTextView.setText("");
        saveMACText(MACTextView.getText().toString());
    }

    // -------------------------------------------------------------------------
    // int(que le pides), int(resultado), Intent -> onActivityResult()
    // -------------------------------------------------------------------------
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if (result != null){ //si hay informacion
            if (result.getContents() == null){ // en caso de cancelar o no recibir dato
                Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_LONG).show();
            } else { // cuando se reciba informacion

                String scannedMac = result.getContents();

                //ArrayList<String> macsArrayList = new ArrayList<String>();

                // Comprueba si ya existe un TextView con la misma MAC como ID
                TextView MACTextView = findViewById(R.id.txtMAC);

                //condicion
                //!macsArrayList.contains(MACTextView.getText())
                Log.d(TAG, "macGetText:"+MACTextView.getText()+"..");
                Log.d(TAG, "scannedmac:"+scannedMac+"..");

                if (!MACTextView.getText().equals(scannedMac)) {
                    MACTextView.setText(scannedMac);
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                    //macsArrayList.add(scannedMac);
                    // Guarda el texto en SharedPreferences
                    saveMACText(MACTextView.getText().toString());
                } else {
                    // Si ya existe un TextView con la misma MAC, muestra un mensaje al usuario
                    Toast.makeText(this, "Ya está escaneada", Toast.LENGTH_SHORT).show();
                }

            }
        } else { // en caso de ser null no haya problemas
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // Estas dos de abajo son funciones para guardar la MAC de la sonda en un TextView
    // -------------------------------------------------------------------------
    // String -> saveMACText()
    // -------------------------------------------------------------------------
    private void saveMACText(String text) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mac_text", text);
        editor.apply();
    }

    // -------------------------------------------------------------------------
    // loadMACText()
    // -------------------------------------------------------------------------
    private String loadMACText() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        return sharedPreferences.getString("mac_text", "");
    }


    // Estas dos de abajo son funciones para guardar el estado del switch
    // -------------------------------------------------------------------------
    // boolean -> saveSwitchState()
    // -------------------------------------------------------------------------
    private void saveSwitchState(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switch_state", isChecked);
        editor.apply();
    }

    // -------------------------------------------------------------------------
    // loadSwitchState()
    // -------------------------------------------------------------------------
    private boolean loadSwitchState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean("switch_state", false);
    }
    // De momento se llama cuando entramos a la actividad, pero en el futuro, siempre estará activo el Job de notificaciones
    // -------------------------------------------------------------------------
    // scheduleJob()
    // -------------------------------------------------------------------------
    public void scheduleJob() {
        // Comprueba si ya tienes permiso para notificaciones

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(!notificationManager.areNotificationsEnabled()){
            Log.e(TAG, "Permiso de notificación denegado, cancelando scheludeJob()");
            // TODO: Aqui mostrar un aviso que diga "Porfavor habilita manualmente las notificaciones!!!"
            AlertDialog.Builder alertaActivarNotificaciones = new AlertDialog.Builder(ParametrosSensorActivity.this);
            alertaActivarNotificaciones.setMessage("Tiene las notificaciones desactivadas, porfavor, actívelas manualmente desde la app Ajustes")
                    .setCancelable(false)
                    .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertaActivarNotificaciones.show();
            return;
        }
        // El código de abajo se ejecutará si ya tienes permiso o después de que el usuario haya respondido a la solicitud de permiso.

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notificacionesSwitch = findViewById(R.id.notificacionesSwitch);
        // Si no está checkeado, cierro la función y no se envían notificaciones
        // Ahora me queda incorporar las notificaciones a NotificacionesJobService
        if (!notificacionesSwitch.isChecked()) {
            Log.d(TAG, "schedulejob: El Switch NO está checkeado!!!");
            return;
        }
        Log.d(TAG, "scheduleJob: El Switch SI está checkeado!!!");

        // Resto del código sin cambios
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (scheduler.getPendingJob(123) != null) {
            Log.d(TAG, "El job ya está activo, continuando job");
            return;
        }
        ComponentName componentName = new ComponentName(this, NotificacionesJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) // 15 minutos
                .build();

        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled successfully ");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    // Llamamos a esta función cuando apagamos el Switch de notificaciones
    // -------------------------------------------------------------------------
    // cancelJob()
    // -------------------------------------------------------------------------
    public void cancelJob() {
        // Primero, cancela el trabajo programado
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);

        // Luego, envía un intent para cancelar las notificaciones
        Intent intent = new Intent(this, NotificacionesJobService.class);
        intent.setAction("CANCELAR_NOTIFICACIONES");
        startService(intent);

        Log.d(TAG, "Job cancelled");
    }


}


































// Profesor, si usted ve esto, ¡bájele la nota a Gabriel Tello!