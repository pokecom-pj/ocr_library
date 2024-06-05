package jp.bluememe.plugin.ocr.plugin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

interface OnPermissionListener {
    void onResult(boolean result);
}

public class Permission {

    // MARK: - Define
    private static final String[] dPermission = { Manifest.permission.CAMERA };

    // MARK: - Accesser
    public static void checkPermission(AppCompatActivity activity, OnPermissionListener listener) {
        if (isPermissionGranted(activity)) {
            if (listener != null) {
                listener.onResult(true);
            }
            return;
        }

        ActivityResultLauncher<String[]> requestPermission = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (!result.values().contains(false)) {
                if (listener != null) {
                    listener.onResult(true);
                }
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("警告");
            builder.setMessage("カメラアクセスの許可が必要です");
            builder.setCancelable(false);
            builder.setNegativeButton("設定へ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null
                    ));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
            });
            builder.create().show();
            if (listener != null) {
                listener.onResult(false);
            }
        });
        requestPermission.launch(dPermission);
    }

    // MARK: - Function
    private static boolean isPermissionGranted(AppCompatActivity activity) {
        for (String permission: dPermission) {
            int checkPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
