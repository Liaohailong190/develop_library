package org.liaohailong.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.liaohailong.library.http.Http;
import org.liaohailong.library.inject.Victor;

/**
 * 所有APP基类 项目BaseFragment需继承此类
 * Created by LHL on 2017/9/6.
 */

public abstract class RootFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = onCreateContentView(inflater, container, savedInstanceState);
        return Victor.inject(this, view, savedInstanceState);
    }

    protected abstract View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Victor.getSaveState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Http.clearTask(null);
    }
}
