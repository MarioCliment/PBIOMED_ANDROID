package org.mario.btle_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;


// ------------------------------------------------------------------
// ------------------------------------------------------------------
public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String TAG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;


    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private String server = "http://192.168.1.133:80/prueba/rest/guardarMedicion.php";

    private TextView elTexto;

    private ObjetoDeDosEnteros ValoresGuardados;

    private int concentracion;

    private int temperatura;

    private boolean enviado = false;



    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
        }
    }


    ;

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void buscarTodosLosDispositivosBTLE() {
        Log.d(TAG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(TAG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(TAG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(TAG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(TAG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " buscarTodosLosDispositivosBTL(): SCAN FAILURE! ");

            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
            requestBluetoothScan();
            return;
        }

        Log.d(TAG, " buscarTodosLosDispositivosBTL(): SCAN READY! ");
        this.elEscanner.startScan(this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(TAG, " ****************************************************");
        Log.d(TAG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(TAG, " ****************************************************");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " mostrarInformacionDispositivoBTLE(): CONNECT FAILURE! ");
            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
            requestBluetoothConnect();
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


        // AQUI SACAMOS EL TEXTO POR PANTALLA

        // Nombre
        TextView nombreTextView = findViewById(R.id.datosBluetoothNombre);
        String nombreString = "Nombre: " + bluetoothDevice.getName();
        nombreTextView.setText(nombreString);

        // Bytes de major
        TextView majorTextView = findViewById(R.id.datosBluetoothMajor);
        String majorString = "Major: " + Utilidades.bytesToInt(tib.getMajor());
        majorTextView.setText(majorString);

        // Bytes de minor
        TextView minorTextView = findViewById(R.id.datosBluetoothMinor);
        String minorString = "Minor: " + Utilidades.bytesToInt(tib.getMinor());
        minorTextView.setText(minorString);

        // Bytes de major y minor
        int major = Utilidades.bytesToInt(tib.getMajor());
        int minor = Utilidades.bytesToInt(tib.getMinor());

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


    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(TAG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(TAG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(TAG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                TextView scanTextView = findViewById(R.id.scaneoStatus);
                BluetoothDevice bluetoothDevice = resultado.getDevice();
                Log.d(TAG, " buscarEsteDispositivoBTLE():  ¿dispositivoBuscado = " + dispositivoBuscado + " equivale a bluetoothDevice.getName() = "+ bluetoothDevice.getName() + " ?");
                // Este mostrarInformacion es para hacer debugging
                //mostrarInformacionDispositivoBTLE(resultado);

                // AQUI ES CUANDO FILTRAMOS Y ENCONTRAMOS NUESTRO DISPOSITIVO
                if (Objects.equals(bluetoothDevice.getAddress(), dispositivoBuscado)) {
                    scanTextView.setText("Escaneo con éxito");
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo " +dispositivoBuscado+ " encontrado");
                    mostrarInformacionDispositivoBTLE(resultado);;


                    if(ValoresGuardados.getID() == 11){
                        concentracion = ValoresGuardados.getVALOR();
                        enviado = false;
                    }
                    else if(ValoresGuardados.getID() == 12 && enviado == false){
                        temperatura = ValoresGuardados.getVALOR();

                        String tiempo = obtenerFechaConFormato();

                        // Crear los datos de la solicitud
                        String data = "tiempo=" + tiempo +"&temperatura=" + temperatura + "&concentracion=" + concentracion;

                        PeticionarioREST elPeticionario = new PeticionarioREST();
                        elPeticionario.hacerPeticionREST("POST", server, data,
                                new PeticionarioREST.RespuestaREST() {
                                    @Override
                                    public void callback(int codigo, String cuerpo) {
                                        elTexto = findViewById(R.id.elTexto);
                                        String text = "codigo respuesta= " + codigo + "<-> \n" + cuerpo;
                                        elTexto.setText(text);
                                    }
                                });
                        enviado = true;
                    }


                } else {
                    Log.d(TAG, " buscarEsteDispositivoBTLE(): dispositivo no encontrado");
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " buscarEsteDispositivoBTLE(): SCAN FAILURE! ");
            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
            requestBluetoothScan();
            return;
        }
        Log.d(TAG, " buscarEsteDispositivoBTLE(): SCAN READY! ");
        this.elEscanner.startScan(this.callbackDelEscaneo);
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void detenerBusquedaDispositivosBTLE() {

        if (this.callbackDelEscaneo == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " detenerBusquedaDispositivosBTLE(): SCAN FAILURE! ");
            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
            requestBluetoothScan();
            return;
        }
        Log.d(TAG, " detenerBusquedaDispositivosBTLE(): SCAN READY! ");
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(TAG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(TAG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );
        //this.buscarEsteDispositivoBTLE( "VECTOR_GP77_13V" );
        this.buscarEsteDispositivoBTLE( "D2:5C:EB:10:7A:80" );
        //this.buscarEsteDispositivoBTLE("fistro");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(TAG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void inicializarBlueTooth() {
        Log.d(TAG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " inicializarBlueTooth(): CONNECT FAILURE! ");

            requestBlePermissions(this, CODIGO_PETICION_PERMISOS);
            requestBluetoothConnect();
            return;
        }
        Log.d(TAG, " inicializarBlueTooth(): CONNECT READY! ");
        bta.enable();

        Log.d(TAG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(TAG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(TAG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(TAG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(TAG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(TAG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Solicitar permisos BLE automáticamente al iniciar la actividad
        requestBlePermissions(this, CODIGO_PETICION_PERMISOS);

        Log.d(TAG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(TAG, " onCreate(): termina ");

    } // onCreate()

    private void requestBluetoothScan() {
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN};
            requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_SCAN);
        }
        else {
            Log.d(TAG, " Permiso Scan Denegado");
        }
    }

    private void requestBluetoothConnect() {
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT};
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

} // class

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
