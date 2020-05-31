package com.example.contacts;

import android.net.Uri;


public class Contact {
    public int id;
    public String surname;
    public String name;
    public String patronimyc;
    public String phone;
    public String email;
    public Uri photo;

    @Override
    public String toString(){
        return surname
                + (name.isEmpty() ? ""
                : (surname.isEmpty() ? name : " " + name))
                + (patronimyc.isEmpty() ? ""
                : (name.isEmpty() ? patronimyc : " " + patronimyc));
    }
}
