package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class ParametrosSensorActivity extends AppCompatActivity {

    private final static String TAG = "ParametrosSensorActivity";
    private static final String PREFERENCES_NAME = "MyPreferences";

    @Override
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
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switch_state", isChecked);
        editor.apply();
    }

    private boolean loadSwitchState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean("switch_state", false);
    }
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