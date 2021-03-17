package com.example.airsense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class resetpassword extends AppCompatActivity {

    ImageView screenicon;
    TextView title,description;
    TextInputLayout phonenumberfield;
    Button nxtbutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_activity);

        screenicon = findViewById(R.id.forget_password_icon);
        title = findViewById(R.id.forget_password_title);
        description = findViewById(R.id.forget_password_description);
        nxtbutton =findViewById(R.id.button1);
        phonenumberfield = findViewById(R.id.phone);

        nxtbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validatePhoneNumber()){
                    return;
                }
                verifyPhoneNumber();
            }



        });


    }
    public void verifyPhoneNumber() {


        final String _phonenumber = phonenumberfield.getEditText().getText().toString().trim();
        final String _phone = "+" + 91 + _phonenumber;

        Query checkuser = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(_phone);
        checkuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    phonenumberfield.setError(null);
                    phonenumberfield.setErrorEnabled(false);
                    Intent intent = new Intent(resetpassword.this,PhoneVerificationActivity.class);
                    intent.putExtra("phoneNo",_phone);
                    intent.putExtra("whatToDo","updateData");
                    startActivity(intent);
                    finish();

                } else {
                    phonenumberfield.setError("No such user");
                    phonenumberfield.requestFocus();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(resetpassword.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }

    private boolean validatePhoneNumber() {
        String val = phonenumberfield.getEditText().getText().toString().trim();
        String checkspaces = "[0-9]{10}";
        if (val.isEmpty()) {
            phonenumberfield.setError("Enter Phone Number");
            return false;
        } else if (!val.matches(checkspaces)) {
            phonenumberfield.setError("Enter valid phone number");
            return false;
        } else {
            phonenumberfield.setError(null);
            phonenumberfield.setErrorEnabled(false);
            return true;
        }
    }
}