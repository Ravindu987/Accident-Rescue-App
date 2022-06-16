package com.example.bluetooth_hc05;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

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


    // methods =======================================================================

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_connect:
                        openActivity(MainActivity.class);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_data:
                        openActivity(DataViewingWindow.class);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_relatives:
                        openActivity(RelativesWindow.class);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_help:
                        openActivity(HelpPage.class);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    default:
                        //do nothing
                        break;
                }
                return false;
            }
        });


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


    private void openActivity(Class className){
        Intent intent = new Intent(this, className);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
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


    /*
    MESSAGE FORMAT
    -------
    message
    -------
    D,HHMMSS.SSS,TTTT.TTTT,GGGGG.GGGG
    -------
    content
    -------
    D - Accident Detection Flag
    HHMMSS.SSS - Time
    T - Latitude / G - Longitude
    -------
    example
    -------
    0,104534.000,7791.0381,06727.4434
    */

    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("HandlerLeak")
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

                    //=====================================================
                    String dataString = (String) msg.obj;
                    String dataArray[] = dataString.split(",");

                    System.out.println(dataString);
                    System.out.println(Arrays.toString(dataArray));

//                    storeData(dataArray);    // to be implemented to store data in a database.

                    String strTime = dataArray[1].substring(0,2)+"."+dataArray[1].substring(2,4);

                    String version = "2.0";

//                    if (dataArray[0].equals("1")){
//                        SmsManager smsManagerSend = SmsManager.getDefault();
//                        String strMessage = version+"ACCIDENT DETECTED\n@"+strTime+"\n"+"view the location on google maps:\nhttps://www.google.com/maps/search/?api=1&query="+dataArray[2]+","+dataArray[3];
//                        System.out.println(strMessage);         // testing
//                        for (String contact: relativeContacts) {
//                            smsManagerSend.sendTextMessage(contact, null, strMessage, null, null);
//                        }
//                    }

                    //=====================================================


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

            byte[] buffer = new byte[1024];
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