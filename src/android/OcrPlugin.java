package jp.bluememe.plugin.ocr;

import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.bluememe.plugin.ocr.plugin.OcrPluginManager;

public class OcrPlugin extends CordovaPlugin {

    // MARK: - Callback
    private CallbackContext mOcrCallbackId = null;

    // MARK: - Member
    private OcrPluginManager mOcrPluginManager = null;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Context applicationContext = cordova.getActivity().getApplicationContext();
        if (action.equals("startOCR")) {
            mOcrCallbackId = callbackContext;
            startOcr();
            return true;
        }
        return false;
    }

    // MARK: - Function
    private OcrPluginManager getOcrPluginManager() {
        if (mOcrPluginManager == null) {
            mOcrPluginManager = new OcrPluginManager();
        }
        return mOcrPluginManager;
    }

    private void startOcr() {
        getOcrPluginManager().start(cordova.getActivity(), new OcrPluginManager.OnOcrPluginManagerListener() {
            @Override
            public void onResult(OcrPluginManager sender, int code, OcrPluginManager.OcrResultInfo result) {
                ocrStop();
                JSONObject resultJson = new JSONObject();
                if (code == OcrPluginManager.CODE_SUCCESS) {
                    if (result == null) {
                        try {
                            resultJson.put("errorCode", -1);
                        } catch (JSONException e) {
                        }
                        mOcrCallbackId.error(resultJson);
                    } else {
                        try {
                            resultJson.put("errorCode", 0);
                            resultJson.put("type", result.mCardType);
                            resultJson.put("name", result.mName);
                            resultJson.put("address", result.mAddress);
                            resultJson.put("birthdate", result.mBirthdate);
                            resultJson.put("gender", result.mGender);
                        } catch (JSONException e) {
                        }
                        mOcrCallbackId.error(resultJson);
                    }
                } else if (code == OcrPluginManager.CODE_AUTHROIZE) {
                    try {
                        resultJson.put("errorCode", -2);
                    } catch (JSONException e) {
                    }
                    mOcrCallbackId.error(resultJson);
                } else {
                    try {
                        resultJson.put("errorCode", -1);
                    } catch (JSONException e) {
                    }
                    mOcrCallbackId.error(resultJson);
                }
            }
        });
    }

    private void ocrStop() {
        getOcrPluginManager().stop();
        mOcrPluginManager = null;
    }
}
