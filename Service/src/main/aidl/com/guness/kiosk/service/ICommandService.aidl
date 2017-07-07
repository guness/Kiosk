// ICommandService.aidl
package com.guness.kiosk.service;

import com.guness.kiosk.service.ICommandServiceCallback;

// Declare any non-default types here with import statements

interface ICommandService {

    void setCallback(ICommandServiceCallback callback);

    void clearMetaCache();

    int getPid();

}
