package com.shark.assistant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class importActivity extends AppCompatActivity {

    private holder data;
    private boolean perms_read = true, perms_write = true;
    private static final int FILE_SELECT_CODE = 300;
    private final int WRITE_ACCESS = 0, READ_ACCESS = 1;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        data = new holder(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.settings);
    }

    public void importData(View v){
        //import a file and update blacklist/people with it
        //get file filename/path
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream is = null;
        String file = "";
        try {
            is = getContentResolver().openInputStream(data.getData());

            Scanner s = new Scanner(is).useDelimiter("\\A");
            file = s.hasNext() ? s.next() : "";

            is.close();

            JSONObject obj = new JSONObject(file);

            JSONArray listArray = obj.getJSONArray("lists");
            JSONArray appArray = listArray.getJSONObject(0).getJSONArray("app");
            JSONArray peopleArray = listArray.getJSONObject(1).getJSONArray("person");
            JSONArray blacklistArray = listArray.getJSONObject(2).getJSONArray("blacklist");

            //update database
            for (int x = 0; x < appArray.length(); x++){
                this.data.newApp(appArray.getJSONArray(x).getJSONObject(0).getString("input"), appArray.getJSONArray(x).getJSONObject(1).getString("output"));
            }

            for (int x = 0; x < peopleArray.length(); x++){
                this.data.newPerson(peopleArray.getJSONArray(x).getJSONObject(0).getString("input"), peopleArray.getJSONArray(x).getJSONObject(1).getString("output"));
            }

            for (int x = 0; x < blacklistArray.length(); x++){
                this.data.newBlacklist(blacklistArray.getJSONArray(x).getJSONObject(0).getString("input"), blacklistArray.getJSONArray(x).getJSONObject(1).getInt("type"));
            }

            //update user
            Toast.makeText(getApplicationContext(), "Import Complete", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e) {
           Log.wtf("Error in JSON read", e.toString());
           Toast.makeText(getApplicationContext(), "Import Failed", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exportData(View v){
        //create a file to be exported

        JsonWriter jsonWriter = null;
        String filename = "", result = "complete";

        try{

            //Ask for permissions

            if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_ACCESS);
            }

            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_ACCESS);

            }

            if (!perms_read || !perms_write){
                Toast.makeText(getApplicationContext(), "Please give read/write permissions to export", Toast.LENGTH_SHORT).show();
                return;
            }


            //get users filename/path
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            filename += dir.getAbsolutePath() + "/notifications_export.json";

            //prep objs for write
            appList = data.getAppList();
            personList = data.getPersonList();
            blacklistList = data.getBlacklistList();

            //write file
            jsonWriter = new JsonWriter(new FileWriter(filename));

            jsonWriter.beginObject();
            jsonWriter.name("lists");
            jsonWriter.beginArray();

            jsonWriter.beginObject();
            jsonWriter.name("app");
            jsonWriter.beginArray();

            for (app x : appList){

                jsonWriter.beginArray();
                jsonWriter.beginObject();
                jsonWriter.name("input");
                jsonWriter.value(x.getInput());
                jsonWriter.endObject();

                jsonWriter.beginObject();
                jsonWriter.name("output");
                jsonWriter.value(x.getOutput());
                jsonWriter.endObject();
                jsonWriter.endArray();

            }

            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.beginObject();
            jsonWriter.name("person");
            jsonWriter.beginArray();

            for (person x : personList){

                jsonWriter.beginArray();
                jsonWriter.beginObject();
                jsonWriter.name("input");
                jsonWriter.value(x.getInput());
                jsonWriter.endObject();

                jsonWriter.beginObject();
                jsonWriter.name("output");
                jsonWriter.value(x.getOutput());
                jsonWriter.endObject();
                jsonWriter.endArray();

            }

            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.beginObject();
            jsonWriter.name("blacklist");
            jsonWriter.beginArray();

            for (blacklist x : blacklistList){

                jsonWriter.beginArray();
                jsonWriter.beginObject();
                jsonWriter.name("input");
                jsonWriter.value(x.getInput());
                jsonWriter.endObject();

                jsonWriter.beginObject();
                jsonWriter.name("type");
                jsonWriter.value(x.getType());
                jsonWriter.endObject();
                jsonWriter.endArray();

            }
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.endArray();
            jsonWriter.endObject();

            jsonWriter.close();

        }
        catch(Exception e){
            Log.wtf("JSON Error", e.toString());
            result = "failed";
        }

        //alert user
        Toast.makeText(getApplicationContext(), "Export " + result + " at " + filename, Toast.LENGTH_LONG).show();

    }

    public void clearData(View v){
        //reset the databases

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to clear the app lists?\n\nThis cannot be undone!");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                boolean res = data.restartDatabase();

                Toast.makeText(getApplicationContext(), res ? "Lists Cleared" : "Lists Clear fail", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("No", null);
        alert.setCancelable(true);
        alert.show();


    }

    public void backData(View v){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(importActivity.this, mainActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                perms_write = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                return;
            }

            case READ_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                perms_read = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                return;
            }

        }
    }


}
