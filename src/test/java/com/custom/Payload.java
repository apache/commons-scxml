package com.custom;

public class Payload {

    private final int id;
    private final String someString;

    public Payload(int id, String someString) {
        this.id = id;
        this.someString = someString;
    }

    public int getId() {
        return id;
    }

    public String getSomeString() {
        return someString;
    }
}
