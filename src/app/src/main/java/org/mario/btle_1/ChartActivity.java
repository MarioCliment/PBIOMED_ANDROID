package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ChartActivity extends AppCompatActivity {
    ProgressBar valorDonut;
    EditText valorPrueba;
    TextView textView;
    String resultado = "";
    private String server = "http://192.168.1.133:80/pruebaRESTful/rest/";

    private String server_especifico = server + "hacerLogin.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Initialize the ProgressBar and EditText within thwe onCreate method
        valorDonut = findViewById(R.id.progress_bar);
        valorPrueba = findViewById(R.id.valorPrueba);
        textView = findViewById(R.id.text_view_progress);
    }

    public void cambiarValor(View view) {
        valorDonut.setProgress(Integer.parseInt(valorPrueba.getText().toString()));
        textView.setText(valorPrueba.getText().toString());

    }
    }
