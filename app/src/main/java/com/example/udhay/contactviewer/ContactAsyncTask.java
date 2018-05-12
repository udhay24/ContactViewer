package com.example.udhay.contactviewer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactAsyncTask extends android.support.v4.content.AsyncTaskLoader<Cursor>{
   private static Uri contactUri ;
   private final Context context;

    public ContactAsyncTask(Context context , Uri... uri) {
        super(context);
        this.context = context;
        contactUri = uri[0];

    }

    @Override
    public Cursor loadInBackground() {

        return context.getContentResolver().query(contactUri , null , null , null ,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        }
}

