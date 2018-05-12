package com.example.udhay.contactviewer.BackgroundTasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.udhay.contactviewer.contact_database.ContactsContract;

import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;

public class ContactAsyncTask extends android.support.v4.content.AsyncTaskLoader<Cursor>{
   private final Context context;

    public ContactAsyncTask(Context context ) {
        super(context);
        this.context = context;


    }

    @Override
    public Cursor loadInBackground() {

        ContactOpenHelper openHelper = new ContactOpenHelper(context);

        SQLiteDatabase database = openHelper.getReadableDatabase();

        return database.query(ContactsContract.Contacts.TABLE_NAME ,
                //Query both the contact name and number
                new String[]{ContactsContract.Contacts.COLUMN_NAME , ContactsContract.Contacts.DEFAULT_NUMBER} ,
                null ,
                null ,
                null ,
                null,
                //Query the contact name is asending order for display
                ContactsContract.Contacts.COLUMN_NAME + " ASC "
                );

    }
}

