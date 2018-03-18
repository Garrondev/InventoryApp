package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

/**
 * Created by gcdev on 3/7/2018.
 */

public class MerchandiseDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABSE_NAME = "inventory.db";

    /**
     * If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    private String SQL_CREATE_ENTRIES = "CREATE TABLE " + MerchandiseEntry.TABLE_NAME + " ("
            + MerchandiseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MerchandiseEntry.COLUMN_MERCHANDISE_NAME + " TEXT NOT NULL, "
            + MerchandiseEntry.COLUMN_MERCHANDISE_PRICE + " NUMERIC DEFAULT 0, "
            + MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR + " TEXT NOT NULL);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + MerchandiseEntry.TABLE_NAME;

    public MerchandiseDbHelper(Context context) {
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
