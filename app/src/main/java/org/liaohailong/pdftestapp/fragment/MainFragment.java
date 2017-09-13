package org.liaohailong.pdftestapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.liaohailong.library.async.Cat;
import org.liaohailong.library.async.Async;
import org.liaohailong.library.async.Mouse;
import org.liaohailong.library.async.Schedulers;
import org.liaohailong.library.db.OrmDao;
import org.liaohailong.library.image.ImageLoader;
import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.SaveState;
import org.liaohailong.pdftestapp.BaseFragment;
import org.liaohailong.pdftestapp.R;
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
public class MainFragment extends BaseFragment {
    @BindView(R.id.text_fragment)
    private TextView textView;
    @BindView(R.id.avatar)
    private ImageView avatar;

    @SaveState
    private int count = 0;
    private static final String TEXT = "我被点击了!!";

    private String imageUrl01 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505054331352&di=67367353f3ac52e7cdaca7221de9c39d&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20161003%2F599d93c935d646b9a1b7e8adb049a8fa_th.jpg";
    private String imageUrl02 = "/storage/emulated/0/output_image.jpg";
    private String imageUrl03 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505106699538&di=6e7649394fca8898968dd0a1388c9b76&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20160829%2F24997e71d5814cf48f307d7caece946c.gif";

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
    public void recordCount(View v) {
        switch (v.getId()) {
            case R.id.text_fragment:
                count++;
                textView.setText(TEXT + count + "次");
                break;
        }
    }

    @OnClick(R.id.avatar)
    private void showImage(View view) {
        Object tag = avatar.getTag();
        if (tag == null) {
            ImageLoader.getInstance().setImage(avatar, imageUrl03);
            avatar.setTag(true);
            return;
        }
        boolean isUrl = (boolean) tag;
        ImageLoader.getInstance().setImage(avatar, isUrl ? imageUrl02 : imageUrl01, R.drawable.eee_drawable);
        avatar.setTag(!isUrl);
    }
}
