package com.example.smartpantrymanager.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pantry_items")
public class PantryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double quantity;
    public String unit;      // "g", "ml" or "pcs"
    public Long expiryDate;  // epoch millis, can be null
}