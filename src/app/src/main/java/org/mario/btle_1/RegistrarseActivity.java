package org.mario.btle_1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class RegistrarseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);
        getSupportActionBar().hide();
    }
}