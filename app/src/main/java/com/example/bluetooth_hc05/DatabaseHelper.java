package com.example.bluetooth_hc05;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String COLUMN_CONTACT_ID = "ID";
    public static final String COLUMN_CONTACT_NAME = "NAME";
    public static final String COLUMN_CONTACT = "CONTACT";
    public static final String EMERGENCY_CONTACTS = "EMERGENCY_CONTACTS";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "accident_rescue.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createContactTable = "CREATE TABLE " + EMERGENCY_CONTACTS + " ( " + COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_CONTACT_NAME + " VARCHAR(20) NOT NULL, " + COLUMN_CONTACT + " CHAR(12) NOT NULL)";
        sqLiteDatabase.execSQL(createContactTable);
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
}
