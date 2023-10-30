package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class EscanerPlaca extends AppCompatActivity {

    private static final String TAG = "SCAN: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScan = findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Boton del escaner pulsado ");
                IntentIntegrator integrador = new IntentIntegrator(EscanerPlaca.this);
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
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                // TODO: Añadir la MAC escaneada a un textView generado por texto
                // Recuerda, si es la misma MAC, decir que "Ya está escaneada"
                // La ID del textView será la MAC, y en el futuro, el usuario podria cambiar el nombre
            }
        } else { // en caso de ser null no haya problemas
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
