package com.example.airsense.Databases;

public class UserHelperClass {

    String fullname,email,password,phone;
    //public UserHelperClass(){}

    public UserHelperClass(String fullname,String email,String password,String phone) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
