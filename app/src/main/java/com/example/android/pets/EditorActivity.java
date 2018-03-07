/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Log tag
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Constant for Edit Pet CursorLoader
     */
    private static final int EDIT_PET_LOADER = 0;

    /**
     * URI for specific pet entry, IF editing existing pet
     */
    private Uri mPassedUri;

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private boolean mPetHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get URI passed with calling Intent. (URI could be null).
        mPassedUri = getIntent().getData();

        // If URI is not null, then EditorActivity has been initiated by selection of a pet in the CatalogActivity
        if (mPassedUri != null) {
            // Change Activity title to indicate that user is editing details of an existing pet
            // rather than adding a new pet.
            setTitle(R.string.editor_activity_title_edit_pet);

            Log.v(LOG_TAG, "Value of URI passed with intent is: " + mPassedUri.toString());

            // Initialize/reuse CursorLoader to retrieve current data for existing pet to be edited
            getLoaderManager().initLoader(EDIT_PET_LOADER, null, this);
        } else {
            setTitle(R.string.editor_activity_title_new_pet);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        // Set a listener on each view, to detect any user changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     * Get user input from editor and save new pet into database
     */
    private void savePet() {
        Log.v(LOG_TAG,"In savePet method; about to check for empty data.");

        // Get user input
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        // Check that weight field is not empty first
        int weight;
        if (!TextUtils.isEmpty(mWeightEditText.getText())) {
            weight = Integer.parseInt(mWeightEditText.getText().toString());
        } else {
            weight = 0;
        }
        int gender = mGenderSpinner.getSelectedItemPosition();

        // Check that user hasn't accidentally tried to save the default/empty condition.
        // If they have, display error toast and return early without performing database operation.
        if (mGenderSpinner.getSelectedItemPosition() == PetEntry.GENDER_UNKNOWN) {
            Log.v(LOG_TAG,"In savePet method; gender is unknown.");
            Log.v(LOG_TAG,"Checking name: " + name);
            Log.v(LOG_TAG,"Checking breed: " + breed);
            Log.v(LOG_TAG,"Checking weight: " + Integer.toString(weight));
            if (TextUtils.isEmpty(name) && TextUtils.isEmpty(breed)
                    && weight == 0) {
                Log.v(LOG_TAG,"In savePet method; name, breed, & weight fields are empty.");
                Toast.makeText(this, R.string.message_error_no_pet_data_to_save,Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create new ContentValues object and associate user input with relevant database table columns
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        // Check whether a weight has been logged
        if (weight != -1) {
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
        }
        values.put(PetEntry.COLUMN_PET_GENDER, gender);

        // Check whether we are saving a new pet or edited data for an existing pet.
        // Insert a new entry or update an existing entry, accordingly.
        if (mPassedUri == null) { // Inserting a new pet entry
            // Call ContentResolver (and thus Content Provider) to insert entry into the database.
            Uri newRowId = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            // Show a toast confirming whether or not pet data was successfully added to the database
            if (newRowId != null) {
                Toast.makeText(this, R.string.message_pet_added, Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, R.string.message_error_failed_to_add_pet, Toast.LENGTH_SHORT);
            }
        } else { // Updating an existing pet entry at the URI passed into the EditorActivity
            int numRowsUpdated = getContentResolver().update(mPassedUri, values, null, null);

            // Show a toast confirming whether or not pet data was successfully edited
            if (numRowsUpdated >= 1) {
                Toast.makeText(this, R.string.message_pet_edited, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.message_error_failed_to_edit_pet, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Check whether EditorActivity is displaying an existing pet that can be deleted
        if (mPassedUri != null) {
            Log.v(LOG_TAG,"In EditorActivity deletePet method; calling delete with URI of: " + mPassedUri.toString());
            // Get ContentProvider and call delete method
            int numRowsDeleted = getContentResolver().delete(mPassedUri,null,null);

            // Display toast confirming whether or not deletion was successful
            if (numRowsDeleted >0) {
                Toast.makeText(this,R.string.message_pet_deleted,Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this,R.string.message_error_failed_to_delete_pet,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mPassedUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to the database
                savePet();
                // Exit the activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Call delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
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
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, mPassedUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Move cursor to first position, if result isn't empty
        if (!(cursor.getCount() > 0)) {
            Log.e(LOG_TAG, "Failed to return cursor with data.");
            return;
        }

        // Move cursor to first column
        cursor.moveToFirst();

        // Retrieve values from the selected pet's database entry
        String cursorName = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME));
        String cursorBreed = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
        int cursorGender = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
        String cursorWeight = Integer.toString(
                cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)));

        // Set the retrieved values on the UI fields as a starting point for editing the pet
        mNameEditText.setText(cursorName);
        mBreedEditText.setText(cursorBreed);
        mGenderSpinner.setSelection(cursorGender);
        mWeightEditText.setText(cursorWeight);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
        mWeightEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}