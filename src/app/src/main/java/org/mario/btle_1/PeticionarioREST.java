package org.mario.btle_1;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

// ------------------------------------------------------------------------
// ------------------------------------------------------------------------
public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    public interface RespuestaREST {
        void callback (int codigo, String cuerpo);
    }

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    public void hacerPeticionREST (String metodo, String urlDestino, String cuerpo, RespuestaREST  laRespuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = laRespuesta;

        this.execute(); // otro thread ejecutará doInBackground()
    }

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    public PeticionarioREST() {
        Log.d("clienterestandroid", "constructor()");
    }

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {
            // envio la peticion
            if (this.elMetodo.equals("GET") &&  this.elCuerpo != null){
                Log.d("clienterestandroid", "Es GET y con datos");
                urlDestino = urlDestino +"?";
                try {
                    Log.d("clienterestandroid", "Convierto el String a JSONObject");
                    JSONObject objeto = new JSONObject(this.elCuerpo);

                    // Obtener las claves del objeto JSON
                    Iterator<String> keys = objeto.keys();
                    Log.d("clienterestandroid", "Empiezo a Iterar con los valores");
                    // Iterar sobre las claves usando un bucle foreach
                    while (keys.hasNext()) {
                        //Obtenemos los valores y los guardamos
                        String clave = keys.next();
                        String valor = objeto.getString(clave);
                        Log.d("clienterestandroid", ("Clave:" + clave + ", Valor: " + valor));

                        // Añadir los valores a una URL
                        urlDestino = urlDestino + (clave + "=" + URLEncoder.encode(valor, "UTF-8") + "&");
                    }

                    // Lógica para eliminar el último "&"
                    urlDestino = urlDestino.substring(0, urlDestino.length() - 1);


                } catch (JSONException e) {
                    Log.d("clienterestandroid", "ERROR: "+e);
                    e.printStackTrace();
                }
            }
            else if (this.elMetodo.equals("GET")){
                Log.d("clienterestandroid", "El GET no tiene nada");
            }


            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            URL url = new URL(urlDestino);

            url.equals(urlDestino);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "Content-Type", "application/json; charset-utf-8" );
            connection.setRequestMethod(this.elMetodo);
            connection.setDoInput(true);

            if ( ! this.elMetodo.equals("GET") && this.elCuerpo != null ) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                connection.setDoOutput(true);
                // si no es GET, pongo el cuerpo que me den en la peticion
                DataOutputStream dos = new DataOutputStream (connection.getOutputStream());
                Log.d("clienterestandroid", "doInBackground(): cuerpo" + this.elCuerpo);
                dos.writeBytes(this.elCuerpo);
                dos.flush();
                dos.close();
            }

            // Ya he enviado la peticion
            Log.d("clienterestandroid", "doInBackground(): peticion enviada ");

            // Ahora obtengo la respuesta

            int rc = connection.getResponseCode();
            String rm = connection.getResponseMessage();
            String respuesta = "" + rc + " : " + rm;
            Log.d("clienterestandroid", "doInBackground() recibo respuesta = " + respuesta);
            this.codigoRespuesta = rc;

            try {

                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                Log.d("clienterestandroid", "leyendo cuerpo");
                StringBuilder acumulador = new StringBuilder ();
                String linea;
                while ( (linea = br.readLine()) != null) {
                    Log.d("clienterestandroid", linea);
                    acumulador.append(linea);
                }
                Log.d("clienterestandroid", "FIN leyendo cuerpo");

                this.cuerpoRespuesta = acumulador.toString();
                Log.d("clienterestandroid", "cuerpo recibido=" + this.cuerpoRespuesta);

                connection.disconnect();

            } catch (IOException ex) {
                // dispara excepcion cuando la respuesta REST no tiene cuerpo y yo intento getInputStream()
                Log.d("clienterestandroid", "doInBackground() : parece que no hay cuerpo en la respuesta");
            }

            return true; // doInBackground() termina bien

        } catch (Exception ex) {
            Log.d("clienterestandroid", "doInBackground(): ocurrio alguna otra excepcion: " + ex.getMessage());
        }

        return false; // doInBackground() NO termina bien
    } // ()

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    protected void onPostExecute(Boolean comoFue) {
        // llamado tras doInBackground()
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        try {
            // ¡Seguro que esto lo soluciona!
            // Gabriel Lituania Dóbilas
            // 3 de ataque | 5 de vida
            // Grito de batalla: PLAP PLAP
            // "¡Amigo del alma, hoy caerás en combate!"
            this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


} // class

