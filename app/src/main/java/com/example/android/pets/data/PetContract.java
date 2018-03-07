package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 */
public final class PetContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PetContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     *
     * Constant for content authority portion of content provider URI
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     *
     * Uri object initialized with scheme + content authority portion of content provider URI
     */
    public static final Uri BASE_CONTENT_URI= Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     *
     * Constant for the data portion of content provider URI, for the Pets database table
     * */
    public static final String PATH_PETS = PetEntry.TABLE_NAME;

    public static class PetEntry implements BaseColumns {

        /** Constant, table name */
        public final static String TABLE_NAME = "pets";

        /** The content URI to access the pet data in the provider */
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        // Constant, title for _id column
        public static final String _ID = BaseColumns._ID;

        // Constant, title for pet name column
        public static final String COLUMN_PET_NAME = "name";

        // Constant, title for pet breed column
        public static final String COLUMN_PET_BREED = "breed";

        // Constant, title for pet gender column
        public static final String COLUMN_PET_GENDER = "gender";

        // Constant, title for pet weight column
        public static final String COLUMN_PET_WEIGHT = "weight";

        // Constant, for undefined gender
        public static final int GENDER_UNKNOWN = 0;
        // Constant, for the male gender
        public static final int GENDER_MALE = 1;
        // Constant, for the female gender
        public static final int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }

    }

}
