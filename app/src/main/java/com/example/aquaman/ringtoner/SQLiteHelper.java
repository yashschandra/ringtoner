package com.example.aquaman.ringtoner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by aquaman on 9/3/18.
 */

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_NAME = "RINGTONE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PATH = "PATH";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PATH + " VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME + ";");
        onCreate(db);
    }

    private SQLiteDatabase database;

    public void insertRecord(RingtoneModel ringtoneModel) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PATH, ringtoneModel.getPath());
        database.insert(TABLE_NAME, null, contentValues);
        database.close();
    }

    public void deleteRecord(String id) {
        database = this.getReadableDatabase();
        database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{id});
    }

    public ArrayList<RingtoneModel> getAllRecords() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        ArrayList<RingtoneModel> ringtones = new ArrayList<RingtoneModel>();
        for(int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            RingtoneModel ringtoneModel = new RingtoneModel();
            ringtoneModel.setId(cursor.getString(0));
            ringtoneModel.setPath(cursor.getString(1));
            ringtones.add(ringtoneModel);
        }
        cursor.close();
        database.close();
        return ringtones;
    }

    public RingtoneModel getRandomRingtone() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        RingtoneModel ringtoneModel = new RingtoneModel();
        ringtoneModel.setPath("/sdcard/Music/english/2/poets of the fall/" + "temple-of-thought.mp3");
        if(cursor.getCount()>0){
            int randomNum = ThreadLocalRandom.current().nextInt(1,cursor.getCount() + 1);
            for(int i=1; i<=cursor.getCount(); i++){
                cursor.moveToNext();
                if(i == randomNum) {
                    ringtoneModel.setId(cursor.getString(0));
                    ringtoneModel.setPath(cursor.getString(1));
                    break;
                }
            }
        }
        cursor.close();
        database.close();
        return ringtoneModel;
    }
}
