package com.example.airsense.authenticators;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airsense.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener
{   TextInputLayout fullname,phone,email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        Button bSignUp = findViewById(R.id.bSignUp);
        fullname = findViewById(R.id.tilUsername);
        phone= findViewById(R.id.tilPhone);
        email = findViewById(R.id.tilEmail);
        password = findViewById(R.id.tilPassword);





        bSignUp.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {
            case R.id.bSignUp:
                if (!validateFullName() | !validatePhoneNumber() | !validateEmail() | !validatePassword()) {
                    return;
                }
                final String val = phone.getEditText().getText().toString().trim();
                final String _phone = "+"+ 91 + val;



                Query checkuser = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(_phone);
                checkuser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            Toast.makeText(SignUpActivity.this, "User Already Exist", Toast.LENGTH_SHORT).show();


                        } else {
                            String name = fullname.getEditText().getText().toString().trim();
                            String emailid = email.getEditText().getText().toString().trim();
                            String password1 = password.getEditText().getText().toString().trim();

                            Intent intent = new Intent(SignUpActivity.this, PhoneVerificationActivity.class);
                            fullname = findViewById(R.id.tilUsername);
                            phone= findViewById(R.id.tilPhone);
                            email = findViewById(R.id.tilEmail);
                            password = findViewById(R.id.tilPassword);
                            intent.putExtra("username",name);
                            intent.putExtra("user_email",emailid);
                            intent.putExtra("user_password",password1);
                            intent.putExtra("phoneNo",_phone);
                            startActivity(intent);


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                        Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
                break;


            case R.id.tvSignIn:
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }


    private boolean validateFullName() {
        String val = fullname.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            fullname.setError("Field can not be empty");
            return false;
        } else {
            fullname.setError(null);
            fullname.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = email.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";

        if (val.isEmpty()) {
            email.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail)) {
            email.setError("Invalid Email!");
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = password.getEditText().getText().toString().trim();
        String checkPassword ="^" + "(?=.*[@#$%^&+=])" +     // at least 1 special character
                "(?=\\S+$)" +           // no white spaces
                ".{4,}" +                // at least 4 characters
                "$";

        if (val.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkPassword)) {
            password.setError("Password should contain 4 characters!");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }


    private boolean validatePhoneNumber() {
        String val = phone.getEditText().getText().toString().trim();
        String checkspaces = "[0-9]{10}";
        if (val.isEmpty()) {
            phone.setError("Enter Phone Number");
            return false;
        } else if (!val.matches(checkspaces)) {
            phone.setError("Enter valid phone number");
            return false;
        } else {
            phone.setError(null);
            phone.setErrorEnabled(false);
            return true;
        }
    }

}
