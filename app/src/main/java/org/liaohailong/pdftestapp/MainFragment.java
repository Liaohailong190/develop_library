package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.liaohailong.library.async.Cat;
import org.liaohailong.library.async.Async;
import org.liaohailong.library.async.Mouse;
import org.liaohailong.library.async.Schedulers;
import org.liaohailong.library.db.OrmDao;
import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.SaveState;
import org.liaohailong.pdftestapp.model.Student;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * 测试的fragment
 * Created by LHL on 2017/9/6.
 */
@BindContentView(R.layout.fragment_main)
public class MainFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.text_fragment)
    private TextView textView;

    @SaveState
    private int count = 0;
    private static final String TEXT = "我被点击了!!";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        new Async<String>()
                .watch(new Mouse<String>() {
                    @Override
                    public String run() {
                        String text = "";
                        try {
                            HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://www.baidu.com/").openConnection();
                            int code = urlConnection.getResponseCode();
                            text = "code = " + code;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return text;
                    }
                })
                .by(new Cat<String>() {
                    @Override
                    public void chase(String s) {
                        textView.setText(s);
                    }
                })
                .mouseOn(Schedulers.IO_THREAD)
                .catOn(Schedulers.UI_THREAD)
                .start();

        OrmDao<Student> studentOrmDao = new OrmDao<>(Student.class);
        studentOrmDao.save(new Student("小明", 1, 18));
        studentOrmDao.save(new Student("小红", 0, 17));
        studentOrmDao.save(new Student("小芳", 0, 16));

        List<Student> students = studentOrmDao.queryAll();
        students.clear();
    }

    @OnClick({R.id.text_fragment})
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
