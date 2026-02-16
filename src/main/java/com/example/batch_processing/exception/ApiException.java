package com.example.batch_processing.exception;

public class ApiException extends RuntimeException{

    public ApiException(String message){
        super(message);
    }
}
