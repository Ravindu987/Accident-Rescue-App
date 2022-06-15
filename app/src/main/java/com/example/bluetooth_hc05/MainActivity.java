package com.example.bluetooth_hc05;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btn_connect, btn_addContact, btn_getContact;
    TextView txt_data, txt_sts, txt_contacts;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice hc05;
    BluetoothSocket bluetoothSocket;

    SendReceive sendReceive;

    DatabaseHelper databaseHelper;

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    static final int STATE_CONNECTED = 1;
    static final int STATE_CONNECTION_FAILED = 2;
    static final int STATE_MESSAGE_RECEIVED = 3;
    static final int STATE_CONTACTS_RECEIVED = 4;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        databaseHelper = new DatabaseHelper(MainActivity.this);

        btn_connect = findViewById(R.id.btn_connect);
        txt_data = findViewById(R.id.txt_data);
        txt_sts = findViewById(R.id.txt_status);
        btn_addContact = findViewById(R.id.btn_add_contact);
        btn_getContact = findViewById(R.id.btn_getContact);
        txt_contacts = findViewById(R.id.txt_contacts);

        connect();
        add();
        get_contacts();
    }

    private void get_contacts() {
        btn_getContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> allContacts = databaseHelper.getAllContacts();

                String contacts = "Contacts: ";
                for( String contact : allContacts){
                    System.out.println(contact);
                    contacts=contacts.concat(contact);
                    contacts=contacts.concat(", ");
                }

                //Toast.makeText(MainActivity.this, contacts, Toast.LENGTH_SHORT).show();
                handler.obtainMessage(STATE_CONTACTS_RECEIVED, contacts.length(),-1, contacts).sendToTarget();

            }
        });
    }

    private void add() {
        btn_addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContactModel contactModel = new ContactModel(1, "Ravindu2", "+94710764814");

                boolean success = databaseHelper.record_contact(contactModel);

                Toast.makeText(MainActivity.this, ""+success, Toast.LENGTH_SHORT).show();

            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case STATE_CONNECTED:
                    txt_sts.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    txt_sts.setText("Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    txt_data.setText((CharSequence) msg.obj);
                    break;
                case STATE_CONTACTS_RECEIVED:
                    txt_contacts.setText((CharSequence) msg.obj);
                    break;
            }
            return false;
        }
    }){
    };


    @SuppressLint("MissingPermission")
    private void connect() {

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hc05 = bluetoothAdapter.getRemoteDevice("00:22:01:00:14:80");

                bluetoothSocket = null;

                try {
                    bluetoothSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bluetoothSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(bluetoothSocket.isConnected()==true){
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                }
                else{
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                InputStream inputStream = null;

                try {
                    inputStream = bluetoothSocket.getInputStream();
                    inputStream.skip(inputStream.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sendReceive = new SendReceive(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                sendReceive.start();

            }
        });

    }

    private class SendReceive extends Thread{
        private final InputStream inputStream;

        public SendReceive(InputStream inputStream) throws IOException {
            this.inputStream = inputStream;
        }

        public void run(){

            byte[] buffer = new byte[10];
            int bytes;

            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String tmp_string = new String(buffer,0,bytes);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes, -1, tmp_string).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}