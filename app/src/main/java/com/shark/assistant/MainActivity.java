package com.shark.assistant;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView lblExplain;
    private holder data;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;
    private int appScreen = 0, pi;
    private final int PERSON = 0, APP = 1, BLACKLIST = 2;
    private LinearLayout sclMainLin;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private boolean isOn = true;
    private Button btnOnOff;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!isOn){
                return;
            }

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
        sclMainLin = findViewById(R.id.sclMainLin);
        btnOnOff = findViewById(R.id.btnOnOff);

        //Setup drawer

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerMain);
        drawerList = (ListView) findViewById(R.id.drawerList);

        setupToolbar();

        drawerItem[] drawerItem = new drawerItem[3];

        drawerItem[0] = new drawerItem(getResources().getString(R.string.people));
        drawerItem[1] = new drawerItem(getResources().getString(R.string.apps));
        drawerItem[2] = new drawerItem(getResources().getString(R.string.blacklist));

        drawerAdapter adapter = new drawerAdapter(this, R.layout.list_item, drawerItem);
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerMain);
        drawerLayout.setDrawerListener(drawerToggle);
        setupDrawerToggle();
        //End setup

        refresh();
        btnPersonClick(null);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    public void btnNewClick(View v) {
        popupDialog(null);
    }

    public void btnPersonClick(View v) {
        appScreen = PERSON;
        lblExplain.setText(R.string.peopleExplain);
        setTitle(R.string.people);
        refresh();
    }

    public void btnBlacklistClick(View v) {
        appScreen = BLACKLIST;
        lblExplain.setText(R.string.blacklistExplain);
        setTitle(R.string.blacklist);
        refresh();
    }

    public void btnAppClick(View v) {
        appScreen = APP;
        lblExplain.setText(R.string.appExplain);
        setTitle(R.string.apps);
        refresh();
    }

    public void btnDeleteClick(final Object obj) {
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
            }
        });
        deleteAlert.setNegativeButton("No", null);
        deleteAlert.setCancelable(true);
        deleteAlert.show();
    }

    public void btnEditClick(final Object obj) {
        popupDialog(obj);
    }

    public void btnOnOff(View v) {
        if (isOn){
            btnOnOff.setText(getResources().getString(R.string.turnOn));
        }else{
            btnOnOff.setText(getResources().getString(R.string.turnOff));
        }

        isOn = !isOn;
    }

    private void refresh(){
        appList = data.getAppList();
        personList = data.getPersonList();
        blacklistList = data.getBlacklistList();

        fillScrollLayout();
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

            final Button btnDel = Child.findViewById(R.id.itemDelete);
            final Button btnEdit = Child.findViewById(R.id.itemEdit);
            final Object objMain = obj;

            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnDeleteClick(objMain);
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnEditClick(objMain);
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

    private void popupDialog(final Object obj){

        LayoutInflater li = LayoutInflater.from(this);
        View dialog = li.inflate(R.layout.dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialog);

        person per;
        app app;
        blacklist blacklist;
        final EditText input = (EditText) dialog.findViewById(R.id.dialogInput);
        final EditText output = (EditText) dialog.findViewById(R.id.dialogOutput);
        final TextView ID = (TextView) dialog.findViewById(R.id.dialogID);
        Button save = (Button) dialog.findViewById(R.id.dialogBtnSave);
        Button exit = (Button) dialog.findViewById(R.id.dialogBtnExit);

        if (obj != null){
            String titlePart = "";
            switch (appScreen){
                case PERSON:
                    per = (person) obj;
                    ID.setText(String.valueOf(per.getId()));
                    input.setText(per.getInput());
                    output.setText(per.getOutput());
                    titlePart = " person \"" + per.getInput() + "\"";
                    break;

                case APP:
                    app = (app) obj;
                    ID.setText(String.valueOf(app.getId()));
                    input.setText(app.getInput());
                    output.setText(app.getOutput());
                    titlePart = " app \"" + app.getInput() + "\"";
                    break;

                case BLACKLIST:
                    blacklist = (blacklist) obj;
                    ID.setText(String.valueOf(blacklist.getId()));
                    input.setText(blacklist.getInput());
                    output.setText(String.valueOf(blacklist.getType()));
                    titlePart = " blacklist item \"" + blacklist.getInput() + "\"";
                    break;
            }

            alertDialogBuilder.setTitle(getResources().getString(R.string.editDialog) + titlePart);
        }
        else{
            String titlePart = "";
            switch (appScreen){
                case PERSON:
                    input.setHint(getResources().getString(R.string.peopleInput));
                    output.setHint(getResources().getString(R.string.peopleOutput));
                    titlePart = " person";
                    break;

                case APP:
                    input.setHint(getResources().getString(R.string.appInput));
                    output.setHint(getResources().getString(R.string.appOutput));
                    titlePart = " app";
                    break;

                case BLACKLIST:
                    input.setHint(getResources().getString(R.string.appInput));
                    output.setHint(getResources().getString(R.string.appOutput));
                    titlePart = " blacklist item";
                    break;
            }

            alertDialogBuilder.setTitle(getResources().getString(R.string.addDialog) + titlePart);
        }

        final AlertDialog alert = alertDialogBuilder.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person p = new person();
                app a = new app();
                blacklist b = new blacklist();

                int intID = Integer.parseInt(ID.getText().toString());
                String intInput = input.getText().toString();
                String intOutput = output.getText().toString();

                if (intInput.isEmpty() || intOutput.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Textboxes must have text", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (obj == null){
                    switch (appScreen){
                        case PERSON:
                            data.newPerson(intInput, intOutput);
                            break;

                        case APP:
                            data.newApp(intInput, intOutput);
                            break;

                        case BLACKLIST:

                            try{
                                int tempint = Integer.valueOf(intOutput);
                                if (tempint >= 0 && tempint <= 2){
                                    data.newBlacklist(intInput, tempint);
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
                            p.setId(intID);
                            p.setInput(intInput);
                            p.setOutput(intOutput);

                            data.savePerson(p);
                            break;

                        case APP:
                            a.setId(intID);
                            a.setInput(intInput);
                            a.setOutput(intOutput);

                            data.saveApp(a);
                            break;

                        case BLACKLIST:
                            int tempint;
                            b.setInput(intInput);
                            try{
                                tempint = Integer.valueOf(intOutput);
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

                alert.dismiss();
                refresh();

            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {
        switch (position){
            case PERSON:
                btnPersonClick(null);
                break;

            case APP:
                btnAppClick(null);
                break;

            case BLACKLIST:
                btnBlacklistClick(null);
                break;
        }

        drawerLayout.closeDrawer(drawerList);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    void setupDrawerToggle(){
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name, R.string.app_name);
        drawerToggle.syncState();
    }

}
