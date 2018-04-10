package org.liaohailong.pdftestapp.activity;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import org.liaohailong.pdftestapp.BaseActivity;
import org.liaohailong.pdftestapp.widget.opengl.FlatColoredSquare;
import org.liaohailong.pdftestapp.widget.opengl.GLDrawer;
import org.liaohailong.pdftestapp.widget.opengl.OpenGLRenderer;
import org.liaohailong.pdftestapp.widget.opengl.SmoothColoredSquare;

import java.util.LinkedList;
import java.util.List;

/**
 * Describe as : OpenGl ES 绘制界面
 * Created by LHL on 2018/4/10.
 */

public class GLActivity extends BaseActivity {
    public static void show(Context context) {
        Intent intent = new Intent(context, GLActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);

        OpenGLRenderer openGLRenderer = new OpenGLRenderer();

        List<GLDrawer> drawerList = new LinkedList<>();
        //第一个方块
        SmoothColoredSquare smoothColoredSquare = new SmoothColoredSquare();
        smoothColoredSquare.setColors(new float[]{
                1.0f, 0f, 0f, 1.0f, //第一个顶点 红色
                0.0f, 1.0f, 0f, 1.0f, //第二个顶点 绿色
                0.0f, 0f, 1.0f, 1.0f, //第三个顶点 红色
                1.0f, 0f, 1.0f, 1.0f, //第四个顶点 品红
        });
        drawerList.add(smoothColoredSquare);

        //第二个方块
        FlatColoredSquare flatColoredSquare = new FlatColoredSquare();
        flatColoredSquare.setColor(new float[]{0.0f, 1.0f, 0.0f, 1.0f});
        drawerList.add(flatColoredSquare);

        //第三个方块
        smoothColoredSquare = new SmoothColoredSquare();
        smoothColoredSquare.setColors(new float[]{
                1.0f, 0f, 0f, 1.0f, //第一个顶点 红色
                0.0f, 1.0f, 0f, 1.0f, //第二个顶点 绿色
                0.0f, 0f, 1.0f, 1.0f, //第三个顶点 红色
                1.0f, 0f, 1.0f, 1.0f, //第四个顶点 品红
        });
        drawerList.add(smoothColoredSquare);

        //设置模型
        openGLRenderer.setGLDrawer(drawerList);

        //设置绘制器
        glSurfaceView.setRenderer(openGLRenderer);
        setContentView(glSurfaceView);
    }
}
