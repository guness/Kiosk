package com.guness.kiosk.webservice.model;

/**
 * Created by guness on 27/06/2017.
 */

public class Item {
    String Code;
    String UIMessage;
    String Detail;
    String InnerException;
    String ExceptionToString;
    int TypeCode;

    public String getCode() {
        return Code;
    }

    public String getUIMessage() {
        return UIMessage;
    }

    public String getDetail() {
        return Detail;
    }

    public String getInnerException() {
        return InnerException;
    }

    public String getExceptionToString() {
        return ExceptionToString;
    }

    public int getTypeCode() {
        return TypeCode;
    }

    @Override
    public String toString() {
        return "Item{" +
                "Code='" + Code + '\'' +
                ", UIMessage='" + UIMessage + '\'' +
                ", Detail='" + Detail + '\'' +
                ", InnerException='" + InnerException + '\'' +
                ", ExceptionToString='" + ExceptionToString + '\'' +
                ", TypeCode=" + TypeCode +
                '}';
    }
}
