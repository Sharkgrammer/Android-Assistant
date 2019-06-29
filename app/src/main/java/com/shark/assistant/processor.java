package com.shark.assistant;

import android.content.Context;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class processor {

    Context context;
    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;

    public processor(Context c){
        context = c;
        holder data = new holder(c);
        blacklistList = data.getBlacklistList();
        appList = data.getAppList();
        personList = data.getPersonList();
    }

    public String processText(String text){
        //0 = contains
        //1 = exact
        //2 = regex
        text = text.toLowerCase();

        //Check if its a link here
        Pattern pattern = Pattern.compile("(http)(.)?\\:\\/\\/(\\w*\\.?\\-?\\??\\=?\\&?\\/*)*");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()){
            text = text.replace(matcher.group(), context.getResources().getString(R.string.linkText));
        }

        if (!blacklistList.isEmpty()){
            for (blacklist x : blacklistList){
                switch(x.getType()){
                    case 0:

                        if (text.contains(x.getInput())){
                            return null;
                        }

                        break;

                    case 1:

                        if (text.equals(x.getInput())){
                            return null;
                        }

                        break;

                    case 2:

                        pattern = Pattern.compile(x.getInput());
                        matcher = pattern.matcher(text);

                        if (matcher.find()){
                            return null;
                        }

                        break;
                }
            }
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

        return text;
    }

}
