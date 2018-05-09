package com.example.udhay.contactviewer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
private final Uri  contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
private Cursor contactCursor;
private static final int LOADER_ID = 100;
private RecyclerView contactRecyclerView;
private ContactAdapter contactAdapter;

// Request code for READ_CONTACTS. It can be any number > 0.
private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactRecyclerView = findViewById(R.id.contact_recycle);
        contactRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            PackageManager manager = getPackageManager();
            int hasPermission = manager.checkPermission("android.permission.READ_CONTACTS", "com.example.udhay.contactviewer");
            if (hasPermission == manager.PERMISSION_GRANTED) {
                loadContact();
            }
        }  else {
                loadContact();
            }



    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.v("loader Creation" , "Loader is Created");
        return new ContactAsyncTask(this , contactUri);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

Log.v("loader finished" , "inside loader finished");
        contactCursor = data;

        contactAdapter = new ContactAdapter(data);
        contactRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contactRecyclerView.setAdapter(contactAdapter);

        Log.v("display" , Integer.toString(contactCursor.getCount()));
        refresh();


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        getSupportLoaderManager().restartLoader(LOADER_ID , null , this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    //this method is used to check the country code
    private String prepareNumber(String number){
        if(number.charAt(0) == '+'){
            return number;
        }
        else{
            return ("+91" + number);
        }
    }
    private void loadContact(){
        LoaderManager manager = getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(LOADER_ID);
        if (loader == null) {
            manager.initLoader(LOADER_ID, null, this).forceLoad();
        } else {
            manager.restartLoader(LOADER_ID, null, this).forceLoad();
        }


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String number = prepareNumber(((ContactAdapter.contactViewHolder) viewHolder).getContactNumber().trim());
                if(direction == ItemTouchHelper.LEFT) {
                    contactAdapter.notifyDataSetChanged();
                    Intent dial = new Intent(Intent.ACTION_DIAL);
                    dial.setData(Uri.parse("tel:" + number));
                    startActivity(dial);
                }
                if(direction == ItemTouchHelper.RIGHT){
                    contactAdapter.notifyDataSetChanged();
                    Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW);
                    whatsAppIntent.setType("text/plain");
                    whatsAppIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone="+number));
                    whatsAppIntent.setPackage("com.whatsapp");
                    try {
                        startActivity(whatsAppIntent);
                    }catch(Exception ex){
                        Toast.makeText(MainActivity.this , "Whats app is not installed" , Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }).attachToRecyclerView(contactRecyclerView);

        Toast.makeText(this, "swipe left to call ", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.detailWhatsApp:
                startActivity(new Intent(this , whatsAppDirect.class ));
                return true;
            default:
                return false;
        }

    }

    public void refresh(){
        if(contactCursor != null){
            SQLiteDatabase customDatabase =  new ContactOpenHelper(this).getWritableDatabase();
            ContentValues values = new ContentValues();
            contactCursor.moveToFirst();
            do{
                values.put(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.COLUMN_NAME ,
                        contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                try {
                    customDatabase.insert(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.TABLE_NAME, null, values);
                }catch (SQLiteConstraintException ex){

                }
                values.clear();
            }while(contactCursor.moveToNext());


            Cursor modifiedContactCursor = customDatabase.query(true , com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.TABLE_NAME ,
                    null , null , null , null , null ,
                    null  , null);

            contactAdapter.swapCursor(modifiedContactCursor);
            contactAdapter.notifyDataSetChanged();
            customDatabase.close();
        }
    }
}
