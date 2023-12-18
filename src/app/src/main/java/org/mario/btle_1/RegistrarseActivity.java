package org.mario.btle_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import java.io.IOException;
import java.util.Properties;

public class RegistrarseActivity extends AppCompatActivity {

    private String server = "http://192.168.10.7:80/PBIOMED_SERVIDOR/src/rest"; // MOVIL MAYR0
    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; // CASA MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; // CASA GRASA

    private String server_registro = server + "/user/add";
    private boolean resultado = false;

    EditText password;

    EditText editTextTextPassword2;
    EditText correo;
    EditText nombre;

    EditText user;

    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        correo = findViewById(R.id.usuario);
        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2);
        user = findViewById(R.id.nickname);
        nombre = findViewById(R.id.nombre);
        password = findViewById(R.id.contrasenya);
        loginBtn = findViewById(R.id.botonLogin);

        loginBtn.setOnClickListener(v->registrarUsuario());
    }

    public void registrarUsuario() {
        String email = this.correo.getText().toString().trim();
        String password = this.password.getText().toString();
        if (validateData(email, password, this.editTextTextPassword2.getText().toString())) {
            hacerRegistro();
        }
    }

    void hacerRegistro(){
        String userS = this.user.getText().toString().trim();
        String nombreS = this.nombre.getText().toString().trim();
        String emailS = this.correo.getText().toString().trim();
        String passwordS = this.password.getText().toString();

        JSONObject objeto = new JSONObject();
        try {
            objeto.put("email", emailS);
            objeto.put("contrasenya", passwordS);
            objeto.put("nombreApellidos", nombreS);
            objeto.put("nickname", userS);
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
                                AlertDialog.Builder registroExitoso = new AlertDialog.Builder(RegistrarseActivity.this);
                                registroExitoso.setMessage("Se ha registrado con éxito, verifique su correo")
                                        .setCancelable(false)
                                        .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                                IrALoginActivity();
                                            }
                                        });
                                registroExitoso.show();
                            } else {
                                AlertDialog.Builder registroFallido = new AlertDialog.Builder(RegistrarseActivity.this);
                                registroFallido.setMessage("Ha ocurrido un error, contacte con soporte")
                                        .setCancelable(false)
                                        .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        });
                                registroFallido.show();
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



    private void IrALoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public boolean validateData(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.correo.setError("El email no es valido");
            return false;
        } else if (password.length() < 6) {
            this.password.setError("La contraseña es muy corta");
            return false;
        } else if (password.equals(confirmPassword)) {
            return true;
        } else {
            this.editTextTextPassword2.setError("Las contraseñas no coinciden");
            return false;
        }
    }
}