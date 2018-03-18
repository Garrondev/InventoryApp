
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.MerchandiseContract;
import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    Cursor cursor;

    // Identifies the particular loader being used
    private static final int INVENTORY_LOADER = 0;

    public static Uri QUANTITY_DECREASE_URI;
    public static ContentValues QUANTITY_DECREASE_VALUES;

    // The adapter being used to display the list's data.
    MerchandiseCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the listView which will be populated with the pet data
        ListView merchandiseListView = (ListView) findViewById(R.id.list_view);

        // Find the empty view
        View emptyView = findViewById(R.id.empty_view);

        // Create MerchandiseCursorAdapter
        mCursorAdapter = new MerchandiseCursorAdapter(this, cursor);

        // Set adapter and empty view for the list view.
        merchandiseListView.setEmptyView(emptyView);
        merchandiseListView.setAdapter(mCursorAdapter);

        // Set an on item click listener
        merchandiseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                // Create the Uri for the clicked item
                Uri currentPetUri = ContentUris.withAppendedId(MerchandiseEntry.CONTENT_URI, id);
                Log.e("Activity", "uri is "+currentPetUri);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_inventory.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        deleteInventory();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteInventory() {
        int rowsDeleted = getContentResolver().delete(MerchandiseContract.MerchandiseEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table
        // we care about
        String[] projection = {
                MerchandiseEntry._ID,
                MerchandiseEntry.COLUMN_MERCHANDISE_NAME,
                MerchandiseEntry.COLUMN_MERCHANDISE_PRICE,
                MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY};
        return new CursorLoader(
                this,                   // Parent activity context
                MerchandiseEntry.CONTENT_URI,   // Provider content URI to query
                projection,                     // Column to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in the the framework will take care of
        // closing the old cursor once we return.
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
