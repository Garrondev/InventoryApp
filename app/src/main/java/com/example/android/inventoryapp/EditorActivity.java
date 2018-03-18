package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

/**
 * Created by gcdev on 3/11/2018.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the item's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the item's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the item's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the item vendor's contact information
     */
    private EditText mVendorEditText;

    private int EDIT_LOADER = 1;

    private boolean mItemHasChanged = false;

    Uri mItemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mItemUri = intent.getData();

        if (mItemUri != null) {
            setTitle(R.string.editor_activity_title_edit);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_title_add);
        }

        // Find all relevent views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mVendorEditText = (EditText) findViewById(R.id.edit_item_vendor);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mVendorEditText.setOnTouchListener(mTouchListener);

        getSupportLoaderManager().initLoader(EDIT_LOADER, null, this);
    }

    /**
     * helper method to verify appropriate entries were entered in the edit text
     */
    private void checkItem() {
        // check that the NameEditText is not empty
        boolean noName = mNameEditText.getText().toString().equals("");
        if (noName) {
            Toast.makeText(this, R.string.insert_name_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        // check that there is a Price: if not, set the price to zero.
        boolean noPrice = mPriceEditText.getText().toString().equals("");
        if (noPrice) {
            mPriceEditText.setText("0");
        }
        // check that there is a set quantity: if not, set the quantity to 0.
        boolean noQuantity = mQuantityEditText.getText().toString().equals("");
        if (noQuantity) {
            mQuantityEditText.setText("0");
        }
        // check that there is a set vendor email address.
        boolean noVendor = mVendorEditText.getText().toString().equals("");
        if (noVendor) {
            Toast.makeText(this, R.string.insert_vendor_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        //save the entered data
            saveItem();
        }

    private void saveItem() {
        // Read input fields,, trim() removes excess white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String vendorString = mVendorEditText.getText().toString().trim();
        double price = Double.parseDouble(priceString);
        int quantity = Integer.parseInt(quantityString);

        // Content values to enter into database as a row (key, data)
        ContentValues values = new ContentValues();
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_NAME, nameString);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE, price);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY, quantity);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR, vendorString);

        if (mItemUri == null) {
            // Insert a new item into the provider, if we're in insert mode,
            // returning the content URI for the new
            Uri newUri = getContentResolver().insert(
                    MerchandiseEntry.CONTENT_URI,
                    values);
            // Show a toast message depending on whether or not the insertion was success
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.item_not_added), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update an existing item, if we're in update/edit mode,
            // Returning the content URI
            int rowsAffected = getContentResolver().update(mItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // if no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.editor_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_success), Toast.LENGTH_SHORT).show();
            }
        }
        // Leave the activity
        finish();
    }

    private void showDeletedConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_warning);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue the editing.
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        // Creat and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mItemUri
            // content URI already identifies the item we want.
            int deletedRows = getContentResolver().delete(mItemUri, null, null);
            if (deletedRows != 0) {
                Toast.makeText(this, R.string.editor_delete_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_failed, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        // This adds menu items tot he app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this si a new item, hid the "Delete" menu item.
        if (mItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Responds to a click on the "Save" menu option
            case R.id.action_save:
                // Check that the necessary data has been entered
                checkItem();
                return true;
            // respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete the current item
                showDeletedConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@Link InventoryActivity}
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mItemUri == null) {
            return null;
        }
        // Define a projection that specifies the columns from the table
        // that we will retrieve data from.
        String[] projection = {
                MerchandiseEntry._ID,
                MerchandiseEntry.COLUMN_MERCHANDISE_NAME,
                MerchandiseEntry.COLUMN_MERCHANDISE_PRICE,
                MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY
        };
        return new CursorLoader(
                this,
                mItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Get item name and set it to the name edit text
            int nameColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_NAME);
            String itemName = cursor.getString(nameColumnIndex);
            mNameEditText.setText(itemName);

            // Get item price and set it to the price edit text
            int priceColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE);
            String itemPrice = String.valueOf(cursor.getDouble(priceColumnIndex));
            mPriceEditText.setText(itemPrice);

            // Get item quantity and set it to the quantity edit text
            int quantityColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY);
            String itemQuantity = String.valueOf(cursor.getInt(quantityColumnIndex));
            mQuantityEditText.setText(itemQuantity);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    /**
     * OnTouch listener that listens for any user touches on a View, implying that
     * they are modifying the view, and we change the mItemHasChanged boolean to
     * true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent me) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handing the back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes
        // should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "keep editing" button, so dismiss the dialog
                // and continue the editing
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the Alert Dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
