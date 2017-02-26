package com.example.android.pets.Data;


        import android.content.ContentProvider;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.UriMatcher;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.net.Uri;
        import android.widget.Toast;

        import com.example.android.pets.CatalogActivity;

        import static android.R.attr.id;
        import static android.content.ContentUris.withAppendedId;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS+"/#",PET_ID);
    }



    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper ntHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        ntHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = ntHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);

        switch(match){
            case PETS:

               // selection = null;
                //selectionArgs = null;
                cursor = database.query(PetsContract.PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

                break;
            case PET_ID:
                selection = PetsContract.PetsEntry._ID +"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetsContract.PetsEntry.TABLE_NAME,projection,selection ,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query Unknown URI ");}

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;



        }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not allowed for"+uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values){

        String name = values.getAsString(PetsContract.PetsEntry.COLUMN_PET_NAME);
        Integer gender = values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_GENDER);
        Integer weight = values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);

        if(gender==null || !PetsContract.PetsEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Gender is invalid");
        }

        if(name== null){
            throw new IllegalArgumentException("Name field can't be null");
        }

        if(weight!= null&& weight <0 ){
            throw new IllegalArgumentException("Weight should be a positive number");
        }


        SQLiteDatabase db = ntHelper.getReadableDatabase();
        long RowId = db.insert(PetsContract.PetsEntry.TABLE_NAME,null,values);

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, RowId);

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetsContract.PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsContract.PetsEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetsContract.PetsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = ntHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement

              int rowUpdated =  database.update(PetsContract.PetsEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowUpdated !=0){
            getContext().getContentResolver().notifyChange(uri,null);

        }

        return rowUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = ntHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                int rowsDeleted = database.delete(PetsContract.PetsEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsContract.PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(PetsContract.PetsEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsContract.PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsContract.PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }}

