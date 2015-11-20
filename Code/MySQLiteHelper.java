package com.programasterapps.poketacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by Rob on 7/17/2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    //Database Names
    static final String DATABASE_NAME = "PokeTacts";

    //Table Names
    static final String CONTACT_TABLE = "Contact_Table";

    //Field Names
    static final String CONTACTID     = "ContactID";
    static final String TYPE_1        = "Type_1";
    static final String TYPE_2        = "Type_2";
    static final String DESCRIPTION   = "Description";
    static final String HEIGHT_FEET   = "Height_Feet";
    static final String HEIGHT_INCHES = "Height_Inches";
    static final String WEIGHT        = "Weight";

    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String my_query = "CREATE TABLE IF NOT EXISTS " + 
				CONTACT_TABLE + " (" +
                CONTACTID     + " VARCHAR PRIMARY KEY NOT NULL UNIQUE, " +
                TYPE_1        + " INTEGER NOT NULL DEFAULT 0, " +
                TYPE_2        + " INTEGER NOT NULL DEFAULT 0, " +
                DESCRIPTION   + " TEXT, "                       +
                HEIGHT_FEET   + " INTEGER, "                    +
                HEIGHT_INCHES + " INTEGER, "                    +
                WEIGHT        + " INTEGER);";

        db.execSQL(my_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insert_contact(String ID, int type1, int type2, String descript, int h_ft, int h_in, int weight)
    {
        SQLiteDatabase myData = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long row;

        if(myData.isOpen())
        {
			values.put(CONTACTID, ID);
			values.put(TYPE_1, type1);
			values.put(TYPE_2, type2);
			values.put(DESCRIPTION, descript);
			values.put(HEIGHT_FEET, h_ft);
			values.put(HEIGHT_INCHES, h_in);
			values.put(WEIGHT, weight);
			
            myData.beginTransaction();
                row = myData.insertOrThrow(CONTACT_TABLE, null, values);
				myData.setTransactionSuccessful();
            myData.endTransaction();
			
            myData.close();
        }
        else
        {
            return false;
        }
        return true;
    }

    public Cursor runQuery(int queryID, String ID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor myCursor = null;
        switch (queryID)
        {
            case 0:
                myCursor = db.rawQuery("SELECT * FROM " + CONTACT_TABLE, null);
                break;
            case 1:
                myCursor = db.rawQuery("SELECT * FROM " + CONTACT_TABLE +
                        " WHERE " + CONTACTID + " = " + ID + ";", null);
                break;

        }

        return myCursor;
    }

}
