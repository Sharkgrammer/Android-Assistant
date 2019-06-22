package com.shark.assistant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "wave.db";
    private SQLiteDatabase db;

    public database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db2) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }

    public void createTablesPerson() {
        String Query = "create table person(person_id int PRIMARY KEY," +
                "person_input varchar(255) not null," +
                "person_output varchar(255) not null" +
                ");";
        try{
            db.execSQL(Query);
        }catch(Exception e){
            Log.wtf("Error", e.toString());
        }
    }

    public void insertTablesPerson(person p){
        String Query = "insert into person(" +
                "person_input," +
                "person_output" +
                ") values ('" +
                p.getInput() + "','" +
                p.getOutput() + "'" +
                ");";
        db.execSQL(Query);
    }

    public List<person> getTablesPerson(){
        String query = "select * from person";
        List<person> list = new ArrayList<>();

        Cursor cursor = this.getWritableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        person p;
        do{

            p = new person();
            p.setId(cursor.getInt(0));
            p.setInput(cursor.getString(1));
            p.setOutput(cursor.getString(2));

            list.add(p);

        }while(cursor.moveToNext());

        cursor.close();
        return list;
    }

    public void deletePerson(int id){
        String Query = "delete from person where person_id = " + id;
        db.execSQL(Query);
    }

    public void createTablesApp() {
        String Query = "create table app(app_id int PRIMARY KEY," +
                "app_input varchar(255) not null," +
                "app_output varchar(255) not null" +
                ");";
        try{
            db.execSQL(Query);
        }catch(Exception e){
            Log.wtf("Error", e.toString());
        }
    }

    public void insertTablesApp(app a){
        String Query = "insert into app(" +
                "app_input," +
                "app_output" +
                ") values ('" +
                a.getInput() + "','" +
                a.getOutput() + "'" +
                ");";
        db.execSQL(Query);
    }

    public List<app> getTablesApp(){
        String query = "select * from app";
        List<app> list = new ArrayList<>();

        Cursor cursor = this.getWritableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        app a;
        do{

            a = new app();
            a.setId(cursor.getInt(0));
            a.setInput(cursor.getString(1));
            a.setOutput(cursor.getString(2));

            list.add(a);

        }while(cursor.moveToNext());

        cursor.close();
        return list;
    }

    public void deleteApp(int id){
        String Query = "delete from app where app_id = " + id;
        db.execSQL(Query);
    }

}
