package com.example.bluetooth_hc05;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class RelativesWindow extends AppCompatActivity {

    EditText etName, etPhone;
    Button btn_add;
    DatabaseHelper databaseHelper;
    String name, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatives_window);

        etName = findViewById(R.id.editTextTextContactName);
        etPhone = findViewById(R.id.editTextContact);
        btn_add = findViewById(R.id.btn_addContact);

        databaseHelper = new DatabaseHelper(RelativesWindow.this);

        add();
    }

    private void add() {
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = etName.getText().toString();
                phone = etPhone.getText().toString();

                ContactModel contactModel = new ContactModel(1, name, phone);

                boolean success = databaseHelper.record_contact(contactModel);

                if(success){
                    etName.getText().clear();
                    etPhone.getText().clear();
                    Toast.makeText(RelativesWindow.this, "Successfully added", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(RelativesWindow.this, "Invalid details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}