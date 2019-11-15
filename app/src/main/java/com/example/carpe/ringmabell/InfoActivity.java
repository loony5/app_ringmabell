package com.example.carpe.ringmabell;

import android.content.Intent;
import android.icu.text.IDNA;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    private Button btn_register, btn_login;
    private TextView txt_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        btn_register = findViewById(R.id.btn_register);
        btn_login = findViewById(R.id.btn_login);
        txt_main = findViewById(R.id.txt_main);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(InfoActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(InfoActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });

        txt_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
