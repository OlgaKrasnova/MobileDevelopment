package com.example.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String CONTACT_ID = ContactsContract.Contacts._ID;
    public static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    public static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    public static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    public static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

    public static final String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    public static final String EMAIL_ADDRESS = ContactsContract.CommonDataKinds.Email.ADDRESS;

    public static final String NAME_CONTACT_ID = ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID;
    public static final String NAME_NAME = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
    public static final String NAME_SURNAME = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
    public static final String NAME_PATRONIMYC = ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME;

    public static final int ID_SHIFT = 1000;

    public static final int REQUEST_READ_CONTACTS = 3;
    int requestCount = 0;

    RecyclerView recyclerView;
    ContactAdapter adapter;
    List<Contact> list;

    static Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = new ArrayList<>();
        adapter = new ContactAdapter(this, list);
        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        handler = new Handler(){
            public void handleMessage(@NonNull Message message) {
                adapter.list = list;
                adapter.notifyDataSetChanged();
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                list = App.app.db.contactDao().readAll();
                for (Contact contact:list) {
                    loadPhoto(contact);
                }
                getPermissionsForContacts();
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == EditActivity.ADD){
                final Contact contact = new Contact();
                contact.surname = data.getStringExtra(EditActivity.SURNAME);
                contact.name = data.getStringExtra(EditActivity.NAME);
                contact.patronimyc = data.getStringExtra(EditActivity.PATRONIMYC);
                contact.phone = data.getStringExtra(EditActivity.PHONE);
                contact.email = data.getStringExtra(EditActivity.EMAIL);
                final String photoString = data.getStringExtra(EditActivity.PHOTO);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        contact.id = App.app.db.contactDao().count();
                        list.add(contact);
                        if (photoString != null) {
                            savePhoto(Uri.parse(photoString), contact);
                        }
                        App.app.db.contactDao().create(contact);
                        handler.sendEmptyMessage(0);
                    }
                });
                thread.start();
            }
            if (requestCode == EditActivity.EDIT){
                int position = data.getIntExtra(EditActivity.POSITION, 0);
                final Contact contact = list.get(position);
                contact.surname = data.getStringExtra(EditActivity.SURNAME);
                contact.name = data.getStringExtra(EditActivity.NAME);
                contact.patronimyc = data.getStringExtra(EditActivity.PATRONIMYC);
                contact.phone = data.getStringExtra(EditActivity.PHONE);
                contact.email = data.getStringExtra(EditActivity.EMAIL);
                final String photoString = data.getStringExtra(EditActivity.PHOTO);
                list.set(position, contact);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (photoString != null) {
                            savePhoto(Uri.parse(photoString), contact);
                        }
                        App.app.db.contactDao().update(contact);
                        handler.sendEmptyMessage(0);
                    }
                });
                thread.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_READ_CONTACTS :
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        getPermissionsForContacts();
                    }
                };
                Thread thread = new Thread(r);
                thread.start();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, EditActivity.class );
        startActivityForResult(intent, EditActivity.ADD);
    }

    public void savePhoto(Uri photoUri, Contact contact){
        contact.photo = photoUri;
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        File path = new File(directory, String.valueOf(contact.id) + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e ){
            e.printStackTrace();
        } finally {
            try {
                if(fos != null){
                    fos.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadPhoto(Contact contact) {
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        try {
            File photoFile = new File(directory, String.valueOf(contact.id) + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFile));
            String path = MediaStore.Images.Media.insertImage(App.app.getContentResolver(), bitmap, String.valueOf(contact.id), null);
            contact.photo = Uri.parse(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getPermissionsForContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) && requestCount > 0) {
                    Log.d("polytech", "Не удалось получить доступ к контактам.");
                } else {
                    requestCount ++;
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                }
            } else {
                getContacts();
            }
        } else {
            getContacts();
        }
    }

    private void getContacts(){
        //Получение телефонов
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_CONTACT_ID, PHONE_NUMBER},null, null, null);
        HashMap<Integer, ArrayList<String>> phones = new HashMap<>();
        if(cursorPhone != null && cursorPhone.getCount() > 0){
            while(cursorPhone.moveToNext()){
                int id = cursorPhone.getInt(cursorPhone.getColumnIndex(PHONE_CONTACT_ID));
                ArrayList<String> current = new ArrayList<>();
                if(phones.containsKey(id)){
                    current = phones.get(id);
                }
                current.add(cursorPhone.getString(cursorPhone.getColumnIndex(PHONE_NUMBER)));
                phones.put(id, current);
            }
        }
        cursorPhone.close();

        //Получение электронных почт
        Cursor cursorEmail = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{EMAIL_CONTACT_ID, EMAIL_ADDRESS},null, null, null);
        HashMap<Integer, ArrayList<String>> emails = new HashMap<>();
        if(cursorEmail != null && cursorEmail.getCount() > 0){
            while(cursorEmail.moveToNext()){
                int id = cursorEmail.getInt(cursorEmail.getColumnIndex(EMAIL_CONTACT_ID));
                ArrayList<String> current = new ArrayList<>();
                if(emails.containsKey(id)){
                    current = emails.get(id);
                }
                current.add(cursorEmail.getString(cursorEmail.getColumnIndex(EMAIL_ADDRESS)));
                emails.put(id, current);
            }
        }
        cursorEmail.close();

        //Получение ФИО
        String where = ContactsContract.Data.MIMETYPE + " = ?";
        String[] params = new String[] {ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor cursorFIO = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{NAME_CONTACT_ID, NAME_SURNAME, NAME_NAME, NAME_PATRONIMYC},
                where, params, null);

        HashMap<Integer, FIO> fios = new HashMap<>();
        if (cursorFIO != null && cursorFIO.getCount() > 0 && cursorFIO.moveToFirst()) {
            while (cursorFIO.moveToNext()) {
                int id = cursorFIO.getInt(cursorFIO.getColumnIndex(NAME_CONTACT_ID));
                String surname = cursorFIO.getString(cursorFIO.getColumnIndex(NAME_SURNAME));
                String name = cursorFIO.getString(cursorFIO.getColumnIndex(NAME_NAME));
                String patronimyc = cursorFIO.getString(cursorFIO.getColumnIndex(NAME_PATRONIMYC));
                fios.put(id, new FIO(surname, name, patronimyc));
            }
        }
        cursorFIO.close();

        //Подключаемся к общей таблице контактов
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{CONTACT_ID, DISPLAY_NAME},
                null, null, DISPLAY_NAME + " ASC");
        if(cursor == null || cursor.getCount() <= 0) {
            return;
        }
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(CONTACT_ID));
            Contact contact = new Contact();
            contact.id = ID_SHIFT + id;
            contact.name = (fios.get(id) == null || fios.get(id).name == null ? "" : fios.get(id).name);
            contact.surname = (fios.get(id) == null || fios.get(id).surname == null ? "" : fios.get(id).surname);
            contact.patronimyc = (fios.get(id) == null || fios.get(id).patronimyc == null ? "" : fios.get(id).patronimyc);
            contact.phone = TextUtils.join(", ", phones.get(id).toArray());
            contact.email = TextUtils.join(", ", emails.get(id).toArray());
            list.add(contact);
        }
        cursor.close();
    }

    private class FIO {
        String surname;
        String name;
        String patronimyc;

        FIO(String surname, String name, String patronimyc){
            this.surname = surname;
            this.name = name;
            this.patronimyc = patronimyc;

        }
    }
}
