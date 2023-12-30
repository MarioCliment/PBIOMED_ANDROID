package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class EditPassActivity extends AppCompatActivity {
    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; //CASA MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; //CASA GRASA
    private String server = "http://192.168.10.7:80/PBIOMED_SERVIDOR/src/rest"; //MOVIL MAYRO

    EditText newpasstext;
    EditText newpasstext2;
    TextView avisoPass;
    private boolean resultado = false;
    private String server_registro = server + "/user/password";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        newpasstext = findViewById(R.id.NewPass);
        newpasstext2 = findViewById(R.id.NewPass2);
        avisoPass = findViewById(R.id.avisoPass);

    }
    public void cancelarPass(View view){
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
    }
    public void guardarPass(View view){
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
    }
}
