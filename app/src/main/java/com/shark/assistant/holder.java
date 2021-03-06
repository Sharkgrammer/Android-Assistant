package com.shark.assistant;

import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class holder {

    private List<app> appList;
    private List<person> personList;
    private List<blacklist> blacklistList;
    private database db;

    public void createTables(){
        //make tables if exist
        db.createTablesApp();
        db.createTablesPerson();
        db.createTablesBlacklist();
    }

    public holder(Context c){
        db = new database(c);
        //Loaded appLIst and personList here from whatever data source
        refresh();
    }

    public List<app> getAppList() {
        return appList;
    }

    public List<person> getPersonList() {
        return personList;
    }

    public void refresh(){
        personList = db.getTablesPerson();
        appList = db.getTablesApp();
        blacklistList = db.getTablesBlacklist();
    }

    public void newPerson(String input, String output){
        person p = new person();
        p.setInput(input);
        p.setOutput(output);

        for (person x : personList){
            if (x.getInput().equals(input)){
                return;
            }
        }

        db.insertTablesPerson(p);
        refresh();
    }

    public void newApp(String input, String output){
        app a = new app();
        a.setInput(input);
        a.setOutput(output);

        for (app x : appList){
            if (x.getInput().equals(input)){
                return;
            }
        }

        db.insertTablesApp(a);
        refresh();
    }

    public void deletePerson(int id){
        db.deletePerson(id);
        refresh();
    }

    public void deleteApp(int id){
        db.deleteApp(id);
        refresh();
    }

    public void savePerson(person p){
        db.savePerson(p.getId(), p.getInput(), p.getOutput());
        refresh();
    }

    public void saveApp(app a){
        db.saveApp(a.getId(), a.getInput(), a.getOutput());
        refresh();
    }

    public List<blacklist> getBlacklistList() {
        return blacklistList;
    }

    public void newBlacklist(String input, int type){
        blacklist b = new blacklist();
        b.setInput(input);
        b.setType(type);

        for (blacklist x : blacklistList){
            if (x.getInput().equals(input)){
                return;
            }
        }

        db.insertTablesBlacklist(b);
        refresh();
    }

    public void saveBlacklist(blacklist b){
        db.saveBlacklist(b.getId(), b.getInput(), b.getType());
        refresh();
    }

    public void deleteBlacklist(int id){
        db.deleteBlacklist(id);
        refresh();
    }

    public boolean restartDatabase(){
        return db.restartDatabase();
    }

}
