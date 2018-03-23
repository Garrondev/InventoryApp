package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.MerchandiseContract.MerchandiseEntry;

import java.text.NumberFormat;

import static com.example.android.inventoryapp.data.DbBitmapUtility.getImage;
/**
 * Created by gcdev on 3/11/2018.
 */

/**
 *  {@Link MerchandiseCursorAdapater} is an adapter for a list or grid view
 *  that uses a {@Link Cursor} of merchandise data as its data source. This adapter
 *  knows how to create list items for each row of pet data in the {@Link Cursor}.
 */
public class MerchandiseCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@Link MerchandiseCursorAdapter}.
     *
     * @param context   The context
     * @param c         The cursor from which to get the data.
     */
    public MerchandiseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     *  Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     *  @param context app context
     *  @param cursor   The cursor from which to get the data. The cursor is already
     *                  moved to the correct position.
     *  @param parent   The parent to which the new view is attached to
     *  @return the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     *  This method binds the merchandise data (in the current row pointed to by
     *  cursor) to the given list item layout. For example, the name for the
     *  current merchandise can be set on the name TextView in the list item layout.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate
        TextView nameView = view.findViewById(R.id.name);
        TextView priceView = view.findViewById(R.id.price_text_view);
        TextView quantityView = view.findViewById(R.id.quantity_text_view);
        ImageView imageView = view.findViewById(R.id.list_item_image);

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_NAME));
        double price = cursor.getDouble(cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_PRICE));
        final int quantityInt = cursor.getInt(cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY));
        String quantity = String.valueOf(quantityInt);
        byte[] imageByte = cursor.getBlob(cursor.getColumnIndex(MerchandiseEntry.COLUMN_MERCHANDISE_IMAGE));

        // format double
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        String priceString = String.valueOf(currencyFormat.format(price));

        // convert btye[] to image
        Bitmap image = getImage(imageByte);

        // Populate fields
        nameView.setText(name);
        priceView.setText(priceString);
        quantityView.setText(quantity);
        imageView.setImageBitmap(image);

        // Find the decrease Quantity button
        Button decrementButton = view.findViewById(R.id.quantity_decrease_button);

        String currentId = cursor.getString(cursor.getColumnIndex(MerchandiseEntry._ID));
        final Uri currentUri = ContentUris.withAppendedId(MerchandiseEntry.CONTENT_URI, Long.parseLong(currentId));
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(MerchandiseEntry.COLUMN_MERCHANDISE_QUANTITY, (quantityInt - 1));
                context.getContentResolver().update(currentUri, values, null, null);
            }
        });
    }
}