package org.liaohailong.pdftestapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_choos_tab);

        tabView = findViewById(R.id.tab_view);
        tabView.setAuto(true)
                .setAutoDuration(1000)
                .setTabDirection(LinearLayout.HORIZONTAL)
                .setBackColor(Color.LTGRAY)
                .setRawCount(2)
                .setRadius(15);
        //TODO 当展示数量<3 && 数据返回数量 >=3 的时候有逻辑漏洞---->高亮位于第二个的时候，如何把第三位的tab展示出来？
        //TODO 上面的问题其实我的代码解决了，但是需要探讨是否统一我的方案！！！
        List<TabEntry> data = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            TabEntry tabEntry = new TabEntry();
            tabEntry.setTextSize(16 * 3);
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
}
