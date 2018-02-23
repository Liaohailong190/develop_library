package org.liaohailong.pdftestapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.liaohailong.library.async.Async;
import org.liaohailong.library.async.Cat;
import org.liaohailong.library.async.Mouse;
import org.liaohailong.library.async.Schedulers;
import org.liaohailong.library.db.Orm;
import org.liaohailong.library.db.OrmDao;
import org.liaohailong.library.http.Http;
import org.liaohailong.library.http.HttpCallback;
import org.liaohailong.library.image.ImageLoader;
import org.liaohailong.library.inject.BindContentView;
import org.liaohailong.library.inject.BindView;
import org.liaohailong.library.inject.OnClick;
import org.liaohailong.library.inject.SaveState;
import org.liaohailong.library.util.ToastUtil;
import org.liaohailong.pdftestapp.BaseFragment;
import org.liaohailong.pdftestapp.R;
import org.liaohailong.pdftestapp.activity.BoomActivity;
import org.liaohailong.pdftestapp.activity.ImageActivity;
import org.liaohailong.pdftestapp.activity.KotlinActivity;
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
    //    private String imageUrl03 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1505106699538&di=6e7649394fca8898968dd0a1388c9b76&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20160829%2F24997e71d5814cf48f307d7caece946c.gif";
    private String[] urls = new String[2];
    private int imageUrlIndex = 0;
    private int httpRequestIndex = 0;
    private Mouse<String, String> mouse = new Mouse<String, String>() {
        @Override
        public String run(String params) {
            String text = "";
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(params).openConnection();
                int code = urlConnection.getResponseCode();
                text = "code = " + code;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return text;
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Async.create(mouse)
                .mouseOn(Schedulers.IO_THREAD)
                .catOn(Schedulers.UI_THREAD)
                .subscribe(new Cat<String>() {
                    @Override
                    public void chase(String s) {
                        textView.setText(s);
                    }
                })
                .execute("https://www.baidu.com/");
        httpRequestIndex = 0;
        for (int i = 0; i < 100; i++) {
            Http.create().url("https://www.baidu.com/")
//                    .worker(new HttpUrlConnectionWorker())
                    .execute(new HttpCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            httpRequestIndex++;
                            ToastUtil.show("Http请求成功 i = " + httpRequestIndex + "  result = " + result);
                        }

                        @Override
                        public void onFailure(int code, String info) {

                        }
                    });
        }

        OrmDao<Student> dao = Orm.create(Student.class);
        dao.save(new Student("小明", 1, 18));
        dao.save(new Student("小红", 0, 17));
        dao.save(new Student("小芳", 0, 16));

        List<Student> students = dao.queryAll();
        students.clear();

        urls[0] = imageUrl01;
        urls[1] = imageUrl02;
//        urls[2] = imageUrl03;
    }

    @OnClick({R.id.text_fragment, R.id.kotlin_btn, R.id.bomb_btn, R.id.jni_btn})
    public void recordCount(View v) {
        switch (v.getId()) {
            case R.id.text_fragment:
                count++;
                textView.setText(TEXT + count + "次");
                break;
            case R.id.kotlin_btn:
                KotlinActivity.Companion.show(getContext());
                break;
            case R.id.bomb_btn:
                BoomActivity.show(getContext());
                break;
            case R.id.jni_btn:
                ImageActivity.show(getContext());
                break;
        }
    }

    @OnClick(R.id.jni_btn)
    private void showJni(View v) {
        Toast.makeText(v.getContext(), "没有了", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.avatar)
    private void showImage(View view) {
        String url = urls[imageUrlIndex % urls.length];
        imageUrlIndex++;
        ImageLoader.getInstance().setImage(avatar, url);
    }
}
