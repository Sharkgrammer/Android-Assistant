package com.shark.assistant;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class processor {

    Context context;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;
    private final int CONTAINS = 0, EXACT = 1, REGEX = 2;

    public processor(Context c){
        context = c;
        holder data = new holder(c);
        blacklistList = data.getBlacklistList();
        appList = data.getAppList();
        personList = data.getPersonList();
    }

    public String processText(String text, boolean privateMode){
        text = text.toLowerCase();

        //Check if its a link here
        Pattern pattern = Pattern.compile("(http)(.)?\\:\\/\\/(\\w*\\.?\\-?\\??\\=?\\&?\\/*)*");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()){
            text = text.replace(matcher.group(), context.getResources().getString(R.string.linkText));
        }

        if (!appList.isEmpty()){
            for (app x : appList){
                if (text.contains(x.getInput())){
                    text = text.replace(x.getInput(), x.getOutput());
                }
            }
        }

        if (!personList.isEmpty()){
            for (person x : personList){
                if (text.contains(x.getInput())){
                    text = text.replace(x.getInput(), x.getOutput());
                }
            }
        }

        String blacklistText = text.replace("---", "");
        if (!blacklistList.isEmpty()){
            for (blacklist x : blacklistList){
                Log.wtf("BLACKLIST", x.getInput());
                switch(x.getType()){
                    case CONTAINS:

                        if (blacklistText.contains(x.getInput())){
                            return null;
                        }

                        break;

                    case EXACT:

                        if (blacklistText.equals(x.getInput())){
                            return null;
                        }

                        break;

                    case REGEX:

                        pattern = Pattern.compile(x.getInput());
                        matcher = pattern.matcher(blacklistText);

                        if (matcher.find()){
                            return null;
                        }

                        break;
                }
            }
        }

        if (privateMode){
            String[] tempTextArr = text.split("---");
            text = tempTextArr[0] + " --- " + tempTextArr[1];
        }

        return text;
    }

}
