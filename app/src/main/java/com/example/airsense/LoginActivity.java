package com.example.airsense;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    public static String LOG_TAG = "BlueSignIn";
    TextInputLayout email,password;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button bSignIn = findViewById(R.id.bSignIn);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        email = findViewById(R.id.tilEmail);
        password = findViewById(R.id.tilPassword);

        bSignIn.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {   if( R.id.bSignIn == view.getId()){

        if (!validateEmail() | !validatePassword()) {
            return;
        }}
        switch (view.getId())

        {
            case R.id.bSignIn:
                startActivity(new Intent(LoginActivity.this,MainActivityside.class));
                break;
            case R.id.tvForgotPassword:
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                break;
            case R.id.tvSignUp:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
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
        String checkPassword =
                "^" + "(?=.*[@#$%^&+=])" +     // at least 1 special character
                        "(?=\\S+$)" +           // no white spaces
                        ".{4,}" +                // at least 4 characters
                        "$";           //at least 4 characters


        if (val.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkPassword)) {
            password.setError("Password is too weak");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }
}
