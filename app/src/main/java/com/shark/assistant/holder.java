package com.shark.assistant;

import android.content.Context;
import android.util.Log;
import java.util.List;

public class holder {

    private List<app> appList;
    private List<person> personList;
    private database db;

    public holder(Context context){
        //Loaded appLIst and personList here from whatever data source
        db = new database(context);
        //make tables if exist
        db.createTablesApp();
        db.createTablesPerson();

        refresh();
    }

    public List<app> getAppList() {
        return appList;
    }

    public List<person> getPersonList() {
        return personList;
    }

    public String getAppName(String name){

        if (appList.isEmpty()){
            return name;
        }

        for (app x : appList){
            if (x.getInput().equals(name)){
                return x.getOutput();
            }
        }

        return name;
    }

    public String getPersonName(String name){

        if (personList.isEmpty()){
            return name;
        }

        for (person x : personList){
            if (x.getInput().equals(name)){
                return x.getOutput();
            }
        }

        return name;
    }

    private void refresh(){
        personList = db.getTablesPerson();
        appList = db.getTablesApp();
    }

    public void newPerson(String input, String output){
        person p = new person();
        p.setInput(input);
        p.setOutput(output);
        db.insertTablesPerson(p);
        refresh();
    }

    public void newApp(String input, String output){
        app a = new app();
        a.setInput(input);
        a.setOutput(output);
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

}
