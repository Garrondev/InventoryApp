package com.example.android.inventoryapp.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Garron on 3/18/2018.
 */

// Source for idea: https://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database
public class DbBitmapUtility {

        // convert from bitmap to byte array
        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }

}
