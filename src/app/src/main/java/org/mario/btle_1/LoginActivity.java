package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private String server = "http://192.168.10.7:80/PBIOMED_SERVIDOR/src/rest"; //MOVIL MAYRO

    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest";  //CASA MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; // CASA GRASA

    private String server_especifico = server + "/user/login";
    private static String usuarioGlobal;
    private static String passwordGlobal;
    private boolean resultado = false;
    EditText password;
    EditText user;
    Button loginBtn;
    Button skipLoginBtn;

    TextView registrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = findViewById(R.id.usuario);
        password = findViewById(R.id.contrasenya);
        loginBtn = findViewById(R.id.botonLogin);
        skipLoginBtn = findViewById(R.id.evilButton);
        registrar = findViewById(R.id.irRegistrar);

        loginBtn.setOnClickListener(v->hacerLogin());
        skipLoginBtn.setOnClickListener(v->irMainActivity());


        TextView textView = findViewById(R.id.irRegistrar);

        registrar.setOnClickListener(v->irRegistro());
        // Texto completo
        String textoCompleto = "¿No tienes cuenta? Registrate";

        // Crear una SpannableString
        SpannableString spannableString = new SpannableString(textoCompleto);

        // Índices donde comienza y termina la palabra "Registrate"
        int startIndex = textoCompleto.indexOf("Registrate");
        int endIndex = startIndex + "Registrate".length();

        // Aplicar el estilo de color azul a la palabra "Registrate"
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Establecer el texto formateado en el TextView
        registrar.setText(spannableString);
    }

    void hacerLogin(){

        String userS = this.user.getText().toString().trim();
        String passwordS = this.password.getText().toString();

        //String data = "?user="+userS+"&password="+passwordS;

        JSONObject objeto = new JSONObject();
        try {
            objeto.put("nickname", userS);
            objeto.put("contrasenya", passwordS);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String data = objeto.toString();


        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("GET", server_especifico, data,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String jsonString = cuerpo;
                        try {
                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonString);
                            //EXTRAER USUARIO PARA GLOBAL
                            usuarioGlobal = jsonObject.getString("nickname");
                            //passwordGlobal = jsonObject.getString("contrasenya");
                            // Extrae datos específicos del objeto JSON
                            resultado = jsonObject.getBoolean("resultado");
                            Log.d("resultado",""+resultado);
                            if (resultado == true){
                                irMainActivity();
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

    void irMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
    }

    void irRegistro(){
        startActivity(new Intent(this, RegistrarseActivity.class));
    }
    public static String getUser() {
        return usuarioGlobal;
    }
    public static String getPassword() {
        return passwordGlobal;
    }
}