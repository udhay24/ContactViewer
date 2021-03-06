package com.example.udhay.contactviewer.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.udhay.contactviewer.MainActivity;
import com.example.udhay.contactviewer.R;
import com.example.udhay.contactviewer.contact_database.ContactOpenHelper;

public class DetailContactActivity extends AppCompatActivity {
    //This uri points to the android contact database
    public static final Uri contactUri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    //Contact name
    static String name;

    //Contact position
    static int position;

    //Recycler view for the current detail screen
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contact);

        //get the name of the contact to be displayed
        Intent startIntent = getIntent();
        name =startIntent.getStringExtra("name");
        position = startIntent.getIntExtra("position" , 0);

setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Cursor cursor = getContentResolver().query(contactUri , new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER} ,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"=?" , new String[]{name} ,
                null);



        mRecyclerView = findViewById(R.id.contact_numbers);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        customAdapter adapter = new customAdapter(this , cursor);

        mRecyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        cursor.moveToFirst();
    }

//    TODO(5) provide the delete option to delete the contact from custom database

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:

                this.finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }
}
class customAdapter extends RecyclerView.Adapter<customAdapter.NumberViewHolder>{

    Cursor mCursor;

    Context mContext;


    public customAdapter(Context context , Cursor cursor){
        mCursor = cursor;
        mContext = context;
    }

    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_contact_number , parent , false);

        return  new NumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        holder.getNumber().setText(mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        final NumberViewHolder temoHolder = holder;
        holder.getNumber().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String contactNumber = temoHolder.getNumber().getText().toString();
                setDefaultNumber(DetailContactActivity.name , contactNumber);
                MainActivity.contactAdapter.notifyItemChanged(position);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    class NumberViewHolder extends RecyclerView.ViewHolder {

        public TextView number;

        public NumberViewHolder(View view){
            super(view);
            number = view.findViewById(R.id.detail_number);

        }

        public TextView getNumber() {
            return number;
        }
    }

    private void setDefaultNumber(String name , String number){

        ContactOpenHelper openHelper = new ContactOpenHelper(mContext);

        SQLiteDatabase database = openHelper.getWritableDatabase();

        ContentValues defaultNumber = new ContentValues();

        defaultNumber.put(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.DEFAULT_NUMBER ,number );

        database.update(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.TABLE_NAME , defaultNumber,
                com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.COLUMN_NAME + "=?",
                new String[]{name});

        database.close();

        database = openHelper.getReadableDatabase();

        Cursor cursor = database.query(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.TABLE_NAME ,
                new String[]{com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.DEFAULT_NUMBER} ,
                com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.COLUMN_NAME + "=?" ,
                new String[]{name} , null , null , null);
        cursor.moveToFirst();

        String defaultContactNumber = cursor.getString(cursor.getColumnIndex(com.example.udhay.contactviewer.contact_database.ContactsContract.Contacts.DEFAULT_NUMBER));

        Toast.makeText(mContext , "Default number is changed to : " + defaultContactNumber , Toast.LENGTH_SHORT ).show();
    }

}
