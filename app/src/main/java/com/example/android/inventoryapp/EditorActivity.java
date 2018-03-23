package com.example.android.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

import static com.example.android.inventoryapp.data.DbBitmapUtility.getBytes;
import static com.example.android.inventoryapp.data.DbBitmapUtility.getImage;

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
    /**
     * Contact vendor button.
     */
    private Button mContactVendorButton;
    /**
     * Item image.
     */
    private ImageView mItemImageView;
    private Button mUploadImageButton;

    private int EDIT_LOADER = 1;

    private boolean mItemHasChanged = false;

    Uri mItemUri;

    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mItemUri = intent.getData();

        if (mItemUri != null) {
            setTitle(R.string.editor_activity_title_edit);

            // Show the quantity increase and decrease buttons
            ImageButton increaseButton = findViewById(R.id.quantity_increase_button);
            ImageButton decreaseButton = findViewById(R.id.quantity_decrease_button);
            increaseButton.setVisibility(View.VISIBLE);
            decreaseButton.setVisibility(View.VISIBLE);
            // Set the onClickListeners on the buttons that update
            // the quantity by +/-1.
            increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    increaseQuantity();
                }
            });
            decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    decreaseQuantity();
                }
            });

            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_title_add);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mVendorEditText = (EditText) findViewById(R.id.edit_item_vendor);
        mItemImageView = (ImageView) findViewById(R.id.item_image_view);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mVendorEditText.setOnTouchListener(mTouchListener);

        // Set the onClickListener to the mContactVendorButton;
        mContactVendorButton = (Button) findViewById(R.id.contact_vendor_button);
        mContactVendorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactVendor();
            }
        });

        getSupportLoaderManager().initLoader(EDIT_LOADER, null, this);

        // Setup ImageView
        // Source for idea: http://viralpatel.net/blogs/pick-image-from-galary-android-app/
        mUploadImageButton = findViewById(R.id.upload_image_button);
        mUploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, GET_FROM_GALLERY);
            }
        });

        requestPermissions();
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }
    }

    // increase the quantity when the (+) button is tapped.
    private void increaseQuantity() {
        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());
        ContentValues increaseQuantityValue = new ContentValues();
        increaseQuantityValue.put(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY, (quantityInt + 1));
        getContentResolver().update(mItemUri, increaseQuantityValue, null, null);
    }

    // decrease the quantity when the (-) button is tapped.
    private void decreaseQuantity() {
        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());
        if (quantityInt < 1) {
            Toast.makeText(this, R.string.quantity_entry_negative, Toast.LENGTH_SHORT).show();
            return;
        } else {
            ContentValues decreaseQuantityValue = new ContentValues();
            decreaseQuantityValue.put(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY, (quantityInt - 1));
            getContentResolver().update(mItemUri, decreaseQuantityValue, null, null);
        }
    }

    // Test method
    private void test() {
        Toast.makeText(this, "This works", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.insert_price_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        // check that there is a set quantity: if not, set the quantity to 0.
        boolean noQuantity = mQuantityEditText.getText().toString().equals("");
        if (noQuantity) {
            Toast.makeText(this, R.string.insert_quantity_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        // check that there is a set vendor email address.
        boolean noVendor = mVendorEditText.getText().toString().equals("");
        if (noVendor) {
            Toast.makeText(this, R.string.insert_vendor_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        // check that there is an image
        if (mItemImageView.getDrawable() == null) {
            Toast.makeText(this, R.string.insert_image_invalid, Toast.LENGTH_LONG).show();
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
        Bitmap itemImage = ((BitmapDrawable) mItemImageView.getDrawable()).getBitmap();

        // Convert the bitmap to a byte array
        byte[] image = getBytes(itemImage);

        double price = Double.parseDouble(priceString);
        int quantity = Integer.parseInt(quantityString);

        // Content values to enter into database as a row (key, data)
        ContentValues values = new ContentValues();
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_NAME, nameString);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE, price);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY, quantity);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR, vendorString);
        values.put(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE, image);

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
        // Create and show the AlertDialog
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
                MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY,
                MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR,
                MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE
        };
        return new CursorLoader(
                this,
                mItemUri,
                projection,
                null,
                null,
                null);
    }

    /**
     * VendorContact String, ItemName String, will both used in
     * a later method.
     */
    String vendorContact = null;
    String itemName = null;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Get item name and set it to the name edit text
            int nameColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_NAME);
            itemName = cursor.getString(nameColumnIndex);
            mNameEditText.setText(itemName);

            // Get item price and set it to the price edit text
            int priceColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE);
            String itemPrice = String.valueOf(cursor.getDouble(priceColumnIndex));
            mPriceEditText.setText(itemPrice);

            // Get item quantity and set it to the quantity edit text
            int quantityColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY);
            String itemQuantity = String.valueOf(cursor.getInt(quantityColumnIndex));
            mQuantityEditText.setText(itemQuantity);

            // Get item vendor's contact and set it to the vendor's contact
            // EditText
            int vendorColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_VENDOR);
            vendorContact = cursor.getString(vendorColumnIndex);
            mVendorEditText.setText(vendorContact);

            // Get item image and set it to the imageview
            int imageColumnIndex = cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE);
            byte[] imageByte = cursor.getBlob(imageColumnIndex);
            mItemImageView.setImageBitmap(getImage(imageByte));
        }
    }

    String contact = null;
    String item = null;

    public void contactVendor() {
        // get the contact information for the EditText or ask the use to
        // enter that info if it's not already present.
        if (vendorContact != null) {
            contact = vendorContact;
        } else {
            Toast.makeText(this, R.string.vendor_intent_failed, Toast.LENGTH_LONG).show();
            return;
        }
        // get the product name from the EditText, or ask the user to
        // enter that info if it's not already present.
        if (itemName != null) {
            item = itemName;
        } else {
            Toast.makeText(this, R.string.item_intent_failed, Toast.LENGTH_LONG).show();
        }
        String[] contacts = {contact};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, contacts);
        intent.putExtra(Intent.EXTRA_SUBJECT, item);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mVendorEditText.setText("");
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

    /**
     * Source for idea: http://viralpatel.net/blogs/pick-image-from-galary-android-app/
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // String picturePath contains the path of selected Image
            mItemImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}
