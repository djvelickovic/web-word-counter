package com.crx.raf.kids.d1.util;

public class Error {

    public static Error of(ErrorCode code, String message){
        return new Error(code,message);
    }

    private ErrorCode errorCode;
    private String message;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Error(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Error{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }
}
