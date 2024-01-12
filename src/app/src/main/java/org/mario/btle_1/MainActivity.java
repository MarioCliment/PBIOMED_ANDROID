package org.mario.btle_1;

import static org.mario.btle_1.BackgroundJobService.obtenerFechaConFormato;

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
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.UUID;


// ------------------------------------------------------------------
// ------------------------------------------------------------------
public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String TAG = ">>>>";

    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; // CASA MAYRO

    private String server = "http://172.20.10.2:80/PBIOMED_SERVIDOR/src/rest"; // MOVIL MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; //CASA GRASA


    private double latitud;
    private double longitud;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private ObjetoDeDosEnteros ValoresGuardados;

    private WebView webView;
    private LinearLayout panelButtons;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonIrParametrosSensor(View v) {
        Intent intent = new Intent(this, ParametrosSensorActivity.class);
        startActivity(intent);
    }

    public void botonIrLarva(View v) {
        Intent intent = new Intent(this, Dato2Activity.class);
        startActivity(intent);
    }

    public void botonIrATorty(View v) {
        Intent intent = new Intent(this, TortyActivity.class);
        startActivity(intent);
    }
    public void botonAEditar(View v){
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
    }

    public void botorIrMain(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void falsaSonda(View v){
        ValoresGuardados = new ObjetoDeDosEnteros( 12, 22);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();
                    Log.d("Lugar", "Latitud: " + latitud + ", Longitud: " + longitud);
                }
            }
        };
        requestLocationUpdates();

        String tiempo = obtenerFechaConFormato();
        String userS = LoginActivity.getUser();
        String latitud_longitud = latitud+","+longitud;

        JSONObject objeto = new JSONObject();
        try {
            objeto.put("nickname", userS);
            objeto.put("fecha", tiempo);
            objeto.put("valor", ValoresGuardados.getVALOR());
            objeto.put("idTipoMedicion", ValoresGuardados.getID());
            objeto.put("lugar", latitud_longitud);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String data = objeto.toString();

        String server_especifico= server+ "/user/measure/add";

        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("POST", server_especifico, data,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String text = "codigo respuesta= " + codigo + "<-> \n" + cuerpo;
                        Log.d(TAG, "callback: "+text);

                    }
                });
    }


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview_map);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://172.20.10.2:80/PBIOMED_SERVIDOR/src/ux/mapas.html");

        panelButtons = (LinearLayout) findViewById(R.id.panel_buttons);

        Button menuButton = (Button) findViewById(R.id.button_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (panelButtons.getVisibility() == View.GONE) {
                    // Si el panel está oculto, lo mostramos
                    panelButtons.setVisibility(View.VISIBLE);
                } else {
                    // Si el panel está visible, lo ocultamos
                    panelButtons.setVisibility(View.GONE);
                }
            }
        });
        // Solicitar permisos BLE automáticamente al iniciar la actividad
    } // onCreate()

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                   LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // Intervalo de actualización en milisegundos
        locationRequest.setFastestInterval(5000); // Intervalo más rápido en milisegundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

} // class

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
