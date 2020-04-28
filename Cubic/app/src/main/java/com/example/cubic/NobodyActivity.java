package com.example.cubic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class NobodyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nobody);
    }

    public void onClick(View view) {
        Intent i = new Intent(this, NobodyActivity.class);
        startActivity(i);
    }

}
