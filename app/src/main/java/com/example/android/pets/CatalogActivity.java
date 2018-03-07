package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    // Tag for log statements
    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    // Identifier for pets cursorloader
    private static final int PET_LOADER = 0;

    // Global reference to list view
    ListView mPetListView;

    // Global reference to instance of PetCursorAdapter
    PetCursorAdapter mCursorAdapter;

    // Reference to instance of database operations helper class
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the ListView which will be populated with the pet data
        mPetListView = (ListView) findViewById(R.id.list_view_pet);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        mPetListView.setEmptyView(emptyView);

        // Create instance of cursor adapter
        mCursorAdapter = new PetCursorAdapter(this, null);

        // Set adapter on the list view
        mPetListView.setAdapter(mCursorAdapter);

        // Create a listener for when an item in the list view is selected
        mPetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               //Create an intent
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);

                // Construct a URI for a single database row, using the clicked item's ID
                Uri uri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);

                // Attach the URI to the intent
                intent.setData(uri);

                // Start the new activity intent
                startActivity(intent);
            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PET_LOADER, null, this);

        // Create instance of our database helper class
        mDbHelper = new PetDbHelper(this);

    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertDummyData() {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Dummy");
        values.put(PetEntry.COLUMN_PET_BREED, "Makebelievien Poodle");
        values.put(PetEntry.COLUMN_PET_GENDER, 1);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 200);

        // Insert a new row of dummy data into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to insert into the pets database table.
        // Receive the new content URI that will allow us to access this data in the future.
        Uri newRowId = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        // Show a toast confirming whether addition of dummy data was successful
        if (newRowId != null) {
            Toast.makeText(this, R.string.message_pet_added, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.message_error_failed_to_add_pet, Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // TODO: ADD ALERTDIALOG FOR USER TO CONFIRM THE DELETION OF ALL PETS
                getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Projection with table columns to return
        String[] projection = new String[] {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        return new CursorLoader(this,
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nullify the old cursor to prevent memory leaks
        mCursorAdapter.swapCursor(null);
    }
}