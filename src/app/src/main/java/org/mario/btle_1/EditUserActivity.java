package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class EditUserActivity extends AppCompatActivity {
    //private String server = "http://192.168.45.7:80/PBIOMED_SERVIDOR/src/rest/";

    private String server = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest"; //CASA MAYRO

    private String server_registro = server + "/user/edit";
    Button botonguardar;
    Button botonEditarBoton;
    Button botonCancelarBoton;
    Button botonPass;
    TextView textoTutorial;

    TextView textViewPasWrong;
    EditText textViewNombreEdit;
    EditText textViewApellidoEdit;
    EditText textViewNicknameEdit;
    EditText textViewEmailEdit;
    EditText editTextPasNew;
    EditText editTextPasOld;



    private boolean resultado = false;
    //private String server_especifico = "http://192.168.45.7:80/PBIOMED_SERVIDOR/src/rest/user/data";
    private String server_especifico = "http://192.168.1.140:80/PBIOMED_SERVIDOR/src/rest/user/data"; //CASA MAYRO
    //PARA QUE FUNCIONE CANCELAR
    String nombreEditPre = "";
    String nicknameEditPre = "";
    String emailEditPre = "";
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        botonguardar = findViewById(R.id.editarBotonGuardar);
        botonEditarBoton = findViewById(R.id.editarBotonEditar);
        textViewNombreEdit = findViewById(R.id.editTextNombre);
        textViewApellidoEdit = findViewById(R.id.editTextApellido);
        textViewNicknameEdit = findViewById(R.id.editTextNickname);
        textViewEmailEdit = findViewById(R.id.editTextEmailAddress);
        botonCancelarBoton = findViewById(R.id.editButtonCancelar);
        botonPass = findViewById(R.id.cambiarPas);
        //DESACTIVANDO EDITAR TEXTO
        textViewNombreEdit.setEnabled(false);
        textViewEmailEdit.setEnabled(false);
        textViewApellidoEdit.setEnabled(false);
        textViewNicknameEdit.setEnabled(false);
        //String data = null;
        //esto seguro que funciona...
        //una funcion statica que solo se llama cuando se va desde el login?
        String userS = LoginActivity.getUser();
        //HOLA MARIO CLIMENT!!
        // se abre la pagina y tengo mi nombre,apellidos,email y nickname disponibles para que los pueda mirar
        //

        JSONObject objeto = new JSONObject();
        try {
            objeto.put("nickname", userS);
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
                        String emailDatabase = "";
                        String[] nombreApellidosDatabase;
                        String nombreDatabase = "";
                        String apellidoDatabase = "";
                        String nicknameDatabase = "";

                        try {
                            String editedString = jsonString;
                            editedString = editedString.substring(1, editedString.length() - 1);

                            // Convierte la cadena JSON en un objeto JSON
                            JSONObject jsonObject = new JSONObject(editedString);

                            // Extrae datos específicos del objeto JSON y los imprime en
                            // los textviews

                            //Extraer email
                            emailDatabase = jsonObject.getString("email");
                            Log.d("email es ", "" + emailDatabase);
                            textViewEmailEdit.setText(emailDatabase);

                            //Extraer nickname
                            nicknameDatabase = jsonObject.getString("nickname");
                            Log.d("nickname es ", "" + nicknameDatabase);
                            textViewNicknameEdit.setText(nicknameDatabase);

                            //Extraer nombre y apellidos
                            nombreApellidosDatabase =separarNombreApellidos(jsonObject.getString("nombreApellidos"));

                            //Extraer nombre
                            nombreDatabase = nombreApellidosDatabase[0];
                            Log.d("nombre es ", "" + nombreDatabase);
                            textViewNombreEdit.setText(nombreDatabase);

                            //Extraer apellido
                            apellidoDatabase = nombreApellidosDatabase[1];
                            Log.d("apellido es ", "" + apellidoDatabase);
                            textViewApellidoEdit.setText(apellidoDatabase);




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
        botonCancelarBoton.setVisibility(View.VISIBLE);
        //FUERA EDITAR!
        botonEditarBoton.setVisibility(View.GONE);
        botonPass.setVisibility(View.GONE);
        //ACTIVANDO EDITAR TEXTO
        textViewNombreEdit.setEnabled(true);
        textViewEmailEdit.setEnabled(true);
        textViewApellidoEdit.setEnabled(true);
        textViewNicknameEdit.setEnabled(true);
    }

    public void guardarEditar(View view) {
        //UHHHH

        //BOTONES FUERA!
        botonguardar.setVisibility(View.GONE);
        botonCancelarBoton.setVisibility(View.GONE);
        //EDITAR DENTRO
        botonEditarBoton.setVisibility(View.VISIBLE);
        botonEditarBoton.setVisibility(View.VISIBLE);
        //DESACTIVANDO EDITAR TEXTO
        textViewNombreEdit.setEnabled(false);
        textViewEmailEdit.setEnabled(false);
        textViewApellidoEdit.setEnabled(false);
        textViewNicknameEdit.setEnabled(false);


        String userS = textViewNicknameEdit.getText().toString();
        String nombreApellidoS = textViewNombreEdit.getText().toString()+textViewApellidoEdit.getText().toString();
        String emailS = textViewEmailEdit.getText().toString();
        String oldUserS = LoginActivity.getUser();

       // if(editTextPasOld.getText().toString() != password)
        JSONObject objeto = new JSONObject();
        try {
            objeto.put("email", emailS);
            objeto.put("nombreApellidos", nombreApellidoS);
            objeto.put("nickname", userS);
            objeto.put("oldNickname", oldUserS);
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
        botonCancelarBoton.setVisibility(View.GONE);
        //EDITAR DENTRO
        botonEditarBoton.setVisibility(View.VISIBLE);
        botonPass.setVisibility(View.VISIBLE);
        //DESACTIVANDO EDITAR TEXTO
        textViewNombreEdit.setEnabled(false);
        textViewEmailEdit.setEnabled(false);
        textViewApellidoEdit.setEnabled(false);
        textViewNicknameEdit.setEnabled(false);
    }
    public void cambiarPass(View view){
        Intent intent = new Intent(this, EditPassActivity.class);
        startActivity(intent);
    }
    // Función para separar el nombre y apellidos
    public static String[] separarNombreApellidos(String nombreCompleto) {
        String[] resultado = new String[2];

        // Encontramos la posición donde comienza el apellido
        int posicionApellido = encontrarPosicionApellido(nombreCompleto);

        // Extraemos el nombre y el apellido
        resultado[0] = nombreCompleto.substring(0, posicionApellido);
        resultado[1] = nombreCompleto.substring(posicionApellido);

        return resultado;
    }

    // Función para encontrar la posición donde comienza el apellido
    public static int encontrarPosicionApellido(String nombreCompleto) {
        // Encontramos la posición donde comienza el apellido (primera letra mayúscula después de la primera letra minúscula)
        int longitudNombre = nombreCompleto.length();
        int posicionApellido = longitudNombre;

        for (int i = 1; i < longitudNombre; i++) {
            if (Character.isUpperCase(nombreCompleto.charAt(i)) && Character.isLowerCase(nombreCompleto.charAt(i - 1))) {
                posicionApellido = i;
                break;
            }
        }

        return posicionApellido;
    }

}

