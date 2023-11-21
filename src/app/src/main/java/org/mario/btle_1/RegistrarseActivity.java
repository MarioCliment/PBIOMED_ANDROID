package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrarseActivity extends AppCompatActivity {

    private String server = "http://192.168.88.7:80/ozonewarden/rest/";

    private String server_registro = server + "usuario";
    private boolean resultado = false;

    EditText password;
    EditText user;

    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        user = findViewById(R.id.usuario);
        password = findViewById(R.id.contrasenya);
        loginBtn = findViewById(R.id.botonLogin);

        loginBtn.setOnClickListener(v->hacerRegistro());
    }

    void hacerRegistro(){
        String userS = this.user.getText().toString().trim();
        String passwordS = this.password.getText().toString();

        JSONObject objeto = new JSONObject();
        try {
            objeto.put("user", userS);
            objeto.put("password", passwordS);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String data = objeto.toString();

        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("POST", server_registro, data,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String jsonString = cuerpo;
                        try {
                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonString);

                            // Extrae datos específicos del objeto JSON
                            resultado = jsonObject.getBoolean("resultado");
                            Log.d("resultado",""+resultado);
                            if (resultado == true){
                                Log.d("registro","Registrado con éxito");
                            }

                            // Haz algo con los datos extraídos
                            // Por ejemplo, muestra los datos en una vista

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Maneja errores de análisis JSON aquí
                        }
                    }
                });
    }
}