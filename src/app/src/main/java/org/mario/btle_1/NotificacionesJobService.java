package org.mario.btle_1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificacionesJobService extends JobService {

    private static final String TAG = "NotificacionesJobService";
    private boolean jobCancelled = false;
    private NotificationManager notificationManager;
    private boolean sensorFuncional = true;
    private int medicionOzono = 90;
    static int SensorApagadoNotificacion = 1;
    static int SensorEstropeadoNotificacion = 2;
    static int ConcentracionAltaNotificacion = 3; // La notificación debe incluir fecha, hora y GPS, y hacer un sonido (en teoria hace esto ultimo)
    static final String CANAL_ID = "MedioambienteProyecto";
    @Override
    // Se llama a esta función cuando se empieza un trabajo (creo)
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Aqui empieza el "timer" y el actualizar las notificaciones
        manejarNotificaciones(jobParameters);
        return true;
    }

    // Esta es la función que junta el JobService y las notificaciones, así que, ¡es de suma importancia!
    private void manejarNotificaciones(JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Is job cancelled?: " +jobCancelled);
                if (jobCancelled){
                    return;
                }
                if(medicionOzono > 70) {
                    lanzarNotificacionDebil("Contaminación alta",
                            "¡Estás en una zona de con mucho ozono!",
                            ConcentracionAltaNotificacion);
                    // TODO: Añadir código que manda a la base de datos la localización GPS,
                    //  la fecha y la hora, y por supuesto, la medición del ozono
                } else {
                    cancelarNotificacion(ConcentracionAltaNotificacion);
                }
                // Si el sensor no funciona
                if(!sensorFuncional) {
                    lanzarNotificacionFuerte("Sonda no funcional",
                            "La sonda no está activa. Comprueba si está apagada, sin batería, o averiada",
                            SensorApagadoNotificacion);
                } else {
                    cancelarNotificacion(SensorApagadoNotificacion);
                }

                Log.d(TAG, "Job finished");
                // params siempre se pasa, pero el boolean es para saber si deberiamos volver a empezar el método
                // De momento lo dejaré en true
                jobFinished(params, true);
            }
        }).start();
    }

    // La notificacion fuerte NO la puede quitar el usuario manualmente (debe resolver el problema)
    public void lanzarNotificacionFuerte(String tituloNotificacion, String textoNotificacion, int idNotificacion) {
        Log.d(TAG, "empieza la función lanzarNotificacion()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notificacion =
                    new NotificationCompat.Builder(this, CANAL_ID)
                            // esto cambia el titulo de la notificacion
                            .setContentTitle(tituloNotificacion)
                            // esto cambia el texto de la notificacion
                            .setContentText(textoNotificacion)
                            // aqui se cambia la foto de la notificacion
                            .setSmallIcon(R.drawable.sans)
                            // Con el grupo agrupamos todas las notificaciones en una solapa (no se si funciona)
                            .setGroup("SuperTortosaBros")
                            .setOngoing(true)
                            .setOnlyAlertOnce(true);
            PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
            notificacion.setContentIntent(intencionPendiente);
            Log.d(TAG, "creando notificacion con id: " + idNotificacion);
            notificationManager.notify(idNotificacion, notificacion.build());
        } else {
            Log.d(TAG, "La version es muy vieja o algo asi");
        }
    }

    // La notificacion débil SÍ la puede quitar el usuario manualmente
    public void lanzarNotificacionDebil(String tituloNotificacion, String textoNotificacion, int idNotificacion) {
        Log.d(TAG, "empieza la función lanzarNotificacion()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Mis Notificaciones",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Descripcion del canal");
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notificacion =
                    new NotificationCompat.Builder(this, CANAL_ID)
                            // esto cambia el titulo de la notificacion
                            .setContentTitle(tituloNotificacion)
                            // esto cambia el texto de la notificacion
                            .setContentText(textoNotificacion)
                            // aqui se cambia la foto de la notificacion
                            .setSmallIcon(R.drawable.andrew)
                            // Con el grupo agrupamos todas las notificaciones en una solapa (no se si funciona)
                            .setGroup("SuperTortosaBros")
                            .setOngoing(false)
                            .setOnlyAlertOnce(true);
            PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
            notificacion.setContentIntent(intencionPendiente);
            Log.d(TAG, "creando notificacion con id: " + idNotificacion);
            notificationManager.notify(idNotificacion, notificacion.build());
        } else {
            Log.d(TAG, "La version es muy vieja o algo asi");
        }
    }
    private void cancelarNotificacion(int notificacionId) {
        try {
            Log.d(TAG, "Cancelando notificacion " + notificacionId);
            notificationManager.cancel(notificacionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    // Se llama a esta función cuando el trabajo se INTERRUMPE, no cuando termina con éxito
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}