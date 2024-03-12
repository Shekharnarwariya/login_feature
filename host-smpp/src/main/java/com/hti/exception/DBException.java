/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.exception;

/**
 *
 * @author Administrator
 */
public class DBException extends Exception {


    public DBException() {
        super("DatabaseException");
    }

    public DBException(String message) {
        super(message);
    }

}
