package com.cimcorp.plc.logParser;

public class ArgNotFoundException extends RuntimeException {

    public ArgNotFoundException(String description){
        super("Argument not provided: " + "<" + description + ">");
    }
}
