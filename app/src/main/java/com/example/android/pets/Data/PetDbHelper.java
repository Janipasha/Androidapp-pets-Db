package com.example.android.pets.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.R.attr.version;
import static android.provider.Contacts.SettingsColumns.KEY;
import static android.transition.Fade.IN;


public class PetDbHelper extends SQLiteOpenHelper  {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Pets";
        public static final String CREATE_SQL = "CREATE TABLE "+DATABASE_NAME  +"("+
                PetsContract.PetsEntry.COLUMN_PETS_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                PetsContract.PetsEntry.COLUMN_PET_BREED+ " TEXT,"
                +PetsContract.PetsEntry.COLUMN_PET_WEIGHT +" INTEGER NOT NULL DEFAULT 0,"
                +PetsContract.PetsEntry.COLUMN_PET_GENDER+" INTEGER NOT NULL,"+
                PetsContract.PetsEntry.COLUMN_PET_NAME+" TEXT NOT NULL);";

        public static final String DELETE_DB = "DROP TABLE IF EXIST"+DATABASE_NAME;

        public PetDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL(DELETE_DB);
            onCreate(db);

        }


       /*

        @Override
        public void onCreate(PetDbHelper db) {


        }

        @Override
        public void onUpgrade(PetDbHelper db, int i, int i1) {
            db.execSQL(DELETE_DB);
            onCreate(db);


        }*/
    }

