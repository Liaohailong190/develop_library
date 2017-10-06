package org.liaohailong.pdftestapp.widget.glide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Glide工具类
 * Created by LHL on 2017/10/6.
 */

public class GlideUtil {
    private GlideUtil() {

    }

    public static GlideRequests with(@NonNull Object object) {
        GlideRequests requests = null;
        if (object instanceof FragmentActivity) {
            FragmentActivity param = (FragmentActivity) object;
            requests = GlideApp.with(param);
        } else if (object instanceof Activity) {
            Activity param = (Activity) object;
            requests = GlideApp.with(param);
        } else if (object instanceof Fragment) {
            Fragment param = (Fragment) object;
            requests = GlideApp.with(param);
        } else if (object instanceof android.support.v4.app.Fragment) {
            android.support.v4.app.Fragment param = (android.support.v4.app.Fragment) object;
            requests = GlideApp.with(param);
        } else if (object instanceof View) {
            View param = (View) object;
            requests = GlideApp.with(param);
        } else if (object instanceof Context) {
            Context param = (Context) object;
            requests = GlideApp.with(param);
        }
        return requests;
    }
}
