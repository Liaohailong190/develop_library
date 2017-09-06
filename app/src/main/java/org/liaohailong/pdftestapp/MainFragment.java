package org.liaohailong.pdftestapp;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.liaohailong.pdftestapp.inject.BindContentView;
import org.liaohailong.pdftestapp.inject.BindOnClick;
import org.liaohailong.pdftestapp.inject.FindViewById;

/**
 * 测试的fragment
 * Created by LHL on 2017/9/6.
 */
@BindContentView(R.layout.fragment_main)
public class MainFragment extends BaseFragment implements View.OnClickListener {
    @FindViewById(R.id.text_fragment)
    private TextView textView;


    @BindOnClick({R.id.text_fragment})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_fragment:
                TextView txt = (TextView) v;
                Toast.makeText(getContext(), txt.getText().toString() + " 被点击了!!", Toast.LENGTH_LONG).show();
                textView.setText("我被点击了！！");
                break;
        }
    }
}
