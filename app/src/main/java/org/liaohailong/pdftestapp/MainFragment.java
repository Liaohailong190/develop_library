package org.liaohailong.pdftestapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.liaohailong.library.db.OrmDao;
import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.BindOnClick;
import org.liaohailong.library.inject.FindViewById;
import org.liaohailong.library.inject.SaveState;
import org.liaohailong.pdftestapp.model.Student;

import java.util.List;


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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        OrmDao<Student> studentOrmDao = new OrmDao<>(Student.class);
        studentOrmDao.save(new Student("小明", 1, 18));
        studentOrmDao.save(new Student("小红", 0, 17));
        studentOrmDao.save(new Student("小芳", 0, 16));

        List<Student> students = studentOrmDao.queryAll();
        students.clear();
    }

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
