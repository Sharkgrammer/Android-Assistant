package com.shark.assistant;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class mainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView lblExplain;
    private holder data;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;
    private List<log> logList;
    private int appScreen = 0, pi, logTotal = 0, logBlocked = 0;
    private final int PERSON = 0, APP = 1, BLACKLIST = 2, LOGS = 3, HIDE_HELP = 4, IMPORT_PAGE = 5, DASHBOARD = 6;
    private LinearLayout sclMainLin;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private boolean isOn = true, privateMode = false;
    private Button btnOnOff, btnAddNew, btnPrivate, btnBlocked, btnPassed, btnRecieved;
    private ConstraintLayout layDash, layMain;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!isOn){
                return;
            }

            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            log log = new log();

            Log.wtf("Package", pack);
            Log.wtf("Title", title);
            Log.wtf("Text", text);

            log.setPack(pack);
            log.setTitle(title);
            log.setText(text);

            DateFormat df;
            df = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());
            log.setTime(df.format(Calendar.getInstance().getTime()));

            String dataStr = pack + " --- " + title + " --- " + text;
            log.setOriginal(dataStr.replace("---", ""));

            processor pro = new processor(mainActivity.this);

            dataStr = pro.processText(dataStr.trim(), privateMode);

            if (dataStr == null){
                log.setBlocked(true);
                log.setFixed("Blocked notification");
                logList.add(log);
                return;
            }else{
                log.setFixed(dataStr.replace("---", ""));
            }

            logList.add(log);

            if (appScreen == LOGS){
                refresh();
            }

            Log.i("TTS", "\"" + dataStr + "\"");
            int speechStatus = textToSpeech.speak(dataStr.replace("---", "."), TextToSpeech.QUEUE_FLUSH, null);
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
        btnAddNew = findViewById(R.id.btnAdd);
        btnPrivate = findViewById(R.id.btnPrivate);
        layDash = findViewById(R.id.layDash);
        layMain = findViewById(R.id.layMain);
        btnBlocked = findViewById(R.id.btnBlocked);
        btnRecieved = findViewById(R.id.btnRecieved);
        btnPassed = findViewById(R.id.btnPassed);

        //Setup drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerMain);
        drawerList = (ListView) findViewById(R.id.drawerList);
        logList = new ArrayList<>();

        setupToolbar();

        drawerItem[] drawerItem = new drawerItem[7];

        drawerItem[0] = new drawerItem(getResources().getString(R.string.dash));
        drawerItem[1] = new drawerItem(getResources().getString(R.string.people));
        drawerItem[2] = new drawerItem(getResources().getString(R.string.apps));
        drawerItem[3] = new drawerItem(getResources().getString(R.string.blacklist));
        drawerItem[4] = new drawerItem(getResources().getString(R.string.logs));
        drawerItem[5] = new drawerItem(getResources().getString(R.string.hide));
        drawerItem[6] = new drawerItem(getResources().getString(R.string.settings));

        drawerAdapter adapter = new drawerAdapter(this, R.layout.list_item, drawerItem);
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerMain);
        drawerLayout.setDrawerListener(drawerToggle);
        setupDrawerToggle();
        //End setup

        SharedPreferences shared = this.getSharedPreferences("com.shark.assistant", MODE_PRIVATE);
        if (!shared.getBoolean("help", true)){
            lblExplain.setVisibility(View.GONE);
        }else{
            lblExplain.setVisibility(View.VISIBLE);
        }

        if (shared.getBoolean("start", true)){
            startActivity(new Intent(mainActivity.this, splashActivity.class));

            SharedPreferences prefs = this.getSharedPreferences("com.shark.assistant", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("start", false);
            editor.apply();
        }

        btnDashClick(null);

        //Hacky hack to return to later
        //TODO fix this mess

        LocalBroadcastManager ins =  LocalBroadcastManager.getInstance(this);

        try{
            ins.unregisterReceiver(onNotice);
        }catch (Exception e){
            Log.wtf("Error in LocalBroadcastManager", e.toString());
        }

        ins.registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.wtf("res", "Resume called");
        refresh(0);
    }

    public void btnNewClick(View v) {
        if (appScreen != LOGS){
            popupDialog(null);
        }else{

            int tempInt = 0;
            for (log x : logList){
                if (x.isBlocked()){
                    tempInt++;
                }
            }

            logTotal = logList.size();
            logBlocked = tempInt;

            logList.clear();
            refresh();
        }

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

    public void btnDashClick(View v){
        appScreen = DASHBOARD;
        setTitle(R.string.dash);
        refresh();
    }

    public void btnPrivate(View v) {
        privateMode = !privateMode;
        if (privateMode){
            btnPrivate.setText(R.string.privateOff);
        }else{
            btnPrivate.setText(R.string.privateOn);
        }
    }

    public void btnLogsClick(View v) {
        appScreen = LOGS;
        lblExplain.setText(R.string.logsExplain);
        setTitle(R.string.logs);
        refresh();
    }

    public void btnBlacklistItemClick(final log log){
        if (log.isBlocked()){
            btnBlacklistClick(null);
        }else{
            AlertDialog.Builder itemAlert = new AlertDialog.Builder(this);
            itemAlert.setMessage("Are you sure you want to blacklist \"" + log.getFixed() + "\"?");
            itemAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    data.newBlacklist(log.getFixed(), 1);
                }
            });
            itemAlert.setNegativeButton("No", null);
            itemAlert.setCancelable(true);
            itemAlert.show();
        }
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

    private void refresh(int i){
        data.refresh();
        refresh();
    }


    private void refresh(){

        if (appScreen == DASHBOARD){
            layDash.setVisibility(View.VISIBLE);
            layMain.setVisibility(View.GONE);

            btnRecieved.setText(String.valueOf(logList.size() + logTotal));

            int tempInt = 0;
            for (log x : logList){
                if (x.isBlocked()){
                    tempInt++;
                }
            }

            btnBlocked.setText(String.valueOf(tempInt + logBlocked));
            btnPassed.setText(String.valueOf((logList.size() + logTotal) - (tempInt + logBlocked)));

        }else{
            layDash.setVisibility(View.GONE);
            layMain.setVisibility(View.VISIBLE);

            appList = data.getAppList();
            personList = data.getPersonList();
            blacklistList = data.getBlacklistList();

            fillScrollLayout();
        }

    }


    private void fillScrollLayout(){
        List<?> temp = null;
        sclMainLin.removeAllViews();
        int mode = PERSON;
        if (privateMode){
            btnPrivate.setText(R.string.privateOff);
        }else{
            btnPrivate.setText(R.string.privateOn);
        }
        switch (appScreen){
            case PERSON:
                temp = personList;
                break;

            case APP:
                mode = APP;
                temp = appList;
                break;

            case BLACKLIST:
                mode = BLACKLIST;
                temp = blacklistList;
                break;
            case LOGS:
                fillLogLayout();
                return;
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

            Child.setOnClickListener(new View.OnClickListener() {
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

            mainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sclMainLin.addView(Child, pi);
                }
            });
        }
    }

    private void fillLogLayout(){
        sclMainLin.removeAllViews();
        btnAddNew.setText(R.string.privateClear);
        TextView pack,title,text,fixed,original,time;
        ConstraintLayout lay;
        Drawable failBack = getResources().getDrawable(R.drawable.itemlogborderfail);
        boolean isBlacklisted;

        for (int i = logList.size() - 1; i >= 0; i--) {
            final View Child = LayoutInflater.from(this).inflate(R.layout.log_item, null);
            pi = logList.size() - i;
            final log log = logList.get(i);

            pack = (TextView) Child.findViewById(R.id.itemPackage);
            title = (TextView) Child.findViewById(R.id.itemTitle);
            text = (TextView) Child.findViewById(R.id.itemText);
            original = (TextView) Child.findViewById(R.id.itemOriginal);
            fixed = (TextView) Child.findViewById(R.id.itemFixed);
            time = (TextView) Child.findViewById(R.id.itemTime);
            lay = (ConstraintLayout) Child.findViewById(R.id.itemLayLog);
            final Button btnLog = Child.findViewById(R.id.itemBlacklist);

            pack.setText(log.getPack());
            title.setText(log.getTitle());
            text.setText(log.getText());
            original.setText(log.getOriginal());
            fixed.setText(log.getFixed());
            time.setText(log.getTime());
            isBlacklisted = log.isBlocked();


            if (isBlacklisted){
                btnLog.setText(R.string.unblacklist);
                lay.setBackground(failBack);
            }

            btnLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnBlacklistItemClick(log);
                }
            });

            mainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sclMainLin.addView(Child, pi - 1);
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
                    input.setHint(getResources().getString(R.string.blacklistInput));
                    output.setHint(getResources().getString(R.string.blacklistOutput));
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
                            b.setId(intID);
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
            case 0:
                btnDashClick(null);
                break;

            case PERSON + 1:
                btnPersonClick(null);
                break;

            case APP + 1:
                btnAppClick(null);
                break;

            case BLACKLIST + 1:
                btnBlacklistClick(null);
                break;
            case LOGS + 1:
                btnLogsClick(null);
                break;
            case HIDE_HELP + 1:
                if (lblExplain.getVisibility() == View.VISIBLE){
                    lblExplain.setVisibility(View.GONE);
                }else{
                    lblExplain.setVisibility(View.VISIBLE);
                }

                SharedPreferences prefs = this.getSharedPreferences("com.shark.assistant", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("help", lblExplain.getVisibility() == View.VISIBLE);
                editor.apply();

                break;
            case IMPORT_PAGE + 1:
                startActivity(new Intent(mainActivity.this, importActivity.class));
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

        //also set button pls
        String text = getResources().getString(R.string.add) + " " + title;
        btnAddNew.setText(text);

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
