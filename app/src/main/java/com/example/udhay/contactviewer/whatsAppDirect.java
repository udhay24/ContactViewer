package com.example.udhay.contactviewer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class whatsAppDirect extends AppCompatActivity {

    EditText numberEditTextView;
    EditText messageEditTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app_direct);


        numberEditTextView = findViewById(R.id.numberText);
        messageEditTextView = findViewById(R.id.messageText);

        Button sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = "+91" + numberEditTextView.getText().toString();
                String message = messageEditTextView.getText().toString();
                if(!isNumberValid(number)){
                    Toast.makeText(whatsAppDirect.this , "Enter a correct number" , Toast.LENGTH_SHORT).show();
                }else {
                    Log.v("message :" , message);
                    Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW);
                    whatsAppIntent.setType("text/plain");
                    whatsAppIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone="+number + "&text=" +message));
                    whatsAppIntent.setPackage("com.whatsapp");
                    startActivity(whatsAppIntent);
                }
            }
        });
    }

    private boolean isNumberValid(String number){
        return number.length() == 13 ? true : false;
    }
}
