package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PantryDao {
    @Insert long insert(PantryItem item);
    @Update void update(PantryItem item);
    @Delete void delete(PantryItem item);

    @Query("SELECT * FROM pantry_items ORDER BY name")
    List<PantryItem> getAll();

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(int id);
}