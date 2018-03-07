package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Pets app. Manages database creation and version management.
 */
public class PetDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    public static final String DATABASE_NAME = "shelter.db";

    /** Database version. If you change the database schema, you must increment the database version */
    public static final int DATABASE_VERSION = 1;

    /** String Constants - typical components for constructing the arguments */
    public static final String ARGUMENTS_OPENING_PARENTHESES = " (";
    public static final String ARGUMENTS_CLOSING_PARENTHESES = ");";
    public static final String ARGUMENTS_COMMA_SEPARATOR = ", ";

    /** String Constants - database data types */
    public static final String DATATYPE_INTEGER = " INTEGER";
    public static final String DATATYPE_TEXT = " TEXT";

    /** String Constants - table construction keywords */
    public static final String KEYWORD_PRIMARY_KEY = " PRIMARY KEY";
    public static final String KEYWORD_AUTOINCREMENT = " AUTOINCREMENT";
    public static final String KEYWORD_NOT_NULL = " NOT NULL";
    public static final String KEYWORD_DEFAULT = " DEFAULT ";

    /**
     * Constructs a new instance of {@link PetDbHelper}.
     *
     * @param context of the app
     */
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetContract.PetEntry.TABLE_NAME + ARGUMENTS_OPENING_PARENTHESES +
                PetContract.PetEntry._ID + DATATYPE_INTEGER + KEYWORD_PRIMARY_KEY + KEYWORD_AUTOINCREMENT + ARGUMENTS_COMMA_SEPARATOR +
                PetContract.PetEntry.COLUMN_PET_NAME + DATATYPE_TEXT + KEYWORD_NOT_NULL + ARGUMENTS_COMMA_SEPARATOR +
                PetContract.PetEntry.COLUMN_PET_BREED + DATATYPE_TEXT + ARGUMENTS_COMMA_SEPARATOR +
                PetContract.PetEntry.COLUMN_PET_GENDER + DATATYPE_INTEGER + KEYWORD_NOT_NULL + KEYWORD_DEFAULT + Integer.toString(PetContract.PetEntry.GENDER_UNKNOWN) + ARGUMENTS_COMMA_SEPARATOR +
                PetContract.PetEntry.COLUMN_PET_WEIGHT + DATATYPE_INTEGER + KEYWORD_NOT_NULL + KEYWORD_DEFAULT + "0" +
                ARGUMENTS_CLOSING_PARENTHESES;

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}