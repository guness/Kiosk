// ICommandServiceCallback.aidl
package com.guness.kiosk.service;

// Declare any non-default types here with import statements

interface ICommandServiceCallback {

    void setServiceCards(in List<String> enabledCardIds);

}
