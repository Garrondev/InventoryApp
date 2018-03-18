package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gcdev on 3/7/2018.
 */

public class MerchandiseContract {
    // Class should not be instantiated.
    private MerchandiseContract() {}

    /**
     *  The "Content authority" is a name for the entire content provider, similar to the
     *  relationship between a domain name and its website. A convenient string to use for
     *  the content authority is the package name for the app, which is guaranteed to the
     *  unique on the device
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible parth (appended to base content URI for possible URI's)
     */
    public static final String PATH_MERCHANDISE = "merchandise";

    /**
     *  Inner class that defines constant values for the merchandise database table.
     *  Each entry in the table represents a single merchandise.
     */
    public static final class MerchandiseEntry implements BaseColumns {
        /**
         * name of the database table for merchandise
         */
        public final static String TABLE_NAME = "merchandise";

        /**
         * The content URI to access the merchandise data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MERCHANDISE);

        /**
         *  The MIME type of the {@Link #CONTENT_URI} for a list of merchandise.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MERCHANDISE;

        /**
         *  The MIME type of the {@Link #CONTENT_URL} for a single merchandise.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MERCHANDISE;

        /**
         *  Unique ID number for the merchandise (only for use in the database table).
         *  Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the merchandise, Type: TEXT
         */
        public final static String COLUMN_MERCHANDISE_NAME = "name";
        /**
         *  Price of Merchandise, Type: NUMERIC
         */
        public final static String COLUMN_MERCHANDISE_PRICE = "price";
        /**
         * Quantity avaliable for the merchandise, Type: INTEGER
         */
        public final static String COLUMN_MERCHANDISE_QUANTITY = "quantity";
        /**
         *  Email for contacting vendor
         */
        public final static String COLUMN_MERCHANDISE_VENDOR = "vendor";
        /**
         *  Image for Item
         */
        public final static String COLUMN_MERCHANDISE_IMAGE = "image";
    }
}
