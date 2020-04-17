package com.example.photo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 0;
    private static final int PICTURE_CROP = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;
    private ImageView imageView;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Start your camera handling here
                } else {
                    Toast.makeText(this, "Нет доступа к камере", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void onClick(View view) {
        //getPrePhoto();
        getFullPhoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == CAMERA_REQUEST){
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageURI(photoUri);
                crop();
            }
            if(requestCode == PICTURE_CROP) {
                Bundle extras = data.getExtras();
                Bitmap picture = extras.getParcelable("data");
                imageView.setImageBitmap(picture);
            }
        }
    }

    private void crop(){
        try {
            Intent cropIntent = new Intent();
            cropIntent.setAction("com.android.camera.action.CROP");
            cropIntent.setDataAndType(photoUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PICTURE_CROP);
        } catch(ActivityNotFoundException error){
            Toast.makeText(this, "Не поддерживается кадрирование", Toast.LENGTH_LONG).show();
        }
    }

    private void getPrePhoto() {
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void getFullPhoto(){
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null){
            File file = null;
            try{
                file = createPhotoFile();
            } catch (IOException error) {
                Toast.makeText(this, "Ошибка создания файла", Toast.LENGTH_LONG).show();
            }
            if (file != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoUri = FileProvider.getUriForFile(
                            this, "com.example.android.provider", file);
                } else {
                    photoUri = Uri.fromFile(file);
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createPhotoFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = File.createTempFile(timeStamp, ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        return file;
    }
}
