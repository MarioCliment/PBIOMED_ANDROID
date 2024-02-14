package org.mario.btle_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class EditPassActivity extends AppCompatActivity {
    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; //CASA MAYRO


    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; //CASA GRASA // Ermmm porque esta mi casa grabada??? Lo reportare como una brecha de privacidad, je!
    private String server = "http://172.20.10.2:80/PBIOMED_SERVIDOR/src/rest"; //MOVIL MAYRO

    EditText emailTextView;
    boolean logueado;
    private boolean resultado = false;
    private String server_registro = server + "/user/password";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        emailTextView = findViewById(R.id.cambiarPassEmailTextView);
    }

    public void botonCancelar(View view){
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    // HOLA MARIO!!! SALUDA!!! SALUDA DE VUELTA ESCRIBIENDO UN COMENTARIO AQUI ABAJO!!!
    //
    public void enviarEmailButton(View view) {
        AlertDialog.Builder alertaEstadoEmail = new AlertDialog.Builder(EditPassActivity.this);
        String email = this.emailTextView.getText().toString().trim();
        if (validateData(email)) {
            // funcionar
            if (true) { // reemplazar este "true" por "correoEnviadoConExito", que comprueba si hemos enviado un correo o si hemos fallado
                // mostrar "El correo se ha enviado con exito"
                alertaEstadoEmail.setMessage("El correo se ha enviado con éxito, compruebe su e-mail ")// yuh uh!
                        .setCancelable(false)
                        .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                irALogin();
                            }
                        });
                alertaEstadoEmail.show();
            } else {
                // mostrar "Ha ocurrido un error"
                alertaEstadoEmail.setMessage("No se ha podido enviar el e-mail, inténtelo más tarde") // nuh uh!
                        .setCancelable(false)
                        .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                alertaEstadoEmail.show();
            }

        } else {
            // mostrar en pantalla "El email esta escrito incorrectamente!"
            alertaEstadoEmail.setMessage("El email esta escrito incorrectamente ") // Meeeeec!
                    .setCancelable(false)
                    .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            alertaEstadoEmail.show();
        }
    }


    /*public void guardarPass(View view){
        if(newpasstext.getText() == newpasstext2.getText()){
            if(true){ // aqui antes ponia ''oldpasstext.getText().toString() == oldpassword'', pero ya no usamos "oldpassword"
                JSONObject objeto = new JSONObject();
                try {
                    objeto.put("contrasenya", newpasstext.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String data = objeto.toString();

                PeticionarioREST elPeticionario = new PeticionarioREST();
                elPeticionario.hacerPeticionREST("PUT", server_registro, data,
                        new PeticionarioREST.RespuestaREST() {
                            @Override
                            public void callback(int codigo, String cuerpo) {
                                String jsonString = cuerpo;
                                try {
                                    // Convierte la cadena JSON en un objeto JSON
                                    JSONObject jsonObject = new JSONObject(jsonString);

                                    // Extrae datos específicos del objeto JSON
                                    resultado = jsonObject.getBoolean("resultado");
                                    Log.d("resultado", "" + resultado);
                                    if (resultado) {
                                        Log.d("registro", "Registrado con éxito");
                                        avisoPass.setText("Contraseña cambiada con exito");
                                        avisoPass.setVisibility(View.VISIBLE);
                                        avisoPass.setTextColor(Color.parseColor("#000000"));
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
            else{ // este else no deberia ocurrir nunca jamas
                avisoPass.setText("Contraseña incorrecta");
                avisoPass.setVisibility(View.VISIBLE);
                avisoPass.setTextColor(Color.parseColor("#C62222"));
            }
        }
        else{
            avisoPass.setText("Las contraseñas no coinciden");
            avisoPass.setVisibility(View.VISIBLE);
            avisoPass.setTextColor(Color.parseColor("#C62222"));
        }
    }*/

    public boolean validateData(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.emailTextView.setError("El email no es valido");
            return false;
        }
        return true;
    }
}
