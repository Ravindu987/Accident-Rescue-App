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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth_hc05.layouts.DataViewingWindow;
import com.example.bluetooth_hc05.layouts.HelpPage;
import com.example.bluetooth_hc05.layouts.AddEmergencyContact;
import com.example.bluetooth_hc05.models.AuthorityModel;
import com.example.bluetooth_hc05.models.LocationModel;
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

    SmsManager smsManagerSend;

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
                        openActivity(AddEmergencyContact.class);
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

        smsManagerSend = SmsManager.getDefault();

        databaseHelper = new DatabaseHelper(MainActivity.this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);

        AuthorityModel colombo = new AuthorityModel(1, "Colombo", "6.868271", "79.9277724", "+94712526577");
        AuthorityModel kandy = new AuthorityModel(1, "Kandy", "7.288041", "80.632553", "+94770079044");
        databaseHelper.recordAuthority(colombo);
        databaseHelper.recordAuthority(kandy);

        Intent intent = new Intent(this, AddEmergencyContact.class);
        intent.putExtra("DatabaseHelper", databaseHelper);

        System.out.println("Working");
        connect();
        //get_contacts();
    }

    private void findViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btn_connect = findViewById(R.id.btn_connect);
        //txt_data = findViewById(R.id.txt_data);
        txt_sts = findViewById(R.id.txt_status);
        //btn_getContact = findViewById(R.id.btn_getContact);
        //txt_contacts = findViewById(R.id.txt_contacts);

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

                    //=====================================================
                    String dataString = (String) msg.obj;
                    String dataArray[] = dataString.split(",");

                    String latitude = "";
                    for(int i=0; i < dataArray[2].length(); i++){
                        if(i==4){
                            continue;
                        }
                        latitude = latitude.concat(String.valueOf(dataArray[2].charAt(i)));
                        if(i==1){
                            latitude = latitude.concat(".");
                        }
                    }

                    String longitude = "";
                    for(int i=0; i < dataArray[3].length(); i++){
                        if(i==5){
                            continue;
                        }
                        longitude = longitude.concat(String.valueOf(dataArray[3].charAt(i)));
                        if(i==2){
                            longitude = longitude.concat(".");
                        }
                    }

                    dataArray[2] = latitude;
                    dataArray[3] = longitude;

                    System.out.println(dataString);
                    System.out.println(Arrays.toString(dataArray));

                    String hours = dataArray[1].substring(0,2);
                    String minutes = String.valueOf((Integer.parseInt(dataArray[1].substring(2,4))+30)%60);
                    if(Integer.parseInt(minutes) < 30){
                        hours = String.valueOf(Integer.parseInt(hours)+1);
                    }
                    hours = String.valueOf((Integer.parseInt(hours)+5)%24);

                    String strTime = hours+"."+minutes;

                    Float ftTime = Float.valueOf(strTime);


                    LocationModel locationModel = new LocationModel(strTime, dataArray[2], dataArray[3], dataArray[4]);
                    try{
                        databaseHelper.recordLocation(locationModel);
                    } catch (SQLiteConstraintException e){
                        e.printStackTrace();
                    }


                    if (dataArray[0].equals("1")){
                        String strMessage = "ACCIDENT DETECTED\n@"+strTime+"\n"+"view the location on google maps:\nhttps://www.google.com/maps/search/?api=1&query="+dataArray[2]+","+dataArray[3];
                        System.out.println(strMessage);         // testing

                        List<AuthorityModel> allAuthorities = databaseHelper.getAllAuthorities();
                        Location curLocation = new Location("");
                        curLocation.setLatitude(Double.parseDouble(dataArray[2]));
                        curLocation.setLongitude(Double.parseDouble(dataArray[3]));

                        AuthorityModel nearestAuthority = getNearestLocation(allAuthorities, curLocation);
                        System.out.println(nearestAuthority.getName());
                        smsManagerSend.sendTextMessage(nearestAuthority.getContact(), null, strMessage, null, null);

                        List<String> allContacts = databaseHelper.getAllContacts();

                        for (String contact: allContacts) {
                            System.out.println(contact);
                            //smsManagerSend.sendTextMessage(contact, null, strMessage, null, null);
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

    private AuthorityModel getNearestLocation( List<AuthorityModel> locations, Location curLocation){

        AuthorityModel nearestLocation = null;
        float distance = 999999.0F;
        
        for( AuthorityModel location : locations){
            double lat = Double.parseDouble(location.getLat());
            double lang = Double.parseDouble(location.getLang());
            Location authority_location = new Location("");
            authority_location.setLatitude(lat);
            authority_location.setLongitude(lang);

            if( authority_location.distanceTo(curLocation) < distance){
                distance = authority_location.distanceTo(curLocation);
                System.out.println("Distance: ######" + distance);
                nearestLocation = location;
            }
        }

        return nearestLocation;
    }

    @SuppressLint("MissingPermission")
    private void connect() {

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Here");
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
                    System.out.println("Socket Created");
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
                    System.out.println("Socket connected");
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