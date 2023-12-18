package org.mario.btle_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import kotlinx.coroutines.Job;

public class BackgroundJobService extends JobService {

    private static final String TAG = "NotificacionesJobService";
    private boolean jobCancelled = false;
    private NotificationManager notificationManager;
    private boolean sensorFuncional = false;
    private int medicionOzono = 0;
    static int SensorApagadoNotificacion = 1;
    static int SensorEstropeadoNotificacion = 2;
    static int ConcentracionAltaNotificacion = 3; // La notificación debe incluir fecha, hora y GPS, y hacer un sonido (en teoria hace esto ultimo)
    static final String CANAL_ID = "MedioambienteProyecto";
    private int contadorFuncionamientoSonda = 0;


    // Para el scan bluetooth -----------------------------------------------
    private String MAC_BUSCADA;

    private ObjetoDeDosEnteros ValoresGuardados;

    private int concentracion;

    private int temperatura;

    private boolean enviado = false;

    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;



    private String server = "http://192.168.10.7:80/PBIOMED_SERVIDOR/src/rest"; //MOVIL MAYRO

    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; // CASA MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; //CASA GRASA

    private double latitud=1;
    private double longitud=1;


    @SuppressLint("MissingPermission")
    @Override
    // Es importante inicializar aqui el notificationManager,
    // puesto que la funcion cancelarTodasLasNotificaciones es llamada desde otras clases
    // -------------------------------------------------------------------------
    // onCreate()
    // -------------------------------------------------------------------------
    public void onCreate() {
        super.onCreate();
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        // MALDICION DE LA SUPRESION!!! bta.enable() puede que de problemitas je!
        bta.enable();
        this.elEscanner = bta.getBluetoothLeScanner();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); // Inicializa el notificationManager aquí


        }
    @Override
    // De momento, esto es para que cancelarTodasLasNotificaciones sea llamado, pero puede servir para un futuro
    // -------------------------------------------------------------------------
    // Intent -> onStartCommand()
    // -------------------------------------------------------------------------
    // Sé que onStartCommand() le entran y devuelve más cosas, pero no son cosas que yo le paso o le pido, por lo tanto, no las incluyo en el diseño
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "CANCELAR_TODO".equals(intent.getAction())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Log.i(TAG, "cancelarTodo: cancelando!!! ");
                    jobCancelled = true;
                    cancelarTodasLasNotificaciones();
                    detenerBusquedaDispositivosBTLE();
                }
            }).start();

        }
        return START_NOT_STICKY;
    }

    @Override
    // Se llama a esta función cuando se empieza un trabajo
    // -------------------------------------------------------------------------
    // JobParameters -> onStartJob() -> boolean
    // -------------------------------------------------------------------------
    // Lo que tenga que ver con jobs suele devolver un true o false. Esto es simplemente si el trabajo debería empezar de nuevo cuando este acabe
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");
        jobCancelled = false;
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        PersistableBundle datosJob = jobParameters.getExtras();
        MAC_BUSCADA = datosJob.getString("MAC");
        // Esto maneja los datos del scan
        hayQueMoverElCacharro(jobParameters);

        // Esto maneja las notificaciones (tiene un delay de 5 segundos)
        manejarNotificaciones(jobParameters);
        return true;
    }


    private void hayQueMoverElCacharro(JobParameters params) {
        // Hmm... He de mover el cacharro, ¡si señor!
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Is mover el cacharro job cancelled?: " +jobCancelled);
                if (jobCancelled){
                    detenerBusquedaDispositivosBTLE();
                    return;
                }
                buscarEsteDispositivoBTLE(MAC_BUSCADA);
                Log.d(TAG, "Job finished");
                // params siempre se pasa, pero el boolean es para saber si deberiamos volver a empezar el método
                // De momento lo dejaré en true
                jobFinished(params, true);
            }
        }).start();
    }

    // Esta es la función que junta el JobService y las notificaciones, así que, ¡es de suma importancia!
    // -------------------------------------------------------------------------
    // JobParameters -> manejarNotificaciones() -> boolean
    // -------------------------------------------------------------------------
    private void manejarNotificaciones(JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // hacemos que duerma 5 segundos para que le de tiempo a mi MIERDa de codigo a leer correctamente si la sonda es funcional o no
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Log.d(TAG, "Is manejar notificaiocnes job cancelled?: " +jobCancelled);
                if (jobCancelled){
                    return;
                }
                // Esto es temporal, no se si habrá una condición
                if(medicionOzono != -1) {
                    lanzarNotificacionDebil("Contaminación alta",
                            "¡Estás en una zona de con mucho ozono! " + medicionOzono,
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
    // -------------------------------------------------------------------------
    // String, String, int -> lanzarNotificacionFuerte()
    // -------------------------------------------------------------------------
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
                            .setSmallIcon(R.drawable.andrew)
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
    // -------------------------------------------------------------------------
    // String, String, int -> lanzarNotificacionDebil()
    // -------------------------------------------------------------------------
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
    // -------------------------------------------------------------------------
    // int -> cancelarNotificacion()
    // -------------------------------------------------------------------------
    // Le pasamos un int (id) y cancela la notificacion que tiene esa id (si no está activa la notificacion, no ocurre nada)
    private void cancelarNotificacion(int notificacionId) {
        try {
            Log.d(TAG, "Cancelando notificacion " + notificacionId);
            notificationManager.cancel(notificacionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // -------------------------------------------------------------------------
    // cancelarTodasLasNotificaciones()
    // -------------------------------------------------------------------------
    // Creo que se explica sola. La llamamos para cuando apagamos las notificaciones,
    // para que no solo dejen de llegar notificaciones, sino que además CANCELE las que están presentes
    public void cancelarTodasLasNotificaciones() {
        try {
            Log.d(TAG, "Cancelando TODAS las notificaciones");
            notificationManager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // ScanResult -> mostrarInformacionDispositivoBTLE()
    // -------------------------------------------------------------------------

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(TAG, " ****************************************************");
        Log.d(TAG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(TAG, " ****************************************************");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, " mostrarInformacionDispositivoBTLE(): CONNECT FAILURE! ");
                return;
            }
        }


        Log.d(TAG, " mostrarInformacionDispositivoBTLE(): CONNECT READY! ");
        Log.d(TAG, " nombre = " + bluetoothDevice.getName());
        Log.d(TAG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(TAG, " uuid = " + puuids[0].getUuid());
           // Log.d(TAG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(TAG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(TAG, " rssi = " + rssi);

        Log.d(TAG, " bytes = " + new String(bytes));
        Log.d(TAG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(TAG, " ----------------------------------------------------");
        Log.d(TAG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(TAG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(TAG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(TAG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(TAG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(TAG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(TAG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(TAG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(TAG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(TAG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(TAG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(TAG, " ****************************************************");


        // Bytes de major y minor
        int major = Utilidades.bytesToInt(tib.getMajor());
        int minor = Utilidades.bytesToInt(tib.getMinor());

        // TODO: Cambiar medicionOzono por el valor actual de la medicion

        Log.i(TAG, "mostrarInformacionDispositivoBTLE: major, minor = "+major +", " +minor);
        ValoresGuardados = new ObjetoDeDosEnteros( major, minor);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------


    // --------------------------------------------------------------
    //  obtenerFechaConFormato() --> String
    // --------------------------------------------------------------
    @SuppressLint("SimpleDateFormat")
    public static String obtenerFechaConFormato() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(date);
    }


    // -------------------------------------------------------------------------
    // String(una MAC) -> buscarEsteDispositivoBTLE() -> ScanResult
    // -------------------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(TAG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(TAG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                if(jobCancelled) {
                    Log.d(TAG, "Detener scaneo porque se acabo el trabajo");
                    return;
                }
                Log.d(TAG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                BluetoothDevice bluetoothDevice = resultado.getDevice();
                Log.d(TAG, " buscarEsteDispositivoBTLE():  ¿dispositivoBuscado = " + dispositivoBuscado + " equivale a bluetoothDevice.getName() = "+ bluetoothDevice.getName() + " ?");
                // Este mostrarInformacion es para hacer debugging
                // mostrarInformacionDispositivoBTLE(resultado);
                // AQUI ES CUANDO FILTRAMOS Y ENCONTRAMOS NUESTRO DISPOSITIVO
                Log.d(TAG, "MAC DE ZAIDA ES: " + bluetoothDevice.getAddress());
                if (Objects.equals(bluetoothDevice.getAddress(), dispositivoBuscado)) {
                    yeLaSondaNoFunciona(true);
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo " +dispositivoBuscado+ " encontrado");
                    mostrarInformacionDispositivoBTLE(resultado);
                    if(ValoresGuardados.getID() == 11){


                        String tiempo = obtenerFechaConFormato();
                        String userS = LoginActivity.getUser();
                        String latitud_longitud = latitud+","+longitud;

                        JSONObject objeto = new JSONObject();
                        try {
                            objeto.put("nickname", userS);
                            objeto.put("fecha", tiempo);
                            objeto.put("valor", ValoresGuardados.getVALOR());
                            objeto.put("id", ValoresGuardados.getID());
                            objeto.put("lugar", latitud_longitud);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String data = objeto.toString();

                        String server_especifico= server+ "user/measure/add";

                        PeticionarioREST elPeticionario = new PeticionarioREST();
                        elPeticionario.hacerPeticionREST("POST", server_especifico, data,
                                new PeticionarioREST.RespuestaREST() {
                                    @Override
                                    public void callback(int codigo, String cuerpo) {
                                        String text = "codigo respuesta= " + codigo + "<-> \n" + cuerpo;
                                        Log.d(TAG, "callback: "+text);

                                    }
                                });
                        enviado = false;
                    }
                    else if(ValoresGuardados.getID() == 12 && enviado == false){


                        String tiempo = obtenerFechaConFormato();
                        String userS = LoginActivity.getUser();
                        String latitud_longitud = latitud+","+longitud;

                        JSONObject objeto = new JSONObject();
                        try {
                            objeto.put("nickname", userS);
                            objeto.put("fecha", tiempo);
                            objeto.put("valor", ValoresGuardados.getID());
                            objeto.put("id", ValoresGuardados.getID());
                            objeto.put("lugar", latitud_longitud);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String data = objeto.toString();

                        String server_especifico= server+ "user/measure/add";

                        PeticionarioREST elPeticionario = new PeticionarioREST();
                        elPeticionario.hacerPeticionREST("POST", server_especifico, data,
                                new PeticionarioREST.RespuestaREST() {
                                    @Override
                                    public void callback(int codigo, String cuerpo) {
                                        String text = "codigo respuesta= " + codigo + "<-> \n" + cuerpo;
                                        Log.d(TAG, "callback: "+text);

                                    }
                                });
                        enviado = true;
                    }


                } else {
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo no encontrado");
                    yeLaSondaNoFunciona(false);
                    //detenerBusquedaDispositivosBTLE();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(TAG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        ScanFilter sf = new ScanFilter.Builder().setDeviceName(dispositivoBuscado).build();

        Log.d(TAG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);
        //Log.d(TAG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado
        //      + " -> " + Utilidades.stringToUUID( dispositivoBuscado ) );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, " buscarEsteDispositivoBTLE(): SCAN FAILURE! ");
                return;
            }

        }
        Log.d(TAG, " buscarEsteDispositivoBTLE(): SCAN READY! ");
        this.elEscanner.startScan(this.callbackDelEscaneo);

    } // ()

    // -------------------------------------------------------------------------
    // detenerBusquedaDispositivosBTLE()
    // -------------------------------------------------------------------------
    // Lo que hace esta función es un COMPLETO MISTERIO!!! (¿Estará la función embrujada...?) (Mis compañeros de proyecto se mosquearán si no aviso que esto es ironía)
    private void detenerBusquedaDispositivosBTLE() {

        if (this.callbackDelEscaneo == null) {
            Log.d(TAG, " detenerBusquedaDispositivosBTLE(): SCAN NULL! ");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " detenerBusquedaDispositivosBTLE(): SCAN FAILURE! ");
            return;
        }
        Log.d(TAG, " detenerBusquedaDispositivosBTLE(): SCAN READY! ");

        // TODO: Mario, tu "stopScan" no podría parar un puño aunque quisiera,
        //      se ejecuta este código (comprobado porque detenerBusquedaDispositivosBTLE(): SCAN READY! sale en el logcat)
        //      pero no se para el scaneo, la unica manera de detenerlo es cerrando la app por completo
        //      ...
        //      He probado un poco, la primera vez que se llama a esta funcion dice SCAN NULL!
        //      Si te sirve de algo...
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // ()

    private void yeLaSondaNoFunciona(boolean funciona) {
        Log.d(TAG, "yeLaSondaNoFunciona: sensorFuncional = " + sensorFuncional);
        if(funciona) {
            contadorFuncionamientoSonda = 0;
            sensorFuncional = true;
        } else {
            Log.d(TAG, "yeLaSondaNoFunciona: contadorFuncionamientoSonda = " + contadorFuncionamientoSonda);
            if(contadorFuncionamientoSonda >= 5) {
                sensorFuncional = false;
            } else{
                contadorFuncionamientoSonda++;
            }
        }
    }


    @Override
    // Se llama a esta función cuando el trabajo se INTERRUMPE, no cuando termina con éxito
    // -------------------------------------------------------------------------
    // JobParameters -> onStopJob() -> boolean
    // -------------------------------------------------------------------------
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion");
        detenerBusquedaDispositivosBTLE();
        jobCancelled = true;
        return true;
    }


}