package org.liaohailong.pdftestapp;

import android.view.View;
import android.widget.TextView;

import org.liaohailong.pdftestapp.inject.BindContentView;
import org.liaohailong.pdftestapp.inject.BindOnClick;
import org.liaohailong.pdftestapp.inject.FindViewById;
import org.liaohailong.pdftestapp.inject.SaveState;

/**
 * 测试的fragment
 * Created by LHL on 2017/9/6.
 */
@BindContentView(R.layout.fragment_main)
public class MainFragment extends BaseFragment implements View.OnClickListener {
    @FindViewById(R.id.text_fragment)
    private TextView textView;

    @SaveState
    private int count = 0;
    private static final String TEXT = "我被点击了!!";


    @BindOnClick({R.id.text_fragment})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_fragment:
                count++;
                textView.setText(TEXT + count + "次");
                break;
        }
    }
}
