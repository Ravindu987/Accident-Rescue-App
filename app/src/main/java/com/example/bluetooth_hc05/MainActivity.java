package com.example.bluetooth_hc05;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public static final int REQUEST_ENABLE_BIT=0;

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

        findViews();

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

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);

        Intent intent = new Intent(this, RelativesWindow.class);
        intent.putExtra("DatabaseHelper", databaseHelper);

        connect();
        get_contacts();
    }

    private void findViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btn_connect = findViewById(R.id.btn_connect);
        txt_data = findViewById(R.id.txt_data);
        txt_sts = findViewById(R.id.txt_status);
        btn_getContact = findViewById(R.id.btn_getContact);
        txt_contacts = findViewById(R.id.txt_contacts);

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

                Toast.makeText(MainActivity.this, contacts, Toast.LENGTH_SHORT).show();
                handler.obtainMessage(STATE_CONTACTS_RECEIVED, contacts.length(),-1, contacts).sendToTarget();
            }
        });
    }

    /*
    MESSAGE FORMAT
    -------
    message
    -------
    D,HHMMSS.SSS,TTTT.TTTT,GGGGG.GGGG, VVV.VV
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

//                    String version = "2.0";

                    if (dataArray[0].equals("1")){
                        SmsManager smsManagerSend = SmsManager.getDefault();
                        String strMessage = "ACCIDENT DETECTED\n@"+strTime+"\n"+"view the location on google maps:\nhttps://www.google.com/maps/search/?api=1&query="+dataArray[2]+","+dataArray[3];
                        System.out.println(strMessage);         // testing

                        List<String> allContacts = databaseHelper.getAllContacts();

                        for (String contact: allContacts) {
                            smsManagerSend.sendTextMessage(contact, null, strMessage, null, null);
                        }
                    }

                    //=====================================================

                    break;

                case STATE_CONTACTS_RECEIVED:
                    txt_contacts.setText((CharSequence) msg.obj);
            }
            return false;
        }
    }){
    };


    private void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BIT) {
            if (resultCode == RESULT_OK) {
                makeToast("Bluetooth enabled");
            } else {
                makeToast("Couldn't turn on Bluetooth");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingPermission")
    private void connect() {

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!bluetoothAdapter.isEnabled()) {
                    makeToast("Enabling bluetooth");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BIT);
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    makeToast("Bluetooth already on");
                }

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