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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView lblExplain;
    private Button btnPerson, btnApp, btnBlacklist;
    private holder data;
    private int mode = 0, index, maxApp, maxPerson, maxBlacklist, pi;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;
    private int appScreen = 0;
    private final int PERSON = 0, APP = 1, BLACKLIST = 2;
    private ScrollView sclMain;
    private LinearLayout sclMainLin;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            Log.wtf("Package", pack);
            Log.wtf("Title", title);
            Log.wtf("Text", text);

            String dataStr = pack + " . " + title + " . " + text;
            processor pro = new processor(MainActivity.this);

            dataStr = pro.processText(dataStr);

            if (dataStr == null){
                return;
            }

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

        data = new holder(this);
        data.createTables();
        lblExplain = findViewById(R.id.lblExplain);
        btnPerson = findViewById(R.id.btnPerson);
        btnApp = findViewById(R.id.btnApp);
        btnBlacklist = findViewById(R.id.btnBlacklist);
        sclMain = findViewById(R.id.sclMain);
        sclMainLin = findViewById(R.id.sclMainLin);

        refresh();
        btnPersonClick(null);
        if (mode == 0) btnFirstClick(null);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    public void btnNewClick(View v) {
        System.out.println(appScreen + "  " + mode);

        /*if (mode == 0){
            mode = 1;
            btnDelete.setImageDrawable(getResources().getDrawable(R.drawable.exit));
            btnAdd.setImageDrawable(getResources().getDrawable(R.drawable.save));
            layoutMod();
        }else{

            if (txtInput.getText().length() < 1 || txtOutput.getText().length() < 1){
                Toast.makeText(getApplicationContext(), "Textboxes cannot be blank", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode != 2){

                switch (appScreen){
                    case PERSON:
                        data.newPerson(txtInput.getText().toString(), txtOutput.getText().toString());
                        break;

                    case APP:
                        data.newApp(txtInput.getText().toString(), txtOutput.getText().toString());
                        break;

                    case BLACKLIST:

                        try{
                            int tempint = Integer.valueOf(txtOutput.getText().toString());
                            if (tempint >= 0 && tempint <= 2){
                                data.newBlacklist(txtInput.getText().toString(), tempint);
                            }else{
                                Toast.makeText(getApplicationContext(), "Type must be between 0 and 2", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){
                            Toast.makeText(getApplicationContext(), "Type must be a number", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

            }else{

                switch (appScreen){
                    case PERSON:
                        person p = personList.get(index);

                        p.setInput(txtInput.getText().toString());
                        p.setOutput(txtOutput.getText().toString());

                        data.savePerson(p);
                        break;

                    case APP:
                        app a = appList.get(index);

                        a.setInput(txtInput.getText().toString());
                        a.setOutput(txtOutput.getText().toString());

                        data.saveApp(a);
                        break;

                    case BLACKLIST:
                        blacklist b = blacklistList.get(index);

                        int tempint;
                        b.setInput(txtInput.getText().toString());
                        try{
                            tempint = Integer.valueOf(txtOutput.getText().toString());
                            if (tempint < 0 || tempint > 2){
                                Toast.makeText(getApplicationContext(), "Type must be between 0 and 2", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }catch(Exception e){
                            Toast.makeText(getApplicationContext(), "Type must be a number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        b.setType(tempint);
                        data.saveBlacklist(b);
                        break;
                }

            }

            mode = 0;

            btnDelete.setImageDrawable(getResources().getDrawable(R.drawable.delete));
            btnAdd.setImageDrawable(getResources().getDrawable(R.drawable.add));

            refresh();
            layoutMod();
            populate();
        }*/
    }

    public void btnPersonClick(View v) {
        appScreen = PERSON;

        lblExplain.setText(R.string.peopleExplain);
       // txtInput.setHint(R.string.peopleInput);
        //txtOutput.setHint(R.string.peopleOutput);

        mode = 0;
        if ((maxPerson + 1) == 0){
            btnNewClick(null);
            return;
        }

        index = 0;
        populate();
    }

    public void btnBlacklistClick(View v) {
        appScreen = BLACKLIST;

        lblExplain.setText(R.string.blacklistExplain);
    //  /  //txtInput.setHint(R.string.blacklistInput);
       // txtOutput.setHint(R.string.blacklistOutput);

        mode = 0;
        if ((maxBlacklist + 1) == 0){
            btnNewClick(null);
            return;
        }

        index = 0;
        populate();
    }

    public void btnAppClick(View v) {
        appScreen = APP;

        lblExplain.setText(R.string.appExplain);
       // txtInput.setHint(R.string.appInput);
       // txtOutput.setHint(R.string.appOutput);

        mode = 0;
        if ((maxApp + 1) == 0){
            btnNewClick(null);
            return;
        }

        index = 0;
        populate();
    }

    public void btnDeleteClick(Object objDel) {
        if (mode != 0){
            mode = 0;
           // btnDelete.setImageDrawable(getResources().getDrawable(R.drawable.delete));
           // btnAdd.setImageDrawable(getResources().getDrawable(R.drawable.add));
            layoutMod();
            populate();
        }else{
            final Object obj = objDel;
            String ans = "";
            switch (appScreen) {
                case PERSON:
                    ans = ((person) obj).getInput();
                    break;

                case APP:
                    ans = ((app) obj).getInput();
                    break;

                case BLACKLIST:
                    ans = ((blacklist) obj).getInput();
                    break;
            }

            AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
            deleteAlert.setMessage("Are you sure you want to delete " + ans + "?");
            deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {

                    switch (appScreen) {
                        case PERSON:
                            data.deletePerson(((person) obj).getId());
                            break;

                        case APP:
                            data.deleteApp(((app) obj).getId());
                            break;

                        case BLACKLIST:
                            data.deleteBlacklist(((blacklist) obj).getId());
                            break;
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
       // btnDelete.setImageDrawable(getResources().getDrawable(R.drawable.exit));
       // btnAdd.setImageDrawable(getResources().getDrawable(R.drawable.save));
        layoutMod();
    }

    public void btnNextClick(View v) {

        switch (appScreen){
            case PERSON:
                if (index == maxPerson)return;;
                break;

            case APP:
                if (index == maxApp)return;
                break;

            case BLACKLIST:
                if (index == maxBlacklist) return;
                break;
        }

        index++;
        populate();
    }

    public void btnBackClick(View v) {
        switch (appScreen){
            case PERSON:
                if (index == 0)return;;
                break;

            case APP:
                if (index == 0)return;
                break;

            case BLACKLIST:
                if (index == 0) return;
                break;
        }

        index--;
        populate();
    }

    public void btnFirstClick(View v) {
        index = 0;
        populate();
    }

    public void btnLastClick(View v) {
        switch (appScreen){
            case PERSON:
                index = maxPerson;
                break;

            case APP:
                index = maxApp;
                break;

            case BLACKLIST:
                index = maxBlacklist;
                break;
        }

        populate();
    }

    private void populate(){
        try{
            switch (appScreen){
                case PERSON:
                    person p = personList.get(index);
                   /// txtInput.setText(p.getInput());
                   // txtOutput.setText(p.getOutput());
                    break;

                case APP:
                    app a = appList.get(index);
                   // txtInput.setText(a.getInput());
                   // txtOutput.setText(a.getOutput());
                    break;

                case BLACKLIST:
                    blacklist b = blacklistList.get(index);
                  //  txtInput.setText(b.getInput());
                  //  txtOutput.setText(String.valueOf(b.getType()));
                    break;
            }

            fillScrollLayout();
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
        blacklistList = data.getBlacklistList();

        index = 0;
        maxApp = appList.size() - 1;
        maxPerson = personList.size() - 1;
        maxBlacklist = blacklistList.size() - 1;
    }

    //0 = normal running, 1 is add, 2 is edit
    private void layoutMod(){
/*
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
                btnBlacklist.setEnabled(true);

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
                btnBlacklist.setEnabled(false);

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
                btnBlacklist.setEnabled(false);
                break;
        }*/
    }



    private void fillScrollLayout(){
        List<?> temp = null;
        sclMainLin.removeAllViews();
        int mode = 0;
        switch (appScreen){
            case PERSON:
                temp = personList;
                break;

            case APP:
                mode = 1;
                temp = appList;
                break;

            case BLACKLIST:
                mode = 2;
                temp = blacklistList;
                break;
        }

        if (temp == null){
            return;
        }

        person per;
        app app;
        blacklist blacklist;
        TextView input, output;
        for (int i = 0; i < temp.size(); i++) {
            final View Child = LayoutInflater.from(this).inflate(R.layout.item, null);
            pi = i;
            Object obj = temp.get(i);

            input = (TextView) Child.findViewById(R.id.itemInput);
            output = (TextView) Child.findViewById(R.id.itemOutput);

            final TextView ID = (TextView) Child.findViewById(R.id.itemId);

            Child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //PostClicked(ID.getText().toString());
                }
            });

            final Button btnDel = Child.findViewById(R.id.itemDelete);
            final Object objDel = obj;
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnDeleteClick(objDel);
                }
            });

            switch (mode){
                case PERSON:
                    per = (person) obj;
                    ID.setText(String.valueOf(per.getId()));
                    input.setText(per.getInput());
                    output.setText(per.getOutput());
                    break;

                case APP:
                    app = (app) obj;
                    ID.setText(String.valueOf(app.getId()));
                    input.setText(app.getInput());
                    output.setText(app.getOutput());
                    break;

                case BLACKLIST:
                    blacklist = (blacklist) obj;
                    ID.setText(String.valueOf(blacklist.getId()));
                    input.setText(blacklist.getInput());
                    output.setText(String.valueOf(blacklist.getType()));
                    break;
            }

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sclMainLin.addView(Child, pi);
                }
            });
        }
    }

}
