package com.example.contacts;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert
    void create(Contact contact);

    @Query("SELECT COUNT(id) FROM contacts")
    int count();

    @Query("SELECT * FROM contacts")
    List<Contact> readAll();

    @Query("SELECT * FROM contacts WHERE id = :position")
    Contact read(int position);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);
}
