package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Dato2Activity extends AppCompatActivity {

    private String server = "http://172.20.10.2:80/PBIOMED_SERVIDOR/src/rest"; //MOVIL MAYRO

    //private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; //CASA MAYRO

    //private String server = "http://192.168.1.148:80/PBIOMED_SERVIDOR/src/rest"; //CASA GRASA

    private String server_temperatura = server + "/user/measure/last/temp";
    private String server_ozono = server + "/user/measure/last/ozo";

    TextView temp;
    TextView hum;



    String userS = LoginActivity.getUser();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dato2);

        temp = findViewById(R.id.temperatura);
        hum = findViewById(R.id.ozono);


    }
    public void funcion1(){
        JSONObject objeto = new JSONObject();
        try {
            objeto.put("nickname", userS);
            objeto.put("idTipoMedicion",12);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String data = objeto.toString();
        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("GET", server_temperatura, data,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String jsonString = cuerpo;
                        String valor = "";


                        try {
                            String editedString = jsonString;
                            editedString = editedString.substring(1, editedString.length() - 1);

                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(editedString);

                            // Extrae datos específicos del objeto JSON y los imprime en
                            // los textviews

                            //Extraer email
                            valor = jsonObject.getString("valor");
                            temp.setText(valor);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Maneja errores de análisis JSON aquí
                        }
                    }
                });
    }

    public void funcion2(){
        JSONObject objeto = new JSONObject();
        try {
            objeto.put("nickname", userS);
            objeto.put("idTipoMedicion",11);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String data = objeto.toString();
        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("GET", server_ozono, data,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String jsonString = cuerpo;
                        String valor = "";


                        try {
                            String editedString = jsonString;
                            editedString = editedString.substring(1, editedString.length() - 1);

                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(editedString);

                            // Extrae datos específicos del objeto JSON y los imprime en
                            // los textviews

                            //Extraer email
                            valor = jsonObject.getString("valor");
                            hum.setText(valor);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Maneja errores de análisis JSON aquí
                        }
                    }
                });
    }
}