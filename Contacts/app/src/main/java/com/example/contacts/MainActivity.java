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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_EXTERNAL_STORAGE};

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
                final String photoString = data.getStringExtra(EditActivity.PHOTO);                list.add(contact);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                        App.app.db.contactDao().create(contact);
                        handler.sendEmptyMessage(0);
                    }
                });
                thread.start();
            }
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

            //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(App.app.getContentResolver(), bitmap, String.valueOf(contact.id), null);
            contact.photo = Uri.parse(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
