package org.liaohailong.library.util;

import android.support.annotation.StringRes;
import android.widget.Toast;

import org.liaohailong.library.RootApplication;


/**
 * 专门弹出吐司的类，为了确保吐司不反复弹出
 * Created by LHL on 2016/12/20.
 */

public class ToastUtil {
    private static Toast mToast = null;

    public static void show(@StringRes int stringRes) {
        String text = RootApplication.getInstance().getResources().getString(stringRes);
        show(text);
    }

    public static void show(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(RootApplication.getInstance(), msg, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        }

    }
}
