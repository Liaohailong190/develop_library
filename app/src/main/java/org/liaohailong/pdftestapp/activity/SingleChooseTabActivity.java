package org.liaohailong.pdftestapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.liaohailong.library.inject.OnClick;
import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.widget.tab.SingleChooseTabView;
import org.liaohailong.pdftestapp.widget.tab.TabEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe as : 单选Tab View演示界面
 * Created by LHL on 2018/6/5.
 */

public class SingleChooseTabActivity extends BaseActivity {

    private SingleChooseTabView tabView;

    private int rawCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_choos_tab);

        tabView = findViewById(R.id.tab_view);
        refresh();
    }

    private void refresh() {
        tabView.setAuto(true)
                .setAutoDuration(1000)
                .setTabDirection(LinearLayout.HORIZONTAL)
                .setBackColor(Color.LTGRAY)
                .setShowBorder(true)
                .setBorderColor(Color.BLUE)
                .setBorderWidth(10)
                .setRawCount(rawCount)
                .setRadius(15);
        List<TabEntry> data = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            TabEntry tabEntry = new TabEntry();
            tabEntry.setTextSize(14 * 3);
            tabEntry.setTextColor(Color.WHITE);
            tabEntry.setTextBold(true);
            tabEntry.setSelectedBackColor(Color.RED);
            tabEntry.setSelectedTextColor(Color.YELLOW);
            tabEntry.setText("我是第-<" + i + ">-号测试数据aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            data.add(tabEntry);
        }
        tabView.setData(data);

        tabView.postDelayed(new Runnable() {
            @Override
            public void run() {
                tabView.notifyDataSetChanged();
            }
        }, 2000);
    }

    @OnClick({R.id.add_btn, R.id.remove_btn})
    public void changed(View view) {
        switch (view.getId()) {
            case R.id.add_btn://添加一个tab
                rawCount++;
                break;
            case R.id.remove_btn://移除一个tab
                rawCount--;
                break;
        }
        rawCount = rawCount > 10 ? 10 : rawCount;
        rawCount = rawCount < 1 ? 1 : rawCount;
        refresh();
    }
}
