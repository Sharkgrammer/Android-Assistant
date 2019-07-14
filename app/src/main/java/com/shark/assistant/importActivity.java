package com.shark.assistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class importActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
    }

    public void importData(View v){
        //import a file and update blacklist/people with it
    }

    public void exportData(View v){
        //create a file to be exported
    }

    public void clearData(View v){
        //reset the databases

    }

    public void backData(View v){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(importActivity.this, mainActivity.class));
    }
}
