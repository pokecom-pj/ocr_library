package com.example.myplugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import jp.co.ip_consulting.drivercardocrlibrary.DriverCardOCR;
import androidx.appcompat.app.AppCompatActivity;
import jp.co.ip_consulting.drivercardocrlibrary.DriverCardOCR.RESULT;
import jp.co.ip_consulting.drivercardocrlibrary.DriverCardOCR.SCAN_TYPE;
import android.os.Bundle;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class MyPlugin extends CordovaPlugin {

    private CallbackContext callbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        cordova.setActivityResultCallback(this);
        AppCompatActivity activity = (AppCompatActivity) cordova.getActivity();
        DriverCardOCR.Companion.getShared().doScanCard(activity, new Function3<RESULT, Bundle, SCAN_TYPE, Unit>() {
            @Override
            public Unit invoke(RESULT result, Bundle resultData, SCAN_TYPE scanType) {
                callbackContext.success("resultData");
                return null;
            }
        });
    }
}

