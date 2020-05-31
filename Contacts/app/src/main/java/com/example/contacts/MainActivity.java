package com.example.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ContactAdapter adapter;
    List<Contact> list;

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
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == EditActivity.ADD){
                Contact contact = new Contact();
                contact.surname = data.getStringExtra(EditActivity.SURNAME);
                contact.name = data.getStringExtra(EditActivity.NAME);
                contact.patronimyc = data.getStringExtra(EditActivity.PATRONIMYC);
                contact.phone = data.getStringExtra(EditActivity.PHONE);
                contact.email = data.getStringExtra(EditActivity.EMAIL);
                list.add(contact);
                adapter.notifyDataSetChanged();
            }
            if (requestCode == EditActivity.EDIT){
                int position = data.getIntExtra(EditActivity.POSITION, 0);
                Contact contact = list.get(position);
                contact.surname = data.getStringExtra(EditActivity.SURNAME);
                contact.name = data.getStringExtra(EditActivity.NAME);
                contact.patronimyc = data.getStringExtra(EditActivity.PATRONIMYC);
                contact.phone = data.getStringExtra(EditActivity.PHONE);
                contact.email = data.getStringExtra(EditActivity.EMAIL);
                list.set(position, contact);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, EditActivity.class );
        startActivityForResult(intent, EditActivity.ADD);
    }
}
