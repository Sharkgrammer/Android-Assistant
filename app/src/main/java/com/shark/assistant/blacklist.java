package com.shark.assistant;

public class blacklist {

    private int id;
    private String input;
    private int type;
    //0 = contains
    //1 = exact
    //2 = regex


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInput() {
        return input.toLowerCase();
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
