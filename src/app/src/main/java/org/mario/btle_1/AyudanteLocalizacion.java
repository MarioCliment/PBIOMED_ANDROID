package org.mario.btle_1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class AyudanteLocalizacion implements LifecycleObserver {

    // Interfaz para escuchar eventos de localización
    public interface EscuchadorLocalizacion {
        void onLocalizacionDisponible(double latitud, double longitud);
    }

    private final Context contexto;
    private final FusedLocationProviderClient clienteLocalizacion;
    private final LocationCallback callbackLocalizacion;
    private final Lifecycle cicloVida;
    private EscuchadorLocalizacion escuchadorLocalizacion;

    // Constructor que recibe el contexto y el ciclo de vida de la actividad o fragmento
    public AyudanteLocalizacion(Context contexto, Lifecycle cicloVida) {
        this.contexto = contexto;
        this.cicloVida = cicloVida;
        // Obtiene una instancia del proveedor de servicios de ubicación fusionada de Google Play
        this.clienteLocalizacion = LocationServices.getFusedLocationProviderClient(contexto);

        // Callback para manejar los resultados de las actualizaciones de ubicación
        this.callbackLocalizacion = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult resultadoLocalizacion) {
                if (resultadoLocalizacion == null) {
                    return;
                }
                // Itera sobre las ubicaciones disponibles y notifica al escuchador
                for (Location localizacion : resultadoLocalizacion.getLocations()) {
                    if (escuchadorLocalizacion != null) {
                        escuchadorLocalizacion.onLocalizacionDisponible(localizacion.getLatitude(), localizacion.getLongitude());
                    }
                }
            }
        };

        // Registra esta clase como un observador del ciclo de vida para gestionar las actualizaciones de ubicación
        this.cicloVida.addObserver(this);
    }

    // Establece el escuchador de eventos de localización
    public void setEscuchadorLocalizacion(EscuchadorLocalizacion escuchador) {
        this.escuchadorLocalizacion = escuchador;
    }

    // Inicia las actualizaciones de ubicación
    public void iniciarActualizacionesLocalizacion() {
        // Verifica si se tiene el permiso necesario para acceder a la ubicación
        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Solicita actualizaciones de ubicación con la solicitud configurada
            clienteLocalizacion.requestLocationUpdates(obtenerSolicitudLocalizacion(), callbackLocalizacion, Looper.getMainLooper());
        } else {
            // Lanza una excepción si el permiso no está concedido
            throw new SecurityException("Permiso de ubicación no concedido");
        }
    }

    // Configura y devuelve la solicitud de actualizaciones de ubicación
    private LocationRequest obtenerSolicitudLocalizacion() {
        LocationRequest solicitudLocalizacion = new LocationRequest();
        solicitudLocalizacion.setInterval(10000); // Intervalo entre actualizaciones en milisegundos
        solicitudLocalizacion.setFastestInterval(5000); // Intervalo más rápido en milisegundos
        solicitudLocalizacion.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Prioridad de alta precisión
        return solicitudLocalizacion;
    }

    // Método llamado cuando se destruye la actividad o fragmento asociado
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void detenerActualizacionesLocalizacion() {
        // Detiene las actualizaciones de ubicación
        clienteLocalizacion.removeLocationUpdates(callbackLocalizacion);
    }
}

