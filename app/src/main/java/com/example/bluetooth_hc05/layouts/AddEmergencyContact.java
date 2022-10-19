package com.example.bluetooth_hc05.layouts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bluetooth_hc05.DatabaseHelper;
import com.example.bluetooth_hc05.R;
import com.example.bluetooth_hc05.models.ContactModel;

public class AddEmergencyContact extends AppCompatActivity {

    EditText etName, etPhone;
    Button btn_add, btn_back;
    DatabaseHelper databaseHelper;
    String name, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatives_window);

        etName = findViewById(R.id.editTextTextContactName);
        etPhone = findViewById(R.id.editTextContact);
        btn_add = findViewById(R.id.btn_addContact);
        btn_back = findViewById(R.id.btn_add_meregncy_back);

        databaseHelper = new DatabaseHelper(AddEmergencyContact.this);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
                    Toast.makeText(AddEmergencyContact.this, "Successfully added", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(AddEmergencyContact.this, "Invalid details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}