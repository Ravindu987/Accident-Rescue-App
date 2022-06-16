package com.example.bluetooth_hc05;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper implements Serializable {
    public static final String COLUMN_CONTACT_ID = "ID";
    public static final String COLUMN_CONTACT_NAME = "NAME";
    public static final String COLUMN_CONTACT = "CONTACT";
    public static final String EMERGENCY_CONTACTS = "EMERGENCY_CONTACTS";
    public static final String LOCATION_TABLE = "LOCATION_TABLE";
    public static final String COLUMN_TIME = "TIME";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    public static final String COLUMN_VELOCITY = "VELOCITY";
    public static final String AUTHORITY_TABLE = "AUTHORITY_TABLE";
    public static final String COLUMN_A_ID = "A_ID";
    public static final String COLUMN_A_NAME = "A_NAME";
    public static final String COLUMN_A_LONGITUDE = "A_LONGITUDE";
    public static final String COLUMN_A_LATITUDE = "A_LATITUDE";
    public static final String COLUMN_A_CONTACT = "A_CONTACT";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "accident_rescue.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createContactTable = "CREATE TABLE " + EMERGENCY_CONTACTS + " ( " + COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_CONTACT_NAME + " VARCHAR(20) NOT NULL, " + COLUMN_CONTACT + " CHAR(12) NOT NULL)";
        String createLocationTable = "CREATE TABLE " + LOCATION_TABLE + " ( " + COLUMN_TIME + " VARCHAR(10) PRIMARY KEY NOT NULL, " + COLUMN_LATITUDE + " VARCHAR(10) NOT NULL, " + COLUMN_LONGITUDE + " VARCHAR(10), " + COLUMN_VELOCITY + " VARCHAR(10))";
        String createAuthorityTable = "CREATE TABLE " + AUTHORITY_TABLE + " ( " + COLUMN_A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_A_NAME + " VARCHAR(15) NOT NULL, " + COLUMN_A_LATITUDE + " VARCHAR(10) NOT NULL, " + COLUMN_A_LONGITUDE + " VARCHAR(10) NOT NULL, " + COLUMN_A_CONTACT + " VARCHAR(12) NOT NULL)";
        sqLiteDatabase.execSQL(createContactTable);
        sqLiteDatabase.execSQL(createLocationTable);
        sqLiteDatabase.execSQL(createAuthorityTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean record_contact(ContactModel contactModel){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CONTACT_NAME, contactModel.getName());
        cv.put(COLUMN_CONTACT , contactModel.getNumber());

        long insert = db.insert(EMERGENCY_CONTACTS, null, cv);
        if(insert==-1){
            return false;
        }
        else{
            return true;
        }
    }

    public List<String> getAllContacts(){

        List<String> allContacts = new ArrayList<>();

        String getContactQuery = "SELECT " + COLUMN_CONTACT + " FROM " + EMERGENCY_CONTACTS;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(getContactQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String contact = cursor.getString(0);
                allContacts.add(contact);
            } while (cursor.moveToNext());
        } else {
        }

        cursor.close();
        //db.close();
        return allContacts;
    }

    public boolean recordLocation(LocationModel locationModel){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TIME, locationModel.getTime());
        cv.put(COLUMN_LATITUDE, locationModel.getLatitude());
        cv.put(COLUMN_LONGITUDE, locationModel.getLongitude());
        cv.put(COLUMN_VELOCITY, locationModel.getVelocity());

        long insert = db.insert(LOCATION_TABLE, null, cv);
        if(insert==-1){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean recordAuthority(AuthorityModel authorityModel){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_A_NAME, authorityModel.getName());
        cv.put(COLUMN_A_LATITUDE, authorityModel.getLat());
        cv.put(COLUMN_A_LONGITUDE, authorityModel.getLang());
        cv.put(COLUMN_A_CONTACT, authorityModel.getContact());

        long insert = db.insert(AUTHORITY_TABLE, null, cv);
        if(insert==-1){
            return false;
        }
        else{
            return true;
        }
    }

    public List<AuthorityModel> getAllAuthorities(){

        List<AuthorityModel> allAuthorities = new ArrayList<>();

        String getAuthorityQuery = "SELECT * FROM " + AUTHORITY_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(getAuthorityQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int ID = cursor.getInt(0);
                String name = cursor.getString(1);
                String lat = cursor.getString(2);
                String lang = cursor.getString(3);
                String contact = cursor.getString(4);
                AuthorityModel tmp_authority = new AuthorityModel(ID, name, lat, lang, contact);
                allAuthorities.add(tmp_authority);
            } while (cursor.moveToNext());
        } else {
        }

        cursor.close();
        //db.close();
        return allAuthorities;
    }
}
