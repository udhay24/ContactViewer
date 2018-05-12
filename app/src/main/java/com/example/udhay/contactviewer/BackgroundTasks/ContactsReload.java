package com.example.udhay.contactviewer.BackgroundTasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.udhay.contactviewer.MainActivity;
import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;
import com.example.udhay.contactviewer.contact_database.ContactsContract;

//This Async Task prepares the custom database

public class ContactsReload extends AsyncTask<Void , Integer , Cursor> {

    //This uri is used to query the android database
    public static final Uri contactUri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;



    Context mContext;
    //Public constructor takes in a context
    public ContactsReload(Context context){
        mContext = context;
    }


    @Override
    protected Cursor doInBackground(Void... voids) {
        //This cursor is obtained from the android content providers
//        It contains all details of the contact

        Cursor contactCursor = mContext.getContentResolver().query(contactUri , null , null , null , android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

        ContentValues contactValue = new ContentValues();

        ContactOpenHelper openHelper = new ContactOpenHelper(mContext);

        SQLiteDatabase database = openHelper.getWritableDatabase();

        for(int i = 0 ; i <contactCursor.getCount() ; i++){

            contactCursor.moveToPosition(i);
//Get the name of the contact
            String name = contactCursor.getString(contactCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//           Get the number of the same contact
//            This number will be used as the Default number in the Default Conlumn of the database

            String number = contactCursor.getString(contactCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER));


            contactValue.put(ContactsContract.Contacts.COLUMN_NAME, name);

            database.insert(ContactsContract.Contacts.TABLE_NAME, null, contactValue);

            contactValue.clear();

//            TODO(1) fix the first run of the app . Because it is freaking Weird


            if(MainActivity.launch<=2) {
                contactValue.put(ContactsContract.Contacts.DEFAULT_NUMBER, number);
                //Update the default Number of the contact
                database.update(ContactsContract.Contacts.TABLE_NAME, contactValue, ContactsContract.Contacts.COLUMN_NAME + " =? ",
                        new String[]{name});
                contactValue.clear();
            }


        }
        contactCursor.close();

//TODO(2) Try closing the database connection . it messed up last time

        database = openHelper.getReadableDatabase();
        //Querying the custom database for all the contact names

        Cursor cursor = database.query(ContactsContract.Contacts.TABLE_NAME ,
                new String[]{ContactsContract.Contacts.COLUMN_NAME , ContactsContract.Contacts.DEFAULT_NUMBER} ,
                null , null ,null , null ,null);

        //This cursor contains all the contact name's and numbers
        return cursor;
    }


    @Override
    protected void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);
        MainActivity.contactAdapter.swapCursor(cursor);
        MainActivity.contactAdapter.notifyDataSetChanged();



        //Remove this Toast after debugging
        Toast.makeText(mContext , "Contacts present" + cursor.getCount() , Toast.LENGTH_SHORT).show();
    }


}
