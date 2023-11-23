package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class EditUserActivity extends AppCompatActivity {
    Button botonguardar = findViewById(R.id.editarBotonGuardar);
    TextView textoTutorial = findViewById(R.id.textViewTutorialEditar);
    private String server = "http://192.168.45.7:80/ozonewarden/rest/";
    private String server_registro = server + "/user/add";
    TextView textViewNombreEdit = findViewById(R.id.textViewNombreEdit);
    TextView textViewApellidoEdit = findViewById(R.id.textViewApellidoEdit);
    TextView textViewNicknameEdit = findViewById(R.id.textViewNicknameEdit);
    TextView textViewEmailEdit = findViewById(R.id.textViewEmailEdit);
    private boolean resultado = false;
    private String server_especifico = "http://localhost/PBIOMED_SERVIDOR/src/rest/index.php/user/data";
    //PARA QUE FUNCIONE CANCELAR
    String nombreEditPre = "";
    String nicknameEditPre = "";
    String emailEditPre = "";
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        String data = "";
        //esto seguro que funciona...
        //una funcion statica que solo se llama cuando se va desde el login?
        String usuario = LoginActivity.getUser();
        //HOLA MARIO CLIMENT!!
        // se abre la pagina y tengo mi nombre,apellidos,email y nickname disponibles para que los pueda mirar
        //
        PeticionarioREST elPeticionario = new PeticionarioREST();
        elPeticionario.hacerPeticionREST("GET", server_especifico, usuario,
                new PeticionarioREST.RespuestaREST() {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        String jsonString = cuerpo;
                        String emailDatabase = "";
                        String nombreDatabase = "";
                        String apellidoDatabase = "";
                        String nicknameDatabase = "";

                        try {
                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonString);

                            // Extrae datos específicos del objeto JSON y los imprime en
                            // los textviews

                            //Extraer email
                            emailDatabase = jsonObject.getString("email");
                            Log.d("email es ", "" + emailDatabase);
                            textViewEmailEdit.setText(emailDatabase);

                            //Extraer nombre
                            nombreDatabase = jsonObject.getString("nombre");
                            Log.d("nombre es ", "" + nombreDatabase);
                            textViewNombreEdit.setText(nombreDatabase);

                            //Extraer apellido
                            apellidoDatabase = jsonObject.getString("apellido");
                            Log.d("apellido es ", "" + apellidoDatabase);
                            textViewNombreEdit.setText(apellidoDatabase);

                            //Extraer nickname
                            nicknameDatabase = jsonObject.getString("nickname");
                            Log.d("nickname es ", "" + nicknameDatabase);
                            textViewNicknameEdit.setText(nicknameDatabase);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Maneja errores de análisis JSON aquí
                        }
                    }
                });
    }

    public void botonEditar(View view) {
        //La animacion para que se ponga azul AUN NO VA!!!
        /*
        int colorFrom = getResources().getColor(R.color.white);
        int colorTo = getResources().getColor(R.color.blue); // el azul
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds habria que cambiar tambien la font
        ConstraintLayout constraintLayout = findViewById(R.id.consEdit);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                constraintLayout.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
         */
        //PARA QUE FUNCIONE CANCELAR
        nombreEditPre = textViewNombreEdit.getText().toString();
        nicknameEditPre = textViewNicknameEdit.getText().toString();
        emailEditPre = textViewEmailEdit.getText().toString();
        //BOTONES VISIBLES
        botonguardar.setVisibility(View.VISIBLE);
        textoTutorial.setVisibility(View.VISIBLE);
    }

    public void guardarEditar(View view) {
        //UHHHH

        //BOTONES FUERA!
        botonguardar.setVisibility(View.GONE);
        textoTutorial.setVisibility(View.GONE);
        String userS = textViewNicknameEdit.getText().toString();
        String nombreS = textViewNombreEdit.getText().toString();
        String emailS = textViewEmailEdit.getText().toString();


        JSONObject objeto = new JSONObject();
        try {
            objeto.put("email", emailS);
            objeto.put("nombreApellidos", nombreS);
            objeto.put("nickname", userS);
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

    public void cancelarEditar(View view) {
        textViewNicknameEdit.setText(nicknameEditPre);
        textViewNombreEdit.setText(nombreEditPre);
        textViewEmailEdit.setText(emailEditPre);
        //BOTONES FUERA!
        botonguardar.setVisibility(View.GONE);
        textoTutorial.setVisibility(View.GONE);
    }
}

