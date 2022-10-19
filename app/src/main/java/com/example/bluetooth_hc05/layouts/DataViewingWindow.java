package com.example.bluetooth_hc05.layouts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.bluetooth_hc05.DatabaseHelper;
import com.example.bluetooth_hc05.R;
import com.example.bluetooth_hc05.models.ContactModel;

import org.w3c.dom.Text;

import java.util.List;

public class DataViewingWindow extends AppCompatActivity {
    Button backButton;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewing_window);

        databaseHelper = new DatabaseHelper(DataViewingWindow.this);

        List<ContactModel> allContacts = databaseHelper.getAllContactModels();
        String contacts = "Contacts: ";

        TableLayout tableLayout = findViewById(R.id.dataview_layout);

        int i=0;
        for( ContactModel contact : allContacts){
            System.out.println(contact);
            TableRow row = new TableRow(this);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT );
            params.setMargins(0, 20, 0, 20);
            row.setLayoutParams(params);


            for(int j=0; j<2; j++){
                TextView tv = new TextView(this);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                if(j==0){
                    tv.setText(contact.getName());
                }else{
                    tv.setText(contact.getNumber());
                }
                tv.setGravity(Gravity.CENTER);
                row.addView(tv);
            }

            tableLayout.addView(row);

        }

        backButton = findViewById(R.id.btn_dataview_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}