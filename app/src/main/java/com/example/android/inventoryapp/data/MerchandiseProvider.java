package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

/**
 * Created by gcdev on 3/7/2018.
 */

public class MerchandiseProvider extends ContentProvider {

    private static final int MERCHANDISE = 100;
    private static final int MERCHANDISE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MerchandiseContract.CONTENT_AUTHORITY, MerchandiseContract.PATH_MERCHANDISE, MERCHANDISE);
        sUriMatcher.addURI(MerchandiseContract.CONTENT_AUTHORITY, MerchandiseContract.PATH_MERCHANDISE + "/#", MERCHANDISE_ID);
    }

    private MerchandiseDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new MerchandiseDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection
     * arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MERCHANDISE:
                // Query directly and collect all possible data
                cursor = database.query(MerchandiseEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MERCHANDISE_ID:
                selection = MerchandiseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(MerchandiseEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                Toast.makeText(getContext(), R.string.cursor_query_error, Toast.LENGTH_LONG).show();
                return null;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MERCHANDISE:
                return insertMerchandise(uri, contentValues);
            default:
                Toast.makeText(getContext(), R.string.insert_query_invalid,Toast.LENGTH_LONG).show();
                return null;
        }
    }

    /**
     * Insert a merchandise into the database with the given content values. Return the
     * new content URI for that specific row in the database.
     */
    private Uri insertMerchandise(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(MerchandiseEntry.COLUMN_MERCHANDISE_NAME);
        if (name == null) {
            Toast.makeText(getContext(), R.string.insert_name_invalid, Toast.LENGTH_LONG).show();
            return null;
        }

        // Check that the price is valid
        double price = values.getAsDouble(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE);
        if (price < 0) {
            Toast.makeText(getContext(), R.string.insert_price_negative, Toast.LENGTH_LONG).show();
            return null;
        }

        // Check that the quantity is not 0
        Integer quantity = values.getAsInteger(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY);
        if (quantity < 0) {
            Toast.makeText(getContext(), R.string.insert_quantity_negative, Toast.LENGTH_LONG).show();
            return null;
        }

        // Check that the vendor contact information is present
        String vendorContact = values.getAsString(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR);
        if (vendorContact == null) {
            Toast.makeText(getContext(),R.string.vendor_entry_invalid, Toast.LENGTH_LONG).show();
            return null;
        }

        // Check that the item has an image
        byte[] image = values.getAsByteArray(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE);
        if (image == null) {
            Toast.makeText(getContext(), R.string.insert_image_invalid, Toast.LENGTH_LONG).show();
            return null;
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // insert the new merchandise with the given values
        long id = database.insert(MerchandiseEntry.TABLE_NAME, null, values);

        // if the ID is -1, then the insertion failed, return null.
        if (id == -1) {
            Toast.makeText(getContext(), R.string.insert_failed, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments,
     * with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MERCHANDISE:
                return updateMerchandise(uri, contentValues, selection, selectionArgs);
            case MERCHANDISE_ID:
                // For the MERCHANDISE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and
                // arguments will be a string array containing the actual ID.
                selection = MerchandiseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMerchandise(uri, contentValues, selection, selectionArgs);
            default:
                Toast.makeText(getContext(), R.string.editor_update_failed, Toast.LENGTH_LONG).show();
                return -1;
        }
    }

    private int updateMerchandise(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.size() == 0) {
            return 0;
        }
        // Get writable database to update
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Check what data needs to be updated and create the required variables
        if (contentValues.containsKey(MerchandiseEntry.COLUMN_MERCHANDISE_NAME)) {
            String name = contentValues.getAsString(MerchandiseEntry.COLUMN_MERCHANDISE_NAME);
            if (name == null) {
                Toast.makeText(getContext(), R.string.insert_name_invalid, Toast.LENGTH_SHORT).show();
                return 0;
            }
        }
        if (contentValues.containsKey(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY);
            if (quantity < 0) {
                Toast.makeText(getContext(), R.string.quantity_entry_negative, Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        if (contentValues.containsKey(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE)) {
            double price = contentValues.getAsDouble(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE);
            if (price < 0.00) {
                Toast.makeText(getContext(), R.string.price_entry_negative, Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        if (contentValues.containsKey(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR)) {
            String vendorContact = contentValues.getAsString(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR);
            if(vendorContact == null) {
                Toast.makeText(getContext(), R.string.vendor_entry_invalid, Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        if (contentValues.containsKey(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE)) {
            byte[] image = contentValues.getAsByteArray(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE);
            if(image == null) {
                Toast.makeText(getContext(), R.string.insert_image_invalid, Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        // store the number of database rows affected by the update statement
        int rowsUpdated = database.update(MerchandiseEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // if rows were updated, notify all listeners that the data has changed
        // for the given uri
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        // Get writable database to update
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        switch (match) {
            case MERCHANDISE:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(MerchandiseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MERCHANDISE_ID:
                // Delete a single row given by the ID in the URI
                selection = MerchandiseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MerchandiseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                Toast.makeText(getContext(), R.string.delete_failed, Toast.LENGTH_LONG).show();
                rowsDeleted = 0;
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MERCHANDISE:
                return MerchandiseEntry.CONTENT_LIST_TYPE;
            case MERCHANDISE_ID:
                return MerchandiseEntry.CONTENT_ITEM_TYPE;
            default:
                Toast.makeText(getContext(), R.string.mime_type_issue, Toast.LENGTH_SHORT).show();
                return null;
        }
    }
}