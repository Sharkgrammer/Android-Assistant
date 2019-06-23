package com.shark.assistant;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private EditText txtInput, txtOutput;
    private TextView lblExplain;
    private Button btnAdd, btnDelete, btnEdit, btnNext, btnBack, btnFirst, btnLast, btnPerson, btnApp;
    private boolean isPerson = true;
    private holder data;
    private int mode = 0, index, maxApp, maxPerson;
    private List<app> appList;
    private List<person> personList;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            Log.wtf("Package",pack);
            Log.wtf("Title",title);
            Log.wtf("Text",text);


            if (pack == null || title == null || text == null){
                return;
            }
            //check packages here

            if (pack.contains("youtube") || title.contains("Messenger is displaying over other apps")){
                return;
            }

            pack = data.getAppName(pack);
            title = data.getPersonName(title);

            String dataStr = pack + " . " + title + " . " + text;

            Log.i("TTS", dataStr);
            int speechStatus = textToSpeech.speak(dataStr, TextToSpeech.QUEUE_FLUSH, null);
            if (speechStatus == TextToSpeech.ERROR) {
                Log.e("TTS", "Error in converting Text to Speech!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.UK);
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        data = new holder(getApplicationContext());
        txtInput = findViewById(R.id.txtInput);
        txtOutput = findViewById(R.id.txtOutput);
        lblExplain = findViewById(R.id.lblExplain);

        btnAdd = findViewById(R.id.btnNew);
        btnDelete = findViewById(R.id.btnDel);
        btnEdit = findViewById(R.id.btnEdit);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnFirst = findViewById(R.id.btnFirst);
        btnLast = findViewById(R.id.btnLast);
        btnPerson = findViewById(R.id.btnPerson);
        btnApp = findViewById(R.id.btnApp);

        refresh();
        btnPersonClick(null);
        if (mode == 0) btnFirstClick(null);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    public void btnNewClick(View v) {
        if (mode == 0){
            mode = 1;
            btnDelete.setText(R.string.exit);
            btnAdd.setText(R.string.save);
            layoutMod();
        }else{

            if (txtInput.getText().length() < 1 || txtOutput.getText().length() < 1){
                Toast.makeText(getApplicationContext(), "Textboxes cannot be blank", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode != 2){
                if (isPerson){
                    data.newPerson(txtInput.getText().toString(), txtOutput.getText().toString());
                }else{
                    data.newApp(txtInput.getText().toString(), txtOutput.getText().toString());
                }
            }else{
                if (isPerson){
                    person p = personList.get(index);

                    p.setInput(txtInput.getText().toString());
                    p.setOutput(txtOutput.getText().toString());

                    data.savePerson(p);
                }else{
                    app a = appList.get(index);

                    a.setInput(txtInput.getText().toString());
                    a.setOutput(txtOutput.getText().toString());

                    data.saveApp(a);
                }
            }

            mode = 0;

            btnDelete.setText(R.string.delete);
            btnAdd.setText(R.string.add);

            refresh();
            layoutMod();
            populate();
        }
    }

    public void btnPersonClick(View v) {
        isPerson = true;

        mode = 0;
        if ((maxPerson + 1) == 0){
            btnNewClick(null);
            return;
        }

        txtInput.setHint(R.string.peopleInput);
        txtOutput.setHint(R.string.peopleOutput);
        lblExplain.setText(R.string.peopleExplain);
        index = 0;
        populate();
    }

    public void btnAppClick(View v) {
        isPerson = false;

        mode = 0;
        if ((maxApp + 1) == 0){
            btnNewClick(null);
            return;
        }

        txtInput.setHint(R.string.appInput);
        txtOutput.setHint(R.string.appOutput);
        lblExplain.setText(R.string.appExplain);
        index = 0;
        populate();
    }

    public void btnDeleteClick(View v) {
        if (mode != 0){
            mode = 0;
            btnDelete.setText(R.string.delete);
            btnAdd.setText(R.string.add);
            layoutMod();

            populate();
        }else{
            AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setMessage("Are you sure you want to delete " + txtInput.getText() + "?");
            deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                   if (isPerson){
                       data.deletePerson(personList.get(index).getId());
                   }else{
                       data.deleteApp(appList.get(index).getId());
                   }

                   refresh();
                   populate();
                }
            });
            deleteAlert.setNegativeButton("No", null);
            deleteAlert.setCancelable(true);
            deleteAlert.show();
        }
    }

    public void btnEditClick(View v) {
        mode = 2;
        btnDelete.setText(R.string.exit);
        btnAdd.setText(R.string.save);
        layoutMod();
    }

    public void btnNextClick(View v) {
        if (isPerson){
            if (index != maxPerson){
                index++;
                populate();
            }
        }else{
            if (index != maxApp){
                index++;
                populate();
            }
        }
    }

    public void btnBackClick(View v) {
        if (isPerson){
            if (index != 0){
                index--;
                populate();
            }
        }else{
            if (index != 0){
                index--;
                populate();
            }
        }
    }

    public void btnFirstClick(View v) {
        index = 0;
        populate();
    }

    public void btnLastClick(View v) {
        if (isPerson){
            index = maxPerson;
            populate();
        }else{
            index = maxApp;
            populate();
        }
    }

    private void populate(){
        try{
            if (isPerson){
                person p = personList.get(index);
                txtInput.setText(p.getInput());
                txtOutput.setText(p.getOutput());
            }else{
                app a = appList.get(index);
                txtInput.setText(a.getInput());
                txtOutput.setText(a.getOutput());
            }
        }
        catch(Exception ex){
            Log.wtf("Error", ex.toString());

            mode = 1;
            layoutMod();
        }
    }

    private void refresh(){
        appList = data.getAppList();
        personList = data.getPersonList();

        index = 0;
        maxApp = appList.size() - 1;
        maxPerson = personList.size() - 1;
    }

    //0 = normal running, 1 is add, 2 is edit
    private void layoutMod(){

        switch(mode){
            case 0:
                txtInput.setEnabled(false);
                txtOutput.setEnabled(false);

                btnAdd.setEnabled(true);
                btnDelete.setEnabled(true);
                btnEdit.setEnabled(true);
                btnNext.setEnabled(true);
                btnBack.setEnabled(true);
                btnFirst.setEnabled(true);
                btnLast.setEnabled(true);
                btnPerson.setEnabled(true);
                btnApp.setEnabled(true);

                txtInput.getText().clear();
                txtOutput.getText().clear();
                break;
            case 1:
                txtInput.setEnabled(true);
                txtOutput.setEnabled(true);

                btnAdd.setEnabled(true);
                btnDelete.setEnabled(true);
                btnEdit.setEnabled(false);
                btnNext.setEnabled(false);
                btnBack.setEnabled(false);
                btnFirst.setEnabled(false);
                btnLast.setEnabled(false);
                btnPerson.setEnabled(false);
                btnApp.setEnabled(false);

                txtInput.getText().clear();
                txtOutput.getText().clear();
                break;
            case 2:
                txtInput.setEnabled(true);
                txtOutput.setEnabled(true);

                btnAdd.setEnabled(true);
                btnDelete.setEnabled(true);
                btnEdit.setEnabled(false);
                btnNext.setEnabled(false);
                btnBack.setEnabled(false);
                btnFirst.setEnabled(false);
                btnLast.setEnabled(false);
                btnPerson.setEnabled(false);
                btnApp.setEnabled(false);
                break;
        }
    }

}
