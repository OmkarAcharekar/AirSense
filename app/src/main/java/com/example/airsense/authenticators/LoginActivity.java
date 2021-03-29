package com.example.airsense.authenticators;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airsense.MainActivity1;
import com.example.airsense.MainActivityside;
import com.example.airsense.R;
import com.example.airsense.resetpassword;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static String LOG_TAG = "BlueSignIn";
    TextInputLayout phone, password;
    RelativeLayout progressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button bSignIn = findViewById(R.id.bSignIn);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        phone = findViewById(R.id.tilEmail);
        password = findViewById(R.id.tilPassword);
        progressbar = findViewById(R.id.loginprogress);


        bSignIn.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(!isConnected(this)){
            showCustomDialog();
        }

        if (R.id.bSignIn == view.getId()) {

            if (!validatePhoneNumber()|!validatePassword()) {
                return;
            }
        }

        progressbar.setVisibility(view.VISIBLE);


        String _phonenumber = phone.getEditText().getText().toString().trim();
        final String password1 = password.getEditText().getText().toString().trim();
        final String _phone = "+" + 91 + _phonenumber;
        switch (view.getId()) {
            case R.id.bSignIn:

                Query checkuser = FirebaseDatabase.getInstance().getReference("Users").orderByChild("phone").equalTo(_phone);
                checkuser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        phone.setError(null);
                        phone.setErrorEnabled(false);
                        if (snapshot.exists()) {
                            String systemPass = snapshot.child(_phone).child("password").getValue(String.class);
                            if (systemPass.equals(password1)) {
                                password.setError(null);
                                password.setErrorEnabled(false);
                                String fullname =  snapshot.child(_phone).child("fullname").getValue(String.class);
                                Toast.makeText(LoginActivity.this,fullname, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivityside.class);
                                startActivity(intent);
                                finish();

                            } else {
                                progressbar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "password does not match", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressbar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressbar.setVisibility(View.GONE);

                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
                break;


            case R.id.tvForgotPassword:
                startActivity(new Intent(LoginActivity.this, resetpassword.class));
                break;
            case R.id.tvSignUp:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("Please connect to the internet to proceed further").setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(getApplicationContext(), MainActivity1.class));
                        finish();

                    }
                });
    }

    private boolean isConnected(LoginActivity loginActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) loginActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wificonn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileconn  = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wificonn != null && wificonn.isConnected()) || (mobileconn != null && mobileconn.isConnected())){

            return true;

        }else{
            return false;
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
