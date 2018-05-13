package com.example.udhay.contactviewer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.View;
import android.widget.Toast;

import com.example.udhay.contactviewer.Activities.DetailContactActivity;
import com.example.udhay.contactviewer.Activities.whatsAppDirect;
import com.example.udhay.contactviewer.BackgroundTasks.ContactAsyncTask;
import com.example.udhay.contactviewer.BackgroundTasks.ContactsReload;
import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;
import com.example.udhay.contactviewer.contact_database.ContactsContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static Cursor contactCursor;
    private static final int LOADER_ID = 100;
    public static RecyclerView contactRecyclerView;
    public static ContactAdapter contactAdapter;

//    TODO(4) change this to a boolean flag

    public static int launch = 0;

    //This Statement is used to check for first run
    public static final String PREFS_NAME = "MyPrefsFile";



    // Request code for READ_CONTACTS.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactRecyclerView = findViewById(R.id.contact_recycle);
        contactRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        contactRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

// This one checks for the permission and loads the data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }else {

            //since the permission is granted load the contacts
            loadContact();
        }








    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSIONS_REQUEST_READ_CONTACTS:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //Since the permission is granted load all the contacts
                    loadContact();
                }
                else {
                    Toast.makeText(this , "please provide the permission" , Toast.LENGTH_SHORT);
                }
            }
        }
    }

    /**
* @return The cursor contains the query from the custom database with the names in the ascending order
* */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new ContactAsyncTask(this);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        contactCursor = data;

        contactAdapter = new ContactAdapter(data);

        contactRecyclerView.setAdapter(contactAdapter);

        //WhHY THE HELL DO I NEED TO CALL REFRESH!!
        refresh();

        // Set the itemOpenHelper to the recycler View
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String number = prepareNumber(((ContactAdapter.contactViewHolder) viewHolder).getContactNumber().trim());
                if (direction == ItemTouchHelper.LEFT) {
                    contactAdapter.notifyDataSetChanged();
                    Intent dial = new Intent(Intent.ACTION_DIAL);
                    dial.setData(Uri.parse("tel:" + number));
                    startActivity(dial);
                }
                if (direction == ItemTouchHelper.RIGHT) {
                    contactAdapter.notifyDataSetChanged();
                    Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW);
                    whatsAppIntent.setType("text/plain");
                    whatsAppIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + number));
                    whatsAppIntent.setPackage("com.whatsapp");
                    try {
                        startActivity(whatsAppIntent);
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.this, "Whats app is not installed", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }).attachToRecyclerView(contactRecyclerView);


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }



    //this method is used to check the country code
    private String prepareNumber(String number) {
        if (number.charAt(0) == '+') {
            return number;
        } else {
            return ("+91" + number);
        }
    }

    //This method calls the loaderManager function
    private void loadContact() {
        LoaderManager manager = getSupportLoaderManager();
        Loader<Cursor> loader = manager.getLoader(LOADER_ID);
        if (loader == null) {
            manager.initLoader(LOADER_ID, null, this).forceLoad();
        } else {
            manager.restartLoader(LOADER_ID, null, this).forceLoad();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.detailWhatsApp:
                startActivity(new Intent(this, whatsAppDirect.class));
                return true;
            case R.id.refresh:
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this).forceLoad();
                return true;
            default:
                return false;
        }

    }


    //This method will prepare the custom databasse.
    // This method should be called during the first run  and can be called by the user to update the database


    public void refresh() {

        ContactsReload refresh = new ContactsReload(this);
        //The input cursor is the one obtained by querying the android Database .
//        It contains all name and numbers of all the records

        refresh.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        launch++;
    }
}

//This listener will help to open the appropriate detail activity

    class ContactClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = MainActivity.contactRecyclerView.getChildAdapterPosition(v);
            position += 1;
            ContactOpenHelper openHelper = new ContactOpenHelper(v.getContext());
            Cursor cursor = openHelper.getWritableDatabase().query(ContactsContract.Contacts.TABLE_NAME , new String[]{ContactsContract.Contacts.COLUMN_NAME },
                    ContactsContract.Contacts._ID+" = ? " , new String[]{Integer.toString(position)} , null , null ,null);
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME));
            Toast.makeText(v.getContext(), "name : " + name, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(v.getContext() , DetailContactActivity.class);
            intent.putExtra("name" , name);
            v.getContext().startActivity(intent);
        }
    }
