package com.shark.assistant;

public class app {

    private int id;
    private String output;
    private String input;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOutput() {
        return output.toLowerCase();
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getInput() {
        return input.toLowerCase();
    }

    public void setInput(String input) {
        this.input = input;
    }
}
