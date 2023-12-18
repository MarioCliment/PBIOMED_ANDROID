package org.mario.btle_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

// El profesor Jesús Tomás ha embrujado nuestro Android, porque a pesar de esto estar en rojo, funciona sin problemas
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class ParametrosSensorActivity extends AppCompatActivity {

    private final static String TAG = "ParametrosSensorActivity";
    private static final String PREFERENCES_NAME = "MyPreferences";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;


    // estos son para la cercania de la sonda al movil
    private boolean haSidoPulsado = false;
    private int resultadoCercania = -1; // si se queda en -1 significa que ha fallado
    private Handler handler = new Handler();


    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };


    // DOGSHIT MARIO PARA LA DOGSHIT DE TORTOSA

    private ObjetoDeDosEnteros ValoresGuardados;

    private boolean enviado = false;

    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;


    @SuppressLint("MissingPermission")
    @Override
    // Al crearse la actividad, el Switch estará como el usuario lo haya dejado
    // -------------------------------------------------------------------------
    // Bundle -> onCreate()
    // -------------------------------------------------------------------------
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(isBluetoothEnabled()) {
            BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
            // MALDICION DE LA SUPRESION!!! bta.enable() puede que de problemitas je!
            bta.enable();
            this.elEscanner = bta.getBluetoothLeScanner();
        } else {
            AlertDialog.Builder alertaActivarBluetooth = new AlertDialog.Builder(ParametrosSensorActivity.this);
            alertaActivarBluetooth.setMessage("Para que la app pueda funcionar, necesita el bluetooth activo. Porfavor, active el bluetooth ")
                    .setCancelable(false)
                    .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertaActivarBluetooth.show();
            requestBluetoothEnable(this);
        }

        setContentView(R.layout.activity_parametros_de_sensor);



        Log.d(TAG, " onCreate(): termina ");
        // ¡¡¡Maldición de la supresión OMEGA!!!
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch sondaSwitch = findViewById(R.id.sondaSwitch);

        // Cargar el estado del Switch
        boolean switchState = loadSwitchState();
        sondaSwitch.setChecked(switchState);
        TextView MACTextView = findViewById(R.id.txtMAC);

        // Carga el texto guardado en SharedPreferences y configúralo en MACTextView
        String savedMATText = loadMACText();
        MACTextView.setText(savedMATText);

        // No necesitamos de momento este código, el servicio se debería iniciar con
        // 1.- La vinculación de una sonda
        // 2.- Activar las notificaciones
        /*
        if (sondaSwitch.isChecked()){
            scheduleJob();
        }*/


        sondaSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Guardar el estado del Switch cuando cambia
            saveSwitchState(isChecked);
            if (isChecked) {
                if (hayMACVinculada()) {
                    // TODO: Tengo que cambiar que esto vaya asi, las notificaciones ahora se cambian desde los ajustes
                    // TODO: del telefono
                    scheduleJob();
                } else {
                    // Desactivamos el switch...
                    desactivarSwitchNotificaciones();
                    // Y creamos una alerta al usuario de que no hay ninguna sonda vinculada
                    AlertDialog.Builder alertaNoHaySondaVinculada = new AlertDialog.Builder(ParametrosSensorActivity.this);
                    alertaNoHaySondaVinculada.setMessage("No hay ninguna sonda vinculada, porfavor, vincule su sonda con el botón Vincular Sonda ")
                            .setCancelable(false)
                            .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    alertaNoHaySondaVinculada.show();
                    // Puede que este cancelJob no sea necesario...
                    cancelJob();
                }


            } else{
                cancelJob();
            }

        });
    }

    // Check if Bluetooth is enabled on the device
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // Prompt the user to enable Bluetooth if it's not already enabled
    @SuppressLint("MissingPermission")
    public static void requestBluetoothEnable(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }

    public boolean hayMACVinculada() {
        TextView MACTextView = findViewById(R.id.txtMAC);
        // Si MACTextView tiene texto, devuelve true, en caso contrario, devuelve false
        if(MACTextView.getText() != ""){
            Log.d(TAG, "hayMACVinculada: ");
            return true;
        } else {
            Log.d(TAG, "NOhayMACVinculada: ");
            return false;
        }

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

    public void desactivarSwitchNotificaciones(){
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notificacionesSwitch = findViewById(R.id.sondaSwitch);
        // Desactivar Switch notificaciones
        if (notificacionesSwitch.isChecked()){
            notificacionesSwitch.setChecked(false);
        }
    }
    // De momento se llama cuando entramos a la actividad, pero en el futuro, siempre estará activo el Job de notificaciones
    // -------------------------------------------------------------------------
    // scheduleJob()
    // -------------------------------------------------------------------------

    // ¡¡¡Hola Gabriel!!! ¡¡¡Saluda de vuelta!!!
    // HOLA!!!! HOLA GABRIEL!!!!

    public void botorIrMain(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void scheduleJob() {

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch sondaSwitch = findViewById(R.id.sondaSwitch);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
        }
        inicializarBlueTooth();
        // Comprueba si ya tienes permiso para notificaciones

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(!notificationManager.areNotificationsEnabled()){
            Log.e(TAG, "Permiso de notificación denegado, no habrán notificaciones");
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
        }
        // Si no está checkeado, cierro la función y no se envían notificaciones
        // Ahora me queda incorporar las notificaciones a NotificacionesJobService
        if (!sondaSwitch.isChecked()) {
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

        PersistableBundle datosJob = new PersistableBundle();
        datosJob.putString("MAC", loadMACText());

        ComponentName componentName = new ComponentName(this, BackgroundJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) // 15 minutos
                .setExtras(datosJob)
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
        Intent intent = new Intent(this, BackgroundJobService.class);
        intent.setAction("CANCELAR_TODO");
        startService(intent);

        Log.d(TAG, "Job cancelled");
    }

    private void requestBluetoothScan() {
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{android.Manifest.permission.BLUETOOTH_SCAN};
            requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_SCAN);
        }
        else {
            Log.d(TAG, " Permiso Scan Denegado");
        }
    }

    private void requestBluetoothConnect() {
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{android.Manifest.permission.BLUETOOTH_CONNECT};
            requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_CONNECT);

        }
        else {
            Log.d(TAG, " Permiso Connetc Denegado");
        }
    }


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(TAG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");
                    requestBlePermissions(this, CODIGO_PETICION_PERMISOS);

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void inicializarBlueTooth() {
        Log.d(TAG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, " inicializarBlueTooth(): habilitamos adaptador BT ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, " inicializarBlueTooth(): CONNECT FAILURE! ");

                requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
                requestBluetoothConnect();
                return;
            }
        }

        Log.d(TAG, " inicializarBlueTooth(): CONNECT READY! ");
        bta.enable();

        Log.d(TAG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(TAG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(TAG, " inicializarBlueTooth(): obtenemos escaner btle ");

        BluetoothLeScanner elEscanner = bta.getBluetoothLeScanner();

        if ( elEscanner == null ) {
            Log.d(TAG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(TAG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    ParametrosSensorActivity.this,
                    new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(TAG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // ()

    // -------------------------------------------------------------------------
    // Activity, int -> requestBlePermissions()
    // -------------------------------------------------------------------------
    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
        }
    }


    // Hasta que no haya una manera mejor de hacer esto, lo dejo por aqui...
    // LOCALIZAR SONDA
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " mostrarInformacionDispositivoBTLE(): CONNECT FAILURE! ");
            return;
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
    private void encontrarCercaniaDispositivoBuscado(final String dispositivoBuscado) {
        Log.d(TAG, " encontrarCercaniaDispositivoBuscado(): empieza ");
        this.callbackDelEscaneo = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(TAG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                BluetoothDevice bluetoothDevice = resultado.getDevice();
                //Log.d(TAG, " buscarEsteDispositivoBTLE():  ¿dispositivoBuscado = " + dispositivoBuscado + " equivale a bluetoothDevice.getName() = "+ bluetoothDevice.getName() + " ?");
                // Este mostrarInformacion es para hacer debugging
                // mostrarInformacionDispositivoBTLE(resultado);
                // AQUI ES CUANDO FILTRAMOS Y ENCONTRAMOS NUESTRO DISPOSITIVO
                //Log.d(TAG, "MAC DE ZAIDA ES: " + bluetoothDevice.getAddress());
                if (Objects.equals(bluetoothDevice.getAddress(), dispositivoBuscado)) {
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo " +dispositivoBuscado+ " encontrado");
                    byte[] bytes = resultado.getScanRecord().getBytes();
                    TramaIBeacon tib = new TramaIBeacon(bytes);
                    // TODO: Cambiar este getTxPower por algo que realmente demuestre la distancia al sensor
                    if (tib.getTxPower() > 60) {
                        resultadoCercania = 3; // cerca
                    } if (tib.getTxPower() > 30) {
                        resultadoCercania = 2; // medio
                    } else {
                        resultadoCercania = 1; // lejos
                    }

                } else {
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo no encontrado");
                    //detenerBusquedaDispositivosBTLE();
                    resultadoCercania = 0; //esto significa no encontrado :(
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
                Log.d(TAG, "buscarEsteDispositivoBTLE(): SCAN FAILURE! ");
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

        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // ()


    private Runnable actualizarRunnable = new Runnable() {
        @Override
        public void run() {
            encontrarCercaniaDispositivoBuscado(loadMACText());
            actualizarBuscarTexto();
            if (haSidoPulsado) {
                handler.postDelayed(this, 500);
            }
        }
    };

    public void botonBuscarSonda(View v) {
        haSidoPulsado = !haSidoPulsado;
        Log.d(TAG, "botonBuscarSonda: haSidoPulsado = " + haSidoPulsado);

        if (haSidoPulsado) {
            handler.post(actualizarRunnable);
        } else {
            handler.removeCallbacks(actualizarRunnable);
            resultadoCercania = -2;
            actualizarBuscarTexto();
        }
    }

    private void actualizarBuscarTexto() {
        // Tu lógica para actualizar el TextView
        Log.d(TAG, "botonBuscarSonda: resultadoCercanias = " + resultadoCercania);
        TextView localizarTextView = findViewById(R.id.localizarTextView);
        switch (resultadoCercania){
            case -2:
                localizarTextView.setText("");
                break;
            case -1:
                localizarTextView.setText("Fallo en escaneo");
                break;
            case 0:
                localizarTextView.setText("Fuera de rango");
                break;
            case 1:
                localizarTextView.setText("Lejos");
                break;
            case 2:
                localizarTextView.setText("Medio");
                break;
            case 3:
                localizarTextView.setText("Cerca");
                break;
        }
    }


}


































// Profesor, si usted ve esto, ¡bájele la nota a Gabriel Tello!