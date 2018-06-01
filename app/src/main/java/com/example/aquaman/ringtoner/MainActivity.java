package com.example.aquaman.ringtoner;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SQLiteHelper sQLiteHelper = new SQLiteHelper(MainActivity.this);

    private int REQ_CODE_PICK_SOUNDFILE = 42;

    private String TAG = "TAG";

    private ArrayList<String> ringtoneId;

    String getFilePath(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] arr = {MediaStore.Audio.Media.DATA};
            cursor = context.getContentResolver().query(uri, arr, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    private ArrayList<String> getAllRingtones(){
        ArrayList<String> ringtones = new ArrayList<String>();
        ringtoneId = new ArrayList<String>();
        ArrayList<RingtoneModel> records = sQLiteHelper.getAllRecords();
        for(int i=0; i<records.size(); i++){
            ringtones.add(records.get(i).getPath());
            ringtoneId.add(records.get(i).getId());
        }
        return ringtones;
    }

    private Uri getNewUri(){
        RingtoneModel ringtone = sQLiteHelper.getRandomRingtone();
        while (matchRingtone(ringtone.getPath())) {
            ringtone = sQLiteHelper.getRandomRingtone();
        }
        saveRingtone("RINGTONE", ringtone.getPath());
        //Log.i(TAG, "New ringtone path " + ringtone.getPath());
        Toast.makeText(getApplicationContext(), "New ringtone path " + ringtone.getPath(), Toast.LENGTH_SHORT).show();
        File file = new File(ringtone.getPath());
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, "My song");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.ARTIST, "artist");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
        final Uri mUri = getContentResolver().insert(uri, values);
        return mUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() != null){
                try {
                    String audioFile = getFilePath(getApplicationContext(), data.getData());
                    RingtoneModel ringtone = new RingtoneModel();
                    ringtone.setPath(audioFile);
                    sQLiteHelper.insertRecord(ringtone);
                    refreshListView();
                }
                catch (NullPointerException e) {
                    errorDialog();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void selectRingtone(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQ_CODE_PICK_SOUNDFILE);
    }

    private void errorDialog(){
        new AlertDialog.Builder(this).setTitle("Error").setMessage("Please choose another explorer.").show();
    }

    private void confirmDeleteItem(final int index){
        new AlertDialog.Builder(this).setTitle("Really?").setMessage("Do you want to delete").setPositiveButton(R.string.positive_dialog_option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sQLiteHelper.deleteRecord(ringtoneId.get(index));
                refreshListView();
            }
        }).setNegativeButton(R.string.negative_dialog_option, null).show();
    }

    private void refreshListView(){
        final ListView ringtonesList = (ListView) findViewById(R.id.ringtonesList);
        ArrayList<String> ringtones = getAllRingtones();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, ringtones);
        ringtonesList.setAdapter(adapter);
        ringtonesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmDeleteItem(i);
            }
        });
    }

    private boolean matchRingtone(String ringtone) {
        //Log.d(TAG, "current " + getRingtone() + " new " + ringtone);
        return (getRingtone().equals(ringtone));
    }

    private void saveRingtone(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Log.d(TAG, "save ringtone " + value);
        editor.putString(key, value);
        editor.commit();
    }

    private String getRingtone() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String ringtone = sharedPreferences.getString("RINGTONE", "");
        //Log.d(TAG, "get ringtone " + ringtone);
        return ringtone;
    }

    private void setNewRingtone() {
        Uri mUri = getNewUri();
        RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_RINGTONE, mUri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button addButton = (Button) findViewById(R.id.addRingtone);
        refreshListView();

        addButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                selectRingtone();
            }
        });

        class MyCallListener extends PhoneStateListener {

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        setNewRingtone();
                        break;
                    default:
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        }
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(new MyCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }
}
