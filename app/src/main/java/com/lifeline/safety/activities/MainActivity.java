package com.lifeline.safety.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;

import com.lifeline.safety.R;


public class MainActivity extends AppCompatActivity {
    TextView titleText;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titleText = findViewById(R.id.titleText);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("username", "Asif");
            startActivity(intent);
            finish();
        });
    }
}