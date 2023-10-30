package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        TextView textView = findViewById(R.id.irRegistrar);

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
        textView.setText(spannableString);
    }
}