package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

        Switch notificacionesSwitch = findViewById(R.id.notificacionesSwitch);

        // Cargar el estado del Switch
        boolean switchState = loadSwitchState();
        notificacionesSwitch.setChecked(switchState);
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

        Button btnScan = findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Boton del escaner pulsado ");
                IntentIntegrator integrador = new IntentIntegrator(ParametrosSensorActivity.this);
                integrador.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrador.setPrompt("Lector - CDP");
                integrador.setCameraId(0); //0 = camara trasera
                integrador.setBeepEnabled(true); //sonido de alerta
                integrador.setBarcodeImageEnabled(true); //para que lea imagenes correctamente
                integrador.initiateScan(); //iniciar escaneo
            }
        });
    }

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
                } else {
                    // Si ya existe un TextView con la misma MAC, muestra un mensaje al usuario
                    Toast.makeText(this, "Ya está escaneada", Toast.LENGTH_SHORT).show();
                }

            }
        } else { // en caso de ser null no haya problemas
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        // ¡Bah! ¡¿Me está intentando sugerir que yo podría equivocarme en mi codigo?! ¡¡¡Maldición de la supresión!!!
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notificacionesSwitch = findViewById(R.id.notificacionesSwitch);
        // Si no está checkeado, cierro la función y no se envian notificaciones
        // Ahora me queda incorporar las notificaciones a NotificacionesJobService
        if (!notificacionesSwitch.isChecked()){
            Log.d(TAG, "schedulejob: El Switch NO está checkeado!!!");
            return;
        }
        Log.d(TAG, "scheduleJob: El Switch SI está checkeado!!!");
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if(scheduler.getPendingJob(123) != null){
            Log.d(TAG, "El job ya está activo, continuando job");
            return;
        }
        ComponentName componentName = new ComponentName(this, NotificacionesJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) // 15 minutos (como mínimo, si ponemos menos, simplemente nos pondrá 15 minutos automaticamente)
                .build();

        // Este apartado nos informará de si se ha conseguido poner en marcha el trabajo
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled successfully ");
        }
        else{
            // no deberia ocurrir
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