package com.example.bluetooth_hc05.layouts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bluetooth_hc05.MainActivity;
import com.example.bluetooth_hc05.R;

public class HelpPage extends AppCompatActivity {
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);

        backButton = findViewById(R.id.helpPage_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivityConnect();

            }
        });
    }

    private void openActivityConnect(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}