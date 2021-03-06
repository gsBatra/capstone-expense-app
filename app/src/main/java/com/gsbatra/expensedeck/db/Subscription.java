package com.gsbatra.expensedeck.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscriptions")
public class Subscription {

    // Note that if id == 0, and this is inserted, an id will be autogenerated
    public Subscription(int id, String title, double amount, String tag, String note) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.tag = tag;
        this.note = note;
    }

    // Assign 0 to id to have it be auto generated
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "tag")
    public String tag;

    @ColumnInfo(name = "note")
    public String note;
}
