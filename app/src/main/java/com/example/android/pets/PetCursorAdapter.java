package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.pets.Data.PetsContract;

import static android.R.attr.name;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Jani on 20-02-2017.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c)
    {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO: Fill out this method and return the list item view (instead of null)

        return LayoutInflater.from(context).inflate(R.layout.list_item,parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO: Fill out this method

        TextView nameTextView = (TextView)view.findViewById(R.id.name);
        TextView summaryTextView = (TextView)view.findViewById(R.id.summary);

        int nameColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_NAME);
        int summaryColumnIndex = cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_BREED);

        String petName = cursor.getString(nameColumnIndex);
        String petSummary = cursor.getString(summaryColumnIndex);

        if (TextUtils.isEmpty(petSummary)) {
            petSummary = context.getString(R.string.unknown_breed);
        }

        nameTextView.setText(petName);
        summaryTextView.setText(petSummary);



    }
}