package com.example.airsense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetNewPassword extends AppCompatActivity {

    Button b1;
    TextInputLayout password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);


        b1 =findViewById(R.id.setupass);
        password = findViewById(R.id.new_password);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               setNewPassword();
            }
        });

    }

    private void setNewPassword() {
        String _newpassword = password.getEditText().getText().toString().trim();
        String _phoneNo = getIntent().getStringExtra("phoneNo");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(_phoneNo).child("password").setValue(_newpassword);
        startActivity(new Intent(SetNewPassword.this,ForgetPasswordSuccessMessage.class));
        finish();
    }
}