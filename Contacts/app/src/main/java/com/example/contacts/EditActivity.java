package com.example.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class EditActivity extends AppCompatActivity {

    public  static final int ADD = 0;
    public  static final int EDIT = 1;
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
        }

        result = new Intent();
        setResult(RESULT_CANCELED, result);
    }

    public void onSaveClick(View view) {
        result.putExtra(SURNAME, editSurname.getText().toString());
        result.putExtra(NAME, editName.getText().toString());
        result.putExtra(PATRONIMYC, editPatronimyc.getText().toString());
        result.putExtra(PHONE, editPhone.getText().toString());
        result.putExtra(EMAIL, editEmail.getText().toString());
        result.putExtra(POSITION, position);
        setResult(RESULT_OK, result);
        finish();
    }
}
