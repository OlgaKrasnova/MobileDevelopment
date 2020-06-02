package com.example.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class EditActivity extends AppCompatActivity {

    public  static final int ADD = 0;
    public  static final int EDIT = 1;
    public  static final int SELECT_IMAGE = 2;
    public static final String PHOTO = "PHOTO";
    public static final String SURNAME = "SURNAME";
    public static final String NAME = "NAME";
    public static final String PATRONIMYC = "PATRONIMYC";
    public static final String PHONE = "PHONE";
    public static final String EMAIL = "EMAIL";
    public static final String POSITION = "POSITION";
    Intent result;
    ImageView imageEdit;
    EditText editSurname, editName, editPatronimyc, editPhone, editEmail;
    int position;
    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        imageEdit = findViewById(R.id.imageEdit);
        editSurname = findViewById(R.id.editSurname);
        editName = findViewById(R.id.editName);
        editPatronimyc = findViewById(R.id.editPatronimyc);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);

        Intent intent = getIntent();
        if (intent.getExtras() != null){
            editSurname.setText(intent.getExtras().getString(SURNAME));
            editName.setText(intent.getExtras().getString(NAME));
            editPatronimyc.setText(intent.getExtras().getString(PATRONIMYC));
            editPhone.setText(intent.getExtras().getString(PHONE));
            editEmail.setText(intent.getExtras().getString(EMAIL));
            position = intent.getExtras().getInt(POSITION);
            if(intent.getExtras().getString(PHOTO) != null){
                photoUri = Uri.parse(intent.getExtras().getString(PHOTO));
                imageEdit.setImageURI(photoUri);
            }
        }

        result = new Intent();
        setResult(RESULT_CANCELED, result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            if(requestCode == SELECT_IMAGE) {
                photoUri = data.getData();
                imageEdit.setImageURI(photoUri);
            }
        }
    }

    public void onSaveClick(View view) {
        result.putExtra(SURNAME, editSurname.getText().toString());
        result.putExtra(NAME, editName.getText().toString());
        result.putExtra(PATRONIMYC, editPatronimyc.getText().toString());
        result.putExtra(PHONE, editPhone.getText().toString());
        result.putExtra(EMAIL, editEmail.getText().toString());
        result.putExtra(POSITION, position);
        if(photoUri != null){
            result.putExtra(PHOTO, photoUri.toString());
        }
        setResult(RESULT_OK, result);
        finish();
    }

    public void onSelectPhoto(View view){
        String action;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            action = Intent.ACTION_OPEN_DOCUMENT;
        } else {
            action = Intent.ACTION_PICK;
        }
        Intent intent = new Intent(action);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }
}
