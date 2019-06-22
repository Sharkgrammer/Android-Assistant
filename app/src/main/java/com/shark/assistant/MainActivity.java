package com.shark.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private EditText txtInput, txtOutput;
    private boolean isPerson = true;
    private holder data;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            Log.wtf("Package",pack);
            Log.wtf("Title",title);
            Log.wtf("Text",text);

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

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    public void btnNewClick(View v) {
        if (txtInput.getText().length() < 1 || txtOutput.getText().length() < 1){
            Toast.makeText(getApplicationContext(), "Textboxes cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPerson){
            data.newPerson(txtInput.getText().toString(), txtOutput.getText().toString());
        }else{
            data.newApp(txtInput.getText().toString(), txtOutput.getText().toString());
        }
    }

    public void btnPersonClick(View v) {
        isPerson = true;
        txtInput.setHint("Name in app");
        txtOutput.setHint("Nickname");
    }

    public void btnAppClick(View v) {
        isPerson = false;
        txtInput.setHint("App package");
        txtOutput.setHint("Nickname");
    }

}
