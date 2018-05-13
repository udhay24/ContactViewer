package com.example.udhay.contactviewer.BackgroundTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.udhay.contactviewer.MainActivity;
import com.example.udhay.contactviewer.contact_database.ContactsContract;

import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;

public class ContactAsyncTask extends android.support.v4.content.AsyncTaskLoader<Cursor>{
   private final Context context;
   SharedPreferences settings;

    public ContactAsyncTask(Context context ) {
        super(context);
        this.context = context;
        settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);

    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something

            new ContactsReload(context , true).execute();


            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }

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

