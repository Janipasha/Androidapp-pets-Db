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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.Data.PetDbHelper;
import com.example.android.pets.Data.PetsContract;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.os.Build.VERSION_CODES.M;
import static com.example.android.pets.Data.PetsContract.PetsEntry.COLUMN_PET_BREED;
import static com.example.android.pets.Data.PetsContract.PetsEntry.COLUMN_PET_GENDER;
import static com.example.android.pets.Data.PetsContract.PetsEntry.COLUMN_PET_NAME;
import static com.example.android.pets.Data.PetsContract.PetsEntry.COLUMN_PET_WEIGHT;
import static com.example.android.pets.Data.PetsContract.PetsEntry.GENDER_FEMALE;
import static com.example.android.pets.Data.PetsContract.PetsEntry.GENDER_MALE;
import static com.example.android.pets.Data.PetsContract.PetsEntry.GENDER_UNKNOWN;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    private Uri mCurrentPetUri;
    private static final int EXISTING_PET_LOADER = 0;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    Cursor nCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        if(mCurrentPetUri== null){
           setTitle("Add a new pet ");

            invalidateOptionsMenu();
        }else {
            setTitle("Edit pet");

            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();


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
                        mGender = PetsContract.PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsContract.PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsContract.PetsEntry.GENDER_UNKNOWN; // Unknown
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



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void insertPets(){
        String mName = mNameEditText.getText().toString().trim();
        String mBreed = mBreedEditText.getText().toString().trim();
        String mWeight = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(mWeight);

        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(mName) && TextUtils.isEmpty(mBreed) &&
                TextUtils.isEmpty(mWeight) && mGender == PetsContract.PetsEntry.GENDER_UNKNOWN)
        {return;}

        ContentValues values1 = new ContentValues();
        values1.put(COLUMN_PET_NAME,mName);
        values1.put(COLUMN_PET_BREED,mBreed);
        values1.put(COLUMN_PET_GENDER,mGender);


        values1.put(COLUMN_PET_WEIGHT,weight);

        Uri mUri = getContentResolver().insert(Uri.parse("content://"+PetsContract.CONTENT_AUTHORITY+"/"+ PetsContract.PATH_PETS),values1);

        if(ContentUris.parseId(mUri) == -1){
            Toast.makeText(this, "Error with saving pet", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Saved with new Row ID ", Toast.LENGTH_SHORT).show();
        }

    }

    private void savePets(){
        String mName = mNameEditText.getText().toString().trim();
        String mBreed = mBreedEditText.getText().toString().trim();
        String mWeight = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(mWeight);

        ContentValues values1 = new ContentValues();
        values1.put(COLUMN_PET_NAME,mName);
        values1.put(COLUMN_PET_BREED,mBreed);
        values1.put(COLUMN_PET_GENDER,mGender);
        values1.put(COLUMN_PET_WEIGHT,weight);


        int updatedRowId = 0;


        updatedRowId = getContentResolver().update(mCurrentPetUri,
                values1,
                null,
                null);

        if(updatedRowId==0){
            Toast.makeText(this, "Error with saving pet", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "updated Sucessfully ", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(mCurrentPetUri== null){
                   insertPets();
                }else {
                    savePets();
                }
                finish();
               // Toast.makeText(this, "Pet Saved with ID 1", Toast.LENGTH_SHORT).show();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        // TODO: Implement this method

        if (mCurrentPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetsContract.PetsEntry._ID,
                PetsContract.PetsEntry.COLUMN_PET_NAME,
                PetsContract.PetsEntry.COLUMN_PET_BREED,
                PetsContract.PetsEntry.COLUMN_PET_GENDER,
                PetsContract.PetsEntry.COLUMN_PET_WEIGHT };

        return new CursorLoader(this ,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;}

    if (cursor.moveToFirst()) {

    int nameColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_NAME);
    int summaryColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_BREED);
    int weightColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);
    int genderColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_GENDER);

    String petName = cursor.getString(nameColumnIndex);
    String petSummary = cursor.getString(summaryColumnIndex);
    int petWeight = cursor.getInt(weightColumnIndex);
    int petGender = cursor.getInt(genderColumnIndex);

    mNameEditText.setText(petName);
    mBreedEditText.setText(petSummary);
    mWeightEditText.setText(Integer.toString(petWeight));

    switch (petGender) {
        case PetsContract.PetsEntry.GENDER_MALE:
            mGenderSpinner.setSelection(1);
            break;
        case PetsContract.PetsEntry.GENDER_FEMALE:
            mGenderSpinner.setSelection(2);
            break;
        default:
            mGenderSpinner.setSelection(0);
            break;
    }

}


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}