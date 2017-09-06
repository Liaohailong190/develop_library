package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.pdftestapp.inject.Victor;

/**
 * 所有fragment的基类
 * Created by LHL on 2017/9/6.
 */

public class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Victor.inject(this, inflater, container);
    }
}
