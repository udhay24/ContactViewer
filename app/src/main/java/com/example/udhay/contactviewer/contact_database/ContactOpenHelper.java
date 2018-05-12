package com.example.udhay.contactviewer.contact_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contact.db";
    private static int DATABASE_VERSION = 4;

    // SqlLite statement to create a database
    private static final String CREATE_TABLE = "CREATE TABLE  " +
            ContactsContract.Contacts.TABLE_NAME + " ( "+
            ContactsContract.Contacts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "+
            ContactsContract.Contacts.COLUMN_NAME + " TEXT  " + " UNIQUE ,"+
            ContactsContract.Contacts.DEFAULT_NUMBER + " TEXT  "
            + " ) ";

    //SqlLite statement to drop the table;
    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS " +ContactsContract.Contacts.TABLE_NAME;

    public ContactOpenHelper(Context context){
        super(context , DATABASE_NAME , null , DATABASE_VERSION );
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }
}
