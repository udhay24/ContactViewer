package com.example.udhay.contactviewer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;
import com.example.udhay.contactviewer.contact_database.ContactsContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final Uri contactUri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    public static Cursor contactCursor;
    private static final int LOADER_ID = 100;
    public static RecyclerView contactRecyclerView;
    public static ContactAdapter contactAdapter;
    public static int launch = 0;

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
        } else {
            loadContact();
        }

        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            launch ++;
            // first time task

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.v("loader Creation", "Loader is Created");
        return new ContactAsyncTask(this, contactUri);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        Log.v("loader finished", "inside loader finished");
        contactCursor = data;

        contactAdapter = new ContactAdapter(data);
        contactRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contactRecyclerView.setAdapter(contactAdapter);

        refresh();
        Log.v("display", Integer.toString(contactCursor.getCount()));


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //this method is used to check the country code
    private String prepareNumber(String number) {
        if (number.charAt(0) == '+') {
            return number;
        } else {
            return ("+91" + number);
        }
    }

    private void loadContact() {
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

        Toast.makeText(this, "swipe left to call ", Toast.LENGTH_LONG).show();
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

    public void refresh() {

        ContactsReload refresh = new ContactsReload(this);
        refresh.execute(contactCursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        launch++;
    }
}
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
