package jp.bluememe.plugin.ocr.plugin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

import jp.co.ip_consulting.drivercardocrlibrary.DriverCardOCR;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class OcrPluginManager {

    // MARK: - Define
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILED = -1;
    public static final int CODE_AUTHROIZE = -2;

    public static final int CARDTYPE_MENKYOSYO = 1;
    public static final int CARDTYPE_MYNUMBER = 2;
    public static final int CARDTYPE_ZAIRYU = 3;

    // MARK: - Member
    private OnOcrPluginManagerListener mListener = null;

    // MARK: - Listener
    public interface OnOcrPluginManagerListener {
        void onResult(OcrPluginManager sender, int code, OcrPluginManager.OcrResultInfo result);
    }

    // MARK: - Accesser
    public void start (AppCompatActivity activity, OnOcrPluginManagerListener listener) {
        mListener = listener;
        Permission.checkPermission(activity, new OnPermissionListener() {
            @Override
            public void onResult(boolean result) {
                if (result) {
                    doStartScan(activity);
                } else {
                    if (mListener != null) {
                        mListener.onResult(OcrPluginManager.this, CODE_AUTHROIZE, null);
                    }
                }
            }
        });
    }

    public void stop() {
        mListener = null;
    }

    // MARK: - Function
    private void doStartScan(AppCompatActivity activity) {
        DriverCardOCR.Companion.getShared().doScanCard(activity, new Function3<DriverCardOCR.RESULT, Bundle, DriverCardOCR.SCAN_TYPE, Unit>() {
            @Override
            public Unit invoke(DriverCardOCR.RESULT result, Bundle bundle, DriverCardOCR.SCAN_TYPE scanType) {
                if (result == DriverCardOCR.RESULT.SUCCESS) {
                    if (mListener != null) {
                        OcrResultInfo info = new OcrResultInfo();
                        if (scanType == DriverCardOCR.SCAN_TYPE.DriverCard) {
                            info.mCardType = CARDTYPE_MENKYOSYO;
                            if (bundle.containsKey("姓名")) {
                                info.mName = bundle.getString("姓名", "");
                            }
                            if (bundle.containsKey("住所")) {
                                info.mAddress = bundle.getString("住所", "");
                            }
                            if (bundle.containsKey("生年月日")) {
                                info.mBirthdate = bundle.getString("生年月日", "").replace("生", "");
                            }
                        } else if (scanType == DriverCardOCR.SCAN_TYPE.MyNumberCard) {
                            info.mCardType = CARDTYPE_MYNUMBER;
                            if (bundle.containsKey("氏名")) {
                                info.mName = bundle.getString("氏名", "");
                            }
                            String address = "";
                            if (bundle.containsKey("住所①")) {
                                address += bundle.getString("住所①", "");
                            }
                            if (bundle.containsKey("住所②")) {
                                address += bundle.getString("住所②", "");
                            }
                            info.mAddress = address;
                            if (bundle.containsKey("生年月日")) {
                                info.mBirthdate = bundle.getString("生年月日", "").replace("生", "");
                            }
                            if (bundle.containsKey("性別")) {
                                info.mGender = bundle.getString("性別", "");
                            }
                        } else if (scanType == DriverCardOCR.SCAN_TYPE.ZairyuCard) {
                            info.mCardType = CARDTYPE_ZAIRYU;
                            if (bundle.containsKey("氏名")) {
                                info.mName = bundle.getString("氏名", "");
                            }
                            if (bundle.containsKey("住所")) {
                                info.mAddress = bundle.getString("住所", "");
                            }
                            if (bundle.containsKey("生年月日")) {
                                info.mBirthdate = bundle.getString("生年月日", "").replace("生", "");
                            }
                            if (bundle.containsKey("性別")) {
                                info.mGender = bundle.getString("性別", "");
                            }
                        } else {
                            mListener.onResult(OcrPluginManager.this, CODE_FAILED, null);
                            return null;
                        }
                        mListener.onResult(OcrPluginManager.this, CODE_SUCCESS, info);

                    }
                }
                return null;
            }
        });
    }

    // MARK: - InnerClass
    public class OcrResultInfo implements Serializable {

        // MARK: - Member
        public int mCardType;
        public String mName;
        public String mAddress;
        public String mBirthdate;
        public String mGender;

    }

}
